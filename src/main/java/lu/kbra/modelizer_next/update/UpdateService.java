package lu.kbra.modelizer_next.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.common.VersionComparator;

public class UpdateService {

	private final HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	public UpdateCheckResult checkForUpdates() throws IOException, InterruptedException {
		final URI manifestUri = URI.create(App.UPDATES_MANIFEST_URL);
		final HttpRequest request = HttpRequest.newBuilder(manifestUri)
				.header("Accept", "application/json")
				.header("User-Agent", App.NAME + "/" + App.VERSION)
				.timeout(Duration.ofSeconds(15))
				.GET()
				.build();

		final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch update manifest: HTTP " + response.statusCode());
		}

		final UpdateManifest manifest = MNMain.OBJECT_MAPPER.readValue(response.body(), UpdateManifest.class);
		final UpdateRelease release = manifest == null ? null : manifest.stable;
		if (release == null || release.version == null || release.version.isBlank()) {
			throw new IOException("Update manifest does not define a stable release.");
		}

		final String currentVersion = this.normalizeVersion(App.VERSION);
		final String latestVersion = this.normalizeVersion(release.version);
		final URI releasePageUri = this.resolveReleasePageUri(release);
		if (VersionComparator.COMPARATOR.compare(latestVersion, currentVersion) <= 0) {
			return UpdateCheckResult.upToDate(currentVersion, releasePageUri);
		}

		final UpdatePlatform platform = UpdatePlatform.detect();
		final UpdateAsset asset = release.assets == null ? null : release.assets.get(platform.manifestKey());
		if (asset == null || asset.name == null || asset.name.isBlank()) {
			throw new IOException("No update asset configured for platform '" + platform.manifestKey() + "'.");
		}

		final URI downloadUri = this.resolveDownloadUri(release, asset);
		return new UpdateCheckResult(true,
				currentVersion,
				latestVersion,
				asset.name,
				downloadUri,
				releasePageUri,
				release.notes);
	}

	public Path downloadUpdate(final UpdateCheckResult update) throws IOException, InterruptedException {
		if (update == null || !update.updateAvailable() || update.downloadUri() == null || update.assetName() == null
				|| update.assetName().isBlank()) {
			throw new IllegalArgumentException("No downloadable update was provided.");
		}

		final Path downloadDirectory = App.getUpdateDownloadsDirectory().toPath();
		Files.createDirectories(downloadDirectory);

		Path target = downloadDirectory.resolve(update.assetName());
		if (Files.exists(target)) {
			target = this.findAvailableTarget(target);
		}

		final HttpRequest request = HttpRequest.newBuilder(update.downloadUri())
				.header("Accept", "application/octet-stream")
				.header("User-Agent", App.NAME + "/" + App.VERSION)
				.timeout(Duration.ofMinutes(10))
				.GET()
				.build();

		final HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to download update asset: HTTP " + response.statusCode());
		}

		try (InputStream inputStream = response.body()) {
			Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
		}

		return target;
	}

	private Path findAvailableTarget(final Path original) {
		final String fileName = original.getFileName().toString();
		final int extensionIndex = fileName.lastIndexOf('.');
		final String baseName = extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
		final String extension = extensionIndex >= 0 ? fileName.substring(extensionIndex) : "";

		int counter = 1;
		Path candidate = original;
		while (Files.exists(candidate)) {
			candidate = original.getParent().resolve(baseName + "-" + counter + extension);
			counter++;
		}
		return candidate;
	}

	private URI resolveDownloadUri(final UpdateRelease release, final UpdateAsset asset) {
		if (asset.url != null && !asset.url.isBlank()) {
			return URI.create(asset.url);
		}

		if (release.tag == null || release.tag.isBlank()) {
			throw new IllegalArgumentException("The update manifest must provide either an asset URL or a release tag.");
		}

		final String encodedTag = this.encodePathSegment(release.tag);
		final String encodedName = this.encodePathSegment(asset.name);
		return URI.create(App.REPOSITORY_URL + "/releases/download/" + encodedTag + "/" + encodedName);
	}

	private URI resolveReleasePageUri(final UpdateRelease release) {
		if (release.releaseUrl != null && !release.releaseUrl.isBlank()) {
			return URI.create(release.releaseUrl);
		}
		if (release.tag != null && !release.tag.isBlank()) {
			return URI.create(App.REPOSITORY_URL + "/releases/tag/" + this.encodePathSegment(release.tag));
		}
		return URI.create(App.REPOSITORY_URL + "/releases");
	}

	private String encodePathSegment(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private String normalizeVersion(final String value) {
		if (value == null) {
			return "0.0.0";
		}
		final String trimmed = value.trim();
		if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
			return trimmed.substring(1);
		}
		return trimmed;
	}
}

final class UpdateManifest {
	public UpdateRelease stable;
}

final class UpdateRelease {
	public String version;
	public String tag;
	public String releaseUrl;
	public String notes;
	public Map<String, UpdateAsset> assets = new LinkedHashMap<>();
}

final class UpdateAsset {
	public String name;
	public String url;
}

enum UpdatePlatform {
	WINDOWS("windows"),
	LINUX("linux"),
	MAC("mac");

	private final String manifestKey;

	UpdatePlatform(final String manifestKey) {
		this.manifestKey = manifestKey;
	}

	String manifestKey() {
		return this.manifestKey;
	}

	static UpdatePlatform detect() {
		final String osName = System.getProperty("os.name", "").toLowerCase();
		if (osName.contains("win")) {
			return WINDOWS;
		}
		if (osName.contains("mac")) {
			return MAC;
		}
		return LINUX;
	}
}
