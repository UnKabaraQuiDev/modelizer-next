package lu.kbra.modelizer_next.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.ChannelComparator;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

final class RemoteUpdateService {

	static final class UpdateManifest {
		public UpdateRelease release;
		public UpdateRelease snapshot;
		public UpdateRelease nightly;

		UpdateRelease channel(final UpdateChannel channel) {
			return switch (channel) {
			case RELEASE -> this.release;
			case SNAPSHOT -> this.snapshot;
			case NIGHTLY -> this.nightly;
			};
		}
	}

	static final class UpdateRelease {
		public ParsedVersion version;
		public String url;
		public String releaseUrl;
		public String notes;
		public String tag;

		String releaseUrlOrDefault() {
			return this.releaseUrl == null || this.releaseUrl.isBlank() ? BootstrapApp.RELEASES_URL : this.releaseUrl;
		}
	}

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	void download(final AvailableUpdate update, final Path destination, final ProgressListener listener) throws IOException {
		if (update == null || update.downloadUri() == null) {
			throw new IOException("No downloadable update is available.");
		}
		this.download(update.downloadUri(), destination, update.latestVersion().toString(), listener);
	}

	void download(final URI downloadUri, final Path destination, final String displayVersion, final ProgressListener listener)
			throws IOException {
		if (downloadUri == null) {
			throw new IOException("No downloadable update is available.");
		}
		try {
			Files.createDirectories(destination.getParent());
			final HttpRequest request = HttpRequest.newBuilder(downloadUri)
					.header("Accept", "application/octet-stream")
					.header("User-Agent", BootstrapApp.NAME + "/" + BootstrapApp.VERSION)
					.timeout(Duration.ofMinutes(10))
					.GET()
					.build();
			final HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IOException("Failed to download update: HTTP " + response.statusCode());
			}
			final long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
			try (InputStream inputStream = response.body(); var outputStream = Files.newOutputStream(destination)) {
				final byte[] buffer = new byte[8192];
				long copied = 0L;
				listener.onProgress("Downloading " + displayVersion + "...",
						0,
						totalBytes > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(totalBytes, 1L));
				for (int read = inputStream.read(buffer); read >= 0; read = inputStream.read(buffer)) {
					outputStream.write(buffer, 0, read);
					copied += read;
					if (totalBytes > 0) {
						listener.onProgress("Downloading " + displayVersion + "...",
								(int) Math.min(copied, Integer.MAX_VALUE),
								(int) Math.min(totalBytes, Integer.MAX_VALUE));
					}
				}
			}
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while downloading update.", ex);
		}
	}

	BootstrapInstallerUpdate findLatestBootstrapInstaller(final UpdateChannel channel, final ParsedVersion currentVersion)
			throws IOException, InterruptedException {
		final JsonNode manifest = this.fetchReleaseManifestJson();
		final JsonNode bootstrap = this.findBootstrapNode(manifest, channel);
		if (bootstrap == null || bootstrap.isMissingNode() || bootstrap.isNull()) {
			return new BootstrapInstallerUpdate(currentVersion,
					currentVersion,
					null,
					URI.create(BootstrapApp.RELEASES_URL),
					BootstrapInstallerUpdate.Platform.UNSUPPORTED);
		}

		final String versionText = bootstrap.path("version").asText();
		final BootstrapInstallerUpdate.Platform platform = this.detectPlatform();
		final URI installerUri = this.findInstallerUri(bootstrap, platform);
		URI releasePageUri;
		try {
			releasePageUri = URI.create(bootstrap.path("releaseUrl").asText());
		} catch (IllegalArgumentException e) {
			releasePageUri = null;
		}
		return new BootstrapInstallerUpdate(currentVersion,
				VersionComparator.parse(versionText),
				installerUri,
				releasePageUri == null ? URI.create(BootstrapApp.RELEASES_URL) : releasePageUri,
				platform);
	}

	UpdateManifest fetchManifest() throws IOException, InterruptedException {
		return BootstrapApp.MAPPER.treeToValue(this.fetchManifestJson(), UpdateManifest.class);
	}

	JsonNode fetchReleaseManifestJson() throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(BootstrapApp.RELEASES_MANIFEST_URL))
				.header("Accept", "application/json")
				.header("User-Agent", BootstrapApp.NAME + "/" + BootstrapApp.VERSION)
				.timeout(Duration.ofSeconds(20))
				.GET()
				.build();

		final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch versions manifest: HTTP " + response.statusCode());
		}
		return BootstrapApp.MAPPER.readTree(response.body());
	}

	JsonNode fetchManifestJson() throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(BootstrapApp.UPDATES_MANIFEST_URL))
				.header("Accept", "application/json")
				.header("User-Agent", BootstrapApp.NAME + "/" + BootstrapApp.VERSION)
				.timeout(Duration.ofSeconds(20))
				.GET()
				.build();

		final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch versions manifest: HTTP " + response.statusCode());
		}
		return BootstrapApp.MAPPER.readTree(response.body());
	}

	AvailableUpdate findLatest(final UpdateChannel channel, final ParsedVersion currentVersion) throws IOException, InterruptedException {
		final UpdateManifest manifest = this.fetchManifest();
		final UpdateRelease release = manifest.channel(channel);
		if (release == null || release.version == null || release.url == null || release.url.isBlank()) {
			throw new IOException("No release configured for channel '" + channel.manifestKey() + "'.");
		}
		final ParsedVersion normalizedCurrent = currentVersion == null ? VersionComparator.parse("0.0.0") : currentVersion;
		if (ChannelComparator.PARSED_COMPARATOR.compare(release.version, normalizedCurrent) == 0
				&& VersionComparator.PARSED_COMPARATOR.compare(release.version, normalizedCurrent) <= 0) {
			return new AvailableUpdate(channel,
					normalizedCurrent,
					normalizedCurrent,
					release.notes,
					null,
					URI.create(release.releaseUrlOrDefault()));
		}

		return new AvailableUpdate(channel,
				normalizedCurrent,
				release.version,
				release.notes,
				URI.create(release.url),
				URI.create(release.releaseUrlOrDefault()));
	}

	private BootstrapInstallerUpdate.Platform detectPlatform() {
		final String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			return BootstrapInstallerUpdate.Platform.WINDOWS;
		}
		if (os.contains("mac") || os.contains("darwin")) {
			return BootstrapInstallerUpdate.Platform.MACOS;
		}
		if (os.contains("linux")) {
			return BootstrapInstallerUpdate.Platform.LINUX;
		}
		return BootstrapInstallerUpdate.Platform.UNSUPPORTED;
	}

	private JsonNode findBootstrapNode(final JsonNode manifest, final UpdateChannel channel) {
		final String latest = manifest.path("version").asText();
		final BootstrapInstallerUpdate.Platform platform = this.detectPlatform();
		final JsonNode entries = manifest.path("entries");
		if (entries.isArray()) {
			for (final JsonNode entry : entries) {
				if (entry != null && entry.isObject() && Objects.equals(latest, entry.path("version").asText())) {
					return entry;
				}
			}
		}
		return null;
	}

	private URI findInstallerUri(final JsonNode bootstrap, final BootstrapInstallerUpdate.Platform platform) {
		final JsonNode assets = bootstrap.path("assets");
		if (platform == BootstrapInstallerUpdate.Platform.UNSUPPORTED || assets == null || assets.isMissingNode() || !assets.isArray()) {
			return null;
		}
		for (JsonNode node : assets) {
			if (node.path("platform").asText().equals(platform.name().toLowerCase())
					&& "bootstrap-native".equals(node.path("kind").asText())) {
				try {
					return URI.create(node.path("url").asText());
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

	private URI toUri(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return URI.create(value);
	}
}