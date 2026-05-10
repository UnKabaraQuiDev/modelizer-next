package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Base implementation of update runtime behavior shared with application-side update checks.
 */
public class AbstractBootstrapRuntime {

	/**
	 * Defines operations for update preparation behavior.
	 */
	@FunctionalInterface
	public interface UpdatePreparation {
		/**
		 * Prepares the for exit during bootstrap/update processing.
		 *
		 * @return {@code true} when the condition is met; otherwise {@code false}
		 * @throws IOException if the operation cannot be completed
		 */
		boolean prepareForExit() throws IOException;
	}

	protected static AbstractBootstrapRuntime INSTANCE = new AbstractBootstrapRuntime();

	/**
	 * Returns the instance during bootstrap/update processing.
	 *
	 * @return the instance
	 */
	public static AbstractBootstrapRuntime getInstance() {
		return AbstractBootstrapRuntime.INSTANCE;
	}

	/**
	 * Checks whether active is enabled or applies during bootstrap/update processing.
	 *
	 * @return {@code true} if active is enabled or applies; otherwise {@code false}
	 */
	public static boolean isActive() {
		return false;
	}

	/**
	 * Creates an abstract bootstrap runtime instance.
	 */
	protected AbstractBootstrapRuntime() {
	}

	/**
	 * Checks the for updates.
	 *
	 * @return the check for updates result
	 * @throws IOException if the operation cannot be completed
	 */
	public AvailableUpdate checkForUpdates() throws IOException {
		return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
	}

	/**
	 * Returns the installed updates disk usage bytes.
	 *
	 * @return the installed updates disk usage bytes
	 * @throws IOException if the operation cannot be completed
	 */
	public long getInstalledUpdatesDiskUsageBytes() throws IOException {
		return 0L;
	}

	/**
	 * Returns the installed updates file count.
	 *
	 * @return the installed updates file count
	 * @throws IOException if the operation cannot be completed
	 */
	public int getInstalledUpdatesFileCount() throws IOException {
		return 0;
	}

	/**
	 * Returns the installed updates directory.
	 *
	 * @return the installed updates directory
	 */
	public Path getInstalledUpdatesDirectory() {
		return null;
	}

	/**
	 * Deletes installed update folders that are no longer needed.
	 *
	 * @return the free unused installed updates result
	 * @throws IOException if the operation cannot be completed
	 */
	public long freeUnusedInstalledUpdates() throws IOException {
		return 0L;
	}

	/**
	 * Returns the current application version.
	 *
	 * @return the current application version
	 */
	public String getCurrentApplicationVersion() {
		return null;
	}

	/**
	 * Returns the selected channel during bootstrap/update processing.
	 *
	 * @return the selected channel
	 */
	public UpdateChannel getSelectedChannel() {
		return UpdateChannel.RELEASE;
	}

	/**
	 * Installs the update and exit.
	 *
	 * @param parentComponent parent component value used by the operation
	 * @param update          update metadata to download or install
	 * @param preparation     preparation value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 * @throws IOException if the operation cannot be completed
	 */
	public boolean installUpdateAndExit(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
			throws IOException {
		return false;
	}

	/**
	 * Checks whether auto check updates is enabled or applies.
	 *
	 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
	 */
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
	public boolean isAutomaticUpdateChecksEnabledByProperty() {
		return false;
	}

	/**
	 * Sets the auto check updates.
	 *
	 * @param enabled whether enabled is enabled
	 */
	public void setAutoCheckUpdates(final boolean enabled) {
	}

	/**
	 * Sets the selected channel during bootstrap/update processing.
	 *
	 * @param updateChannel update channel value used by the operation
	 */
	public void setSelectedChannel(final UpdateChannel updateChannel) {
	}

}
