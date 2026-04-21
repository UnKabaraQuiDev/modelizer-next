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

import com.fasterxml.jackson.databind.ObjectMapper;

import lu.kbra.modelizer_next.common.VersionComparator;

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
		public String version;
		public String url;
		public String releaseUrl;
		public String notes;
		public String tag;

		String releaseUrlOrDefault() {
			return this.releaseUrl == null || this.releaseUrl.isBlank() ? BootstrapApp.RELEASES_URL : this.releaseUrl;
		}

	}

	private final ObjectMapper mapper = new ObjectMapper();

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	void download(final AvailableUpdate update, final Path destination, final ProgressListener listener) throws IOException {
		if (update == null || update.downloadUri() == null) {
			throw new IOException("No downloadable update is available.");
		}
		try {
			Files.createDirectories(destination.getParent());
			final HttpRequest request = HttpRequest.newBuilder(update.downloadUri())
					.header("Accept", "application/octet-stream")
					.header("User-Agent", BootstrapApp.NAME + "/" + BootstrapApp.VERSION)
					.timeout(Duration.ofMinutes(10))
					.GET()
					.build();
			final HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IOException("Failed to download update jar: HTTP " + response.statusCode());
			}
			final long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
			try (InputStream inputStream = response.body(); var outputStream = Files.newOutputStream(destination)) {
				final byte[] buffer = new byte[8192];
				long copied = 0L;
				listener.onProgress("Downloading " + update.latestVersion() + "...",
						0,
						totalBytes > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(totalBytes, 1L));
				for (int read = inputStream.read(buffer); read >= 0; read = inputStream.read(buffer)) {
					outputStream.write(buffer, 0, read);
					copied += read;
					if (totalBytes > 0) {
						listener.onProgress("Downloading " + update.latestVersion() + "...",
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

	AvailableUpdate findLatest(final UpdateChannel channel, final String currentVersion) throws IOException, InterruptedException {
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

		final UpdateManifest manifest = this.mapper.readValue(response.body(), UpdateManifest.class);
		final UpdateRelease release = manifest.channel(channel);
		if (release == null || release.version == null || release.version.isBlank() || release.url == null || release.url.isBlank()) {
			throw new IOException("No release configured for channel '" + channel.manifestKey() + "'.");
		}

		final String normalizedCurrent = currentVersion == null || currentVersion.isBlank() ? "0.0.0" : currentVersion;
		if (VersionComparator.COMPARATOR.compare(release.version, normalizedCurrent) <= 0) {
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
}
