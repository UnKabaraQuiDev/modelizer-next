package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

public interface UpdateRuntime {

	@FunctionalInterface
	interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

	AvailableUpdate checkForUpdates() throws IOException;

	BootstrapConfig getBootstrapConfig();

	JsonNode getBootstrapJson();

	String getCurrentApplicationVersion();

	UpdateChannel getSelectedChannel();

	boolean installUpdateAndExit(Component parentComponent, AvailableUpdate update, UpdatePreparation preparation) throws IOException;

	boolean isAutoCheckUpdates();

	boolean isAutomaticUpdateChecksEnabledByProperty();

	boolean isAvailable();

	void setAutoCheckUpdates(boolean enabled);

	void setSelectedChannel(UpdateChannel updateChannel);

}
