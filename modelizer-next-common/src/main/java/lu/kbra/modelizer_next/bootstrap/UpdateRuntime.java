package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public interface UpdateRuntime {

	@FunctionalInterface
	public interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

	AvailableUpdate checkForUpdates() throws IOException;

	long getInstalledUpdatesDiskUsageBytes() throws IOException;

	int getInstalledUpdatesFileCount() throws IOException;

	Path getInstalledUpdatesDirectory();

	long freeUnusedInstalledUpdates() throws IOException;

	BootstrapConfig getBootstrapConfig();

	JsonNode getBootstrapJson();

	ParsedVersion getCurrentApplicationVersion();

	UpdateChannel getSelectedChannel();

	boolean installUpdateAndExit(Component parentComponent, AvailableUpdate update, UpdatePreparation preparation) throws IOException;

	boolean isAutoCheckUpdates();

	boolean isAutomaticUpdateChecksEnabledByProperty();

	boolean isAvailable();

	void setAutoCheckUpdates(boolean enabled);

	void setSelectedChannel(UpdateChannel updateChannel);

}
