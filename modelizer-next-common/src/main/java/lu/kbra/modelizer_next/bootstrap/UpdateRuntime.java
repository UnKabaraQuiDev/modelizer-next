package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;

public interface UpdateRuntime {

	boolean isAvailable();

	boolean isAutomaticUpdateChecksEnabledByProperty();

	boolean isAutoCheckUpdates();

	void setAutoCheckUpdates(boolean enabled);

	UpdateChannel getSelectedChannel();

	void setSelectedChannel(UpdateChannel updateChannel);

	String getCurrentApplicationVersion();

	AvailableUpdate checkForUpdates() throws IOException;

	boolean installUpdateAndExit(Component parentComponent, AvailableUpdate update, UpdatePreparation preparation) throws IOException;

	@FunctionalInterface
	interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

}
