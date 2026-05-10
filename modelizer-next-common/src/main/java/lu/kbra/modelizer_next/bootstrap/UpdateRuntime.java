package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Runtime contract exposed to the launched application for update checks and restart/update flow.
 */
public interface UpdateRuntime {

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

	/**
	 * Checks the for updates.
	 *
	 * @return the check for updates result
	 * @throws IOException if the operation cannot be completed
	 */
	AvailableUpdate checkForUpdates() throws IOException;

	/**
	 * Returns the installed updates disk usage bytes.
	 *
	 * @return the installed updates disk usage bytes
	 * @throws IOException if the operation cannot be completed
	 */
	long getInstalledUpdatesDiskUsageBytes() throws IOException;

	/**
	 * Returns the installed updates file count.
	 *
	 * @return the installed updates file count
	 * @throws IOException if the operation cannot be completed
	 */
	int getInstalledUpdatesFileCount() throws IOException;

	/**
	 * Returns the installed updates directory.
	 *
	 * @return the installed updates directory
	 */
	Path getInstalledUpdatesDirectory();

	/**
	 * Deletes installed update folders that are no longer needed.
	 *
	 * @return the free unused installed updates result
	 * @throws IOException if the operation cannot be completed
	 */
	long freeUnusedInstalledUpdates() throws IOException;

	/**
	 * Returns the bootstrap config.
	 *
	 * @return the bootstrap config
	 */
	BootstrapConfig getBootstrapConfig();

	/**
	 * Returns the bootstrap JSON.
	 *
	 * @return the bootstrap JSON
	 */
	JsonNode getBootstrapJson();

	/**
	 * Returns the current application version.
	 *
	 * @return the current application version
	 */
	ParsedVersion getCurrentApplicationVersion();

	/**
	 * Returns the selected channel during bootstrap/update processing.
	 *
	 * @return the selected channel
	 */
	UpdateChannel getSelectedChannel();

	/**
	 * Installs the update and restart.
	 *
	 * @param parentComponent parent component value used by the operation
	 * @param update          update metadata to download or install
	 * @param preparation     preparation value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 * @throws IOException if the operation cannot be completed
	 */
	boolean installUpdateAndRestart(Component parentComponent, AvailableUpdate update, UpdatePreparation preparation) throws IOException;

	/**
	 * Checks whether auto check updates is enabled or applies.
	 *
	 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
	 */
	boolean isAutoCheckUpdates();

	/**
	 * Checks whether automatic update checks enabled by property is enabled or applies.
	 *
	 * @return {@code true} if automatic update checks enabled by property is enabled or applies;
	 *         otherwise {@code
	 *         false}
	 */
	boolean isAutomaticUpdateChecksEnabledByProperty();

	/**
	 * Checks whether available is enabled or applies during bootstrap/update processing.
	 *
	 * @return {@code true} if available is enabled or applies; otherwise {@code false}
	 */
	boolean isAvailable();

	/**
	 * Sets the auto check updates.
	 *
	 * @param enabled whether enabled is enabled
	 */
	void setAutoCheckUpdates(boolean enabled);

	/**
	 * Sets the selected channel during bootstrap/update processing.
	 *
	 * @param updateChannel update channel value used by the operation
	 */
	void setSelectedChannel(UpdateChannel updateChannel);

}
