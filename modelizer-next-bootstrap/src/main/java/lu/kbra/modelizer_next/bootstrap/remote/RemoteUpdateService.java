package lu.kbra.modelizer_next.bootstrap.remote;

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
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.bootstrap.AvailableUpdate;
import lu.kbra.modelizer_next.bootstrap.ProgressListener;
import lu.kbra.modelizer_next.bootstrap.UpdateChannel;
import lu.kbra.modelizer_next.bootstrap.config.BootstrapApp;
import lu.kbra.modelizer_next.bootstrap.selfupdate.BootstrapInstallerUpdate;
import lu.kbra.modelizer_next.common.ChannelComparator;
import lu.kbra.modelizer_next.common.Platform;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * HTTP client for fetching update manifests and downloading application updates.
 */
public final class RemoteUpdateService {

	/**
	 * Represents an update manifest in the remote update part of the application.
	 */
	public static final class UpdateManifest {
		public UpdateRelease release;
		public UpdateRelease snapshot;
		public UpdateRelease nightly;
		public ParsedVersion bootstrapVersion;

		/**
		 * Returns the manifest release entry for the requested update channel.
		 *
		 * @param channel update channel to query
		 * @return the channel result
		 */
		public UpdateRelease channel(final UpdateChannel channel) {
			return switch (channel) {
			case RELEASE -> this.release;
			case SNAPSHOT -> this.snapshot;
			case NIGHTLY -> this.nightly;
			};
		}

		@Override
		public String toString() {
			return "UpdateManifest@" + System.identityHashCode(this) + " [release=" + release + ", snapshot=" + snapshot + ", nightly="
					+ nightly + ", bootstrapVersion=" + bootstrapVersion + "]";
		}

	}

	/**
	 * Represents an update release in the remote update part of the application.
	 */
	public static final class UpdateRelease {
		public ParsedVersion version;
		public String url;
		public String releaseUrl;
		public String notes;
		public String tag;

		/**
		 * Returns the configured release URL or the default release page when none is set.
		 *
		 * @return the release URL or default result
		 */
		public String releaseUrlOrDefault() {
			return this.releaseUrl == null || this.releaseUrl.isBlank() ? BootstrapApp.RELEASES_URL : this.releaseUrl;
		}

		@Override
		public String toString() {
			return "UpdateRelease@" + System.identityHashCode(this) + " [version=" + version + ", url=" + url + ", releaseUrl=" + releaseUrl
					+ ", notes=" + notes + ", tag=" + tag + "]";
		}

	}

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	/**
	 * Downloads an update artifact.
	 *
	 * @param update      update metadata to download or install
	 * @param destination destination path that receives generated data
	 * @param listener    listener notified about progress or changes
	 * @throws IOException if the operation cannot be completed
	 */
	public void download(final AvailableUpdate update, final Path destination, final ProgressListener listener) throws IOException {
		if (update == null || update.downloadUri() == null) {
			throw new IOException("No downloadable update is available.");
		}
		this.download(update.downloadUri(), destination, update.latestVersion().toString(), listener);
	}

	/**
	 * Downloads an update artifact.
	 *
	 * @param downloadUri    URI of the file to download
	 * @param destination    destination path that receives generated data
	 * @param displayVersion version text shown in progress messages
	 * @param listener       listener notified about progress or changes
	 * @throws IOException if the operation cannot be completed
	 */
	public void download(final URI downloadUri, final Path destination, final String displayVersion, final ProgressListener listener)
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

	/**
	 * Finds the latest bootstrap installer that matches the supplied input.
	 *
	 * @param channel        update channel to query
	 * @param currentVersion currently installed version
	 * @return the matching latest bootstrap installer, or {@code null} when no match exists
	 * @throws IOException          if the operation cannot be completed
	 * @throws InterruptedException if the operation cannot be completed
	 */
	public BootstrapInstallerUpdate findLatestBootstrapInstaller(final UpdateChannel channel, final ParsedVersion currentVersion)
			throws IOException, InterruptedException {
		final JsonNode manifest = this.fetchReleaseManifestJson();
		final JsonNode bootstrap = this.findBootstrapNode(manifest, channel);
		if (bootstrap == null || bootstrap.isMissingNode() || bootstrap.isNull()) {
			return new BootstrapInstallerUpdate(currentVersion,
					currentVersion,
					null,
					URI.create(BootstrapApp.RELEASES_URL),
					Platform.UNSUPPORTED);
		}

		final String versionText = bootstrap.path("version").asText();
		final Platform platform = this.detectPlatform();
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

	/**
	 * Fetches the manifest.
	 *
	 * @return the fetch manifest result
	 * @throws IOException          if the operation cannot be completed
	 * @throws InterruptedException if the operation cannot be completed
	 */
	public UpdateManifest fetchManifest() throws IOException, InterruptedException {
		return BootstrapApp.MAPPER.treeToValue(this.fetchManifestJson(), UpdateManifest.class);
	}

	/**
	 * Fetches the release manifest JSON.
	 *
	 * @return the fetch release manifest JSON result
	 * @throws IOException          if the operation cannot be completed
	 * @throws InterruptedException if the operation cannot be completed
	 */
	public JsonNode fetchReleaseManifestJson() throws IOException, InterruptedException {
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

	/**
	 * Fetches the manifest JSON.
	 *
	 * @return the fetch manifest JSON result
	 * @throws IOException          if the operation cannot be completed
	 * @throws InterruptedException if the operation cannot be completed
	 */
	public JsonNode fetchManifestJson() throws IOException, InterruptedException {
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

	/**
	 * Finds the latest that matches the supplied input.
	 *
	 * @param channel        update channel to query
	 * @param currentVersion currently installed version
	 * @return the matching latest, or {@code null} when no match exists
	 * @throws IOException          if the operation cannot be completed
	 * @throws InterruptedException if the operation cannot be completed
	 */
	public AvailableUpdate findLatest(final UpdateChannel channel, final ParsedVersion currentVersion)
			throws IOException, InterruptedException {
		final UpdateManifest manifest = this.fetchManifest();
		System.out.println("Versions found: " + manifest);
		System.out.println();
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

	/**
	 * Detects the current value from the runtime environment.
	 *
	 * @return the detect platform result
	 */
	private Platform detectPlatform() {
		return Platform.get();
	}

	/**
	 * Finds the bootstrap node that matches the supplied input.
	 *
	 * @param manifest update manifest to inspect
	 * @param channel  update channel to query
	 * @return the matching bootstrap node, or {@code null} when no match exists
	 */
	private JsonNode findBootstrapNode(final JsonNode manifest, final UpdateChannel channel) {
		final String latest = manifest.path("version").asText();
		final Platform platform = this.detectPlatform();
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

	/**
	 * Finds the installer URI that matches the supplied input.
	 *
	 * @param bootstrap bootstrap value used by the operation
	 * @param platform  target platform to match
	 * @return the matching installer URI, or {@code null} when no match exists
	 */
	private URI findInstallerUri(final JsonNode bootstrap, final Platform platform) {
		final JsonNode assets = bootstrap.path("assets");
		if (platform == Platform.UNSUPPORTED || assets == null || assets.isMissingNode() || !assets.isArray()) {
			return null;
		}
		for (JsonNode node : assets) {
			if (node.path("platform").asText().equals(platform.manifestKey()) && "bootstrap-native".equals(node.path("kind").asText())) {
				try {
					return URI.create(node.path("url").asText());
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

}
