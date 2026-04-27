package lu.kbra.modelizer_next.bootstrap.subapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lu.kbra.modelizer_next.bootstrap.UpdateChannel;
import lu.kbra.modelizer_next.bootstrap.config.BootstrapApp;
import lu.kbra.modelizer_next.common.VersionComparator;

public final class ApplicationUpdateStorage {

	public static final int MAX_RETAINED_UPDATES_PER_CHANNEL = 3;

	private final ApplicationInventory inventory;

	public ApplicationUpdateStorage() {
		this(new ApplicationInventory());
	}

	public ApplicationUpdateStorage(final ApplicationInventory inventory) {
		this.inventory = inventory;
	}

	public Path getUpdatesDirectory() {
		return BootstrapApp.getApplicationsDirectory().toPath();
	}

	public int countFiles() throws IOException {
		final Path directory = this.getUpdatesDirectory();
		if (!Files.isDirectory(directory)) {
			return 0;
		}
		try (var stream = Files.list(directory)) {
			return (int) stream.filter(Files::isRegularFile).count();
		}
	}

	public long calculateDiskUsageBytes() throws IOException {
		final Path directory = this.getUpdatesDirectory();
		if (!Files.isDirectory(directory)) {
			return 0L;
		}
		try (var stream = Files.walk(directory)) {
			return stream.filter(Files::isRegularFile).mapToLong(this::sizeOf).sum();
		}
	}

	public long freeUnusedUpdates(final UpdateChannel activeChannel, final InstalledApplication activeApplication) throws IOException {
		final Path directory = this.getUpdatesDirectory();
		if (!Files.isDirectory(directory)) {
			return 0L;
		}

		final List<InstalledApplication> installed = new ArrayList<>();
		try (var stream = Files.list(directory)) {
			stream.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.map(this.inventory::readInstalledApplication)
					.filter(java.util.Optional::isPresent)
					.map(java.util.Optional::get)
					.forEach(installed::add);
		}

		installed.sort(Comparator.comparing(InstalledApplication::version, VersionComparator.PARSED_COMPARATOR).reversed());

		final List<Path> keep = new ArrayList<>();
		if (activeApplication != null) {
			keep.add(activeApplication.jarFile().toAbsolutePath().normalize());
		}
		installed.stream()
				.filter(app -> app.version().updateChannel() == activeChannel)
				.limit(MAX_RETAINED_UPDATES_PER_CHANNEL)
				.map(app -> app.jarFile().toAbsolutePath().normalize())
				.forEach(keep::add);

		long freed = 0L;
		for (final InstalledApplication app : installed) {
			final Path path = app.jarFile().toAbsolutePath().normalize();
			if (keep.contains(path)) {
				continue;
			}
			freed += deleteIfExists(path);
		}

		try (var stream = Files.list(directory)) {
			for (final Path path : stream.filter(Files::isRegularFile).filter(this::isTemporaryUpdateFile).toList()) {
				freed += deleteIfExists(path);
			}
		}
		return freed;
	}

	private boolean isTemporaryUpdateFile(final Path path) {
		final String name = path.getFileName().toString();
		return name.endsWith(".part") || name.endsWith(".tmp");
	}

	private long deleteIfExists(final Path path) throws IOException {
		final long size = sizeOf(path);
		return Files.deleteIfExists(path) ? size : 0L;
	}

	private long sizeOf(final Path path) {
		try {
			return Files.size(path);
		} catch (final IOException ex) {
			return 0L;
		}
	}
}
