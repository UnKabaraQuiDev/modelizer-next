package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Factory methods for update runtime implementations used when no bootstrap is present.
 */
public final class UpdateRuntimes {

	/**
	 * Represents a no op update runtime in the bootstrap part of the application.
	 */
	private static final class NoOpUpdateRuntime implements UpdateRuntime {

		/**
		 * Checks the for updates.
		 *
		 * @return the check for updates result
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public AvailableUpdate checkForUpdates() throws IOException {
			return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
		}

		/**
		 * Returns the installed updates disk usage bytes.
		 *
		 * @return the installed updates disk usage bytes
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public long getInstalledUpdatesDiskUsageBytes() throws IOException {
			return 0L;
		}

		/**
		 * Returns the installed updates file count.
		 *
		 * @return the installed updates file count
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public int getInstalledUpdatesFileCount() throws IOException {
			return 0;
		}

		/**
		 * Returns the installed updates directory.
		 *
		 * @return the installed updates directory
		 */
		@Override
		public Path getInstalledUpdatesDirectory() {
			return null;
		}

		/**
		 * Deletes installed update folders that are no longer needed.
		 *
		 * @return the free unused installed updates result
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public long freeUnusedInstalledUpdates() throws IOException {
			return 0L;
		}

		/**
		 * Returns the bootstrap config.
		 *
		 * @return the bootstrap config
		 */
		@Override
		public BootstrapConfig getBootstrapConfig() {
			return null;
		}

		/**
		 * Returns the bootstrap JSON.
		 *
		 * @return the bootstrap JSON
		 */
		@Override
		public JsonNode getBootstrapJson() {
			return null;
		}

		/**
		 * Returns the current application version.
		 *
		 * @return the current application version
		 */
		@Override
		public ParsedVersion getCurrentApplicationVersion() {
			return null;
		}

		/**
		 * Returns the selected channel during bootstrap/update processing.
		 *
		 * @return the selected channel
		 */
		@Override
		public UpdateChannel getSelectedChannel() {
			return UpdateChannel.RELEASE;
		}

		/**
		 * Installs the update and restart.
		 *
		 * @param parentComponent parent component value used by the operation
		 * @param update          update metadata to download or install
		 * @param preparation     preparation value used by the operation
		 * @return {@code true} when the condition is met; otherwise {@code false}
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public boolean
				installUpdateAndRestart(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
						throws IOException {
			return false;
		}

		/**
		 * Checks whether auto check updates is enabled or applies.
		 *
		 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
		 */
		@Override
		public boolean isAutoCheckUpdates() {
			return false;
		}

		/**
		 * Checks whether automatic update checks enabled by property is enabled or applies.
		 *
		 * @return {@code true} if automatic update checks enabled by property is enabled or applies;
		 *         otherwise {@code
		 *         false}
		 */
		@Override
		public boolean isAutomaticUpdateChecksEnabledByProperty() {
			return false;
		}

		/**
		 * Checks whether available is enabled or applies during bootstrap/update processing.
		 *
		 * @return {@code true} if available is enabled or applies; otherwise {@code false}
		 */
		@Override
		public boolean isAvailable() {
			return false;
		}

		/**
		 * Sets the auto check updates.
		 *
		 * @param enabled whether enabled is enabled
		 */
		@Override
		public void setAutoCheckUpdates(final boolean enabled) {
		}

		/**
		 * Sets the selected channel during bootstrap/update processing.
		 *
		 * @param updateChannel update channel value used by the operation
		 */
		@Override
		public void setSelectedChannel(final UpdateChannel updateChannel) {
		}

	}

	private static final UpdateRuntime NO_OP_RUNTIME = new NoOpUpdateRuntime();

	private static volatile UpdateRuntime runtime = UpdateRuntimes.NO_OP_RUNTIME;

	/**
	 * Returns the instance during bootstrap/update processing.
	 *
	 * @return the instance
	 */
	public static UpdateRuntime getInstance() {
		return UpdateRuntimes.runtime;
	}

	/**
	 * Creates an InstalledApplication value from an installed jar path.
	 *
	 * @param updateRuntime update runtime value used by the operation
	 */
	public static void install(final UpdateRuntime updateRuntime) {
		UpdateRuntimes.runtime = Objects.requireNonNullElse(updateRuntime, UpdateRuntimes.NO_OP_RUNTIME);
	}

	/**
	 * Checks whether active is enabled or applies during bootstrap/update processing.
	 *
	 * @return {@code true} if active is enabled or applies; otherwise {@code false}
	 */
	public static boolean isActive() {
		return UpdateRuntimes.runtime.isAvailable();
	}

	/**
	 * Creates an update runtimes instance.
	 */
	private UpdateRuntimes() {
	}

}
