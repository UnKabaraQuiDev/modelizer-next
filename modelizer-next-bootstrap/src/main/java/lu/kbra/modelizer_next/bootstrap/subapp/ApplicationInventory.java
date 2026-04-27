package lu.kbra.modelizer_next.bootstrap.subapp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.jar.JarFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lu.kbra.modelizer_next.bootstrap.AvailableUpdate;
import lu.kbra.modelizer_next.bootstrap.ProgressListener;
import lu.kbra.modelizer_next.bootstrap.UpdateChannel;
import lu.kbra.modelizer_next.bootstrap.config.BootstrapApp;
import lu.kbra.modelizer_next.bootstrap.remote.RemoteUpdateService;
import lu.kbra.modelizer_next.common.VersionComparator;

public final class ApplicationInventory {

	private final ObjectMapper mapper = new ObjectMapper();

	public Optional<InstalledApplication> findLatestInstalled() throws IOException {
		final Path applicationsDirectory = BootstrapApp.getApplicationsDirectory().toPath();
		if (!Files.isDirectory(applicationsDirectory)) {
			return Optional.empty();
		}
		try (var stream = Files.list(applicationsDirectory)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.map(this::readInstalledApplication)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.max(Comparator.comparing(InstalledApplication::version, VersionComparator.PARSED_COMPARATOR));
		}
	}

	public Optional<InstalledApplication> findLatestInstalled(UpdateChannel wantedChannel) throws IOException {
		final Path applicationsDirectory = BootstrapApp.getApplicationsDirectory().toPath();
		if (!Files.isDirectory(applicationsDirectory)) {
			return Optional.empty();
		}
		try (var stream = Files.list(applicationsDirectory)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.map(this::readInstalledApplication)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(c -> c.version().updateChannel() == wantedChannel)
					.max(Comparator.comparing(InstalledApplication::version, VersionComparator.PARSED_COMPARATOR));
		}
	}

	public InstalledApplication install(final AvailableUpdate update, final ProgressListener listener) throws IOException {
		final String safeVersion = update.latestVersion().toString().replaceAll("[^A-Za-z0-9._-]", "_");
		final Path target = BootstrapApp.getApplicationsDirectory().toPath().resolve("modelizer-next-app-" + safeVersion + ".jar");
		final Path tmp = BootstrapApp.getTempDirectory().toPath().resolve(target.getFileName().toString() + ".part");
		new RemoteUpdateService().download(update, tmp, listener);
		Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
		return this.readInstalledApplication(target)
				.orElseThrow(() -> new IOException("The downloaded application jar is missing app.json metadata."));
	}

	public Optional<InstalledApplication> readInstalledApplication(final Path jarPath) {
		try (JarFile jarFile = new JarFile(jarPath.toFile())) {
			final var entry = jarFile.getJarEntry("app.json");
			if (entry == null) {
				return Optional.empty();
			}
			try (InputStream inputStream = jarFile.getInputStream(entry)) {
				final JsonNode json = this.mapper.readTree(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
				final String version = json.path("version").asText(null);
				final String entryPoint = json.path("entryPoint").asText(null);
				if (version == null || version.isBlank() || entryPoint == null || entryPoint.isBlank()) {
					return Optional.empty();
				}
				return Optional.of(new InstalledApplication(VersionComparator.parse(version), entryPoint, jarPath));
			}
		} catch (final Exception ex) {
			return Optional.empty();
		}
	}
}
