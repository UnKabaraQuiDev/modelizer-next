package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;

public class AbstractBootstrapRuntime {

	protected static AbstractBootstrapRuntime INSTANCE = new AbstractBootstrapRuntime();

	protected AbstractBootstrapRuntime() {
	}

	public static boolean isActive() {
		return false;
	}

	public static AbstractBootstrapRuntime getInstance() {
		return INSTANCE;
	}

	public boolean isAutomaticUpdateChecksEnabledByProperty() {
		return false;
	}

	public boolean isAutoCheckUpdates() {
		return false;
	}

	public void setAutoCheckUpdates(final boolean enabled) {
	}

	public UpdateChannel getSelectedChannel() {
		return UpdateChannel.RELEASE;
	}

	public void setSelectedChannel(final UpdateChannel updateChannel) {
	}

	public String getCurrentApplicationVersion() {
		return null;
	}

	public AvailableUpdate checkForUpdates() throws IOException {
		return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
	}

	public boolean installUpdateAndExit(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
			throws IOException {
		return false;
	}

	@FunctionalInterface
	public interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

}
