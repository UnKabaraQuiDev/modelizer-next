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

/**
 * Handles paths and file operations for downloaded application update artifacts.
 */
public final class ApplicationUpdateStorage {

	public static final int MAX_RETAINED_UPDATES_PER_CHANNEL = 3;

	private final ApplicationInventory inventory;

	/**
	 * Creates an application update storage instance.
	 */
	public ApplicationUpdateStorage() {
		this(new ApplicationInventory());
	}

	/**
	 * Creates an application update storage instance.
	 *
	 * @param inventory inventory value used by the operation
	 */
	public ApplicationUpdateStorage(final ApplicationInventory inventory) {
		this.inventory = inventory;
	}

	/**
	 * Calculates the disk usage bytes during bootstrap/update processing.
	 *
	 * @return the calculate disk usage bytes result
	 * @throws IOException if the operation cannot be completed
	 */
	public long calculateDiskUsageBytes() throws IOException {
		final Path directory = this.getUpdatesDirectory();
		if (!Files.isDirectory(directory)) {
			return 0L;
		}
		try (var stream = Files.walk(directory)) {
			return stream.filter(Files::isRegularFile).mapToLong(this::sizeOf).sum();
		}
	}

	/**
	 * Counts regular files in the supplied directory tree.
	 *
	 * @return the count files result
	 * @throws IOException if the operation cannot be completed
	 */
	public int countFiles() throws IOException {
		final Path directory = this.getUpdatesDirectory();
		if (!Files.isDirectory(directory)) {
			return 0;
		}
		try (var stream = Files.list(directory)) {
			return (int) stream.filter(Files::isRegularFile).count();
		}
	}

	/**
	 * Deletes the if exists during bootstrap/update processing.
	 *
	 * @param path file system path to read or write
	 * @return the delete if exists result
	 * @throws IOException if the operation cannot be completed
	 */
	private long deleteIfExists(final Path path) throws IOException {
		final long size = this.sizeOf(path);
		return Files.deleteIfExists(path) ? size : 0L;
	}

	/**
	 * Deletes downloaded update files that are no longer needed.
	 *
	 * @param activeChannel     active channel value used by the operation
	 * @param activeApplication active application value used by the operation
	 * @return the free unused updates result
	 * @throws IOException if the operation cannot be completed
	 */
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
				.limit(ApplicationUpdateStorage.MAX_RETAINED_UPDATES_PER_CHANNEL)
				.map(app -> app.jarFile().toAbsolutePath().normalize())
				.forEach(keep::add);

		long freed = 0L;
		for (final InstalledApplication app : installed) {
			final Path path = app.jarFile().toAbsolutePath().normalize();
			if (keep.contains(path)) {
				continue;
			}
			freed += this.deleteIfExists(path);
		}

		try (var stream = Files.list(directory)) {
			for (final Path path : stream.filter(Files::isRegularFile).filter(this::isTemporaryUpdateFile).toList()) {
				freed += this.deleteIfExists(path);
			}
		}
		return freed;
	}

	/**
	 * Returns the updates directory.
	 *
	 * @return the updates directory
	 */
	public Path getUpdatesDirectory() {
		return BootstrapApp.getApplicationsDirectory().toPath();
	}

	/**
	 * Checks whether temporary update file is enabled or applies.
	 *
	 * @param path file system path to read or write
	 * @return {@code true} if temporary update file is enabled or applies; otherwise {@code false}
	 */
	private boolean isTemporaryUpdateFile(final Path path) {
		final String name = path.getFileName().toString();
		return name.endsWith(".part") || name.endsWith(".tmp");
	}

	/**
	 * Returns the size of a file or directory tree in bytes.
	 *
	 * @param path file system path to read or write
	 * @return the size of result
	 */
	private long sizeOf(final Path path) {
		try {
			return Files.size(path);
		} catch (final IOException ex) {
			return 0L;
		}
	}
}
