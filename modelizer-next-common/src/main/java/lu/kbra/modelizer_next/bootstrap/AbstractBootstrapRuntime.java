package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;

public class AbstractBootstrapRuntime {

	@FunctionalInterface
	public interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

	protected static AbstractBootstrapRuntime INSTANCE = new AbstractBootstrapRuntime();

	public static AbstractBootstrapRuntime getInstance() {
		return AbstractBootstrapRuntime.INSTANCE;
	}

	public static boolean isActive() {
		return false;
	}

	protected AbstractBootstrapRuntime() {
	}

	public AvailableUpdate checkForUpdates() throws IOException {
		return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
	}

	public long getInstalledUpdatesDiskUsageBytes() throws IOException {
		return 0L;
	}

	public int getInstalledUpdatesFileCount() throws IOException {
		return 0;
	}

	public Path getInstalledUpdatesDirectory() {
		return null;
	}

	public long freeUnusedInstalledUpdates() throws IOException {
		return 0L;
	}

	public String getCurrentApplicationVersion() {
		return null;
	}

	public UpdateChannel getSelectedChannel() {
		return UpdateChannel.RELEASE;
	}

	public boolean installUpdateAndExit(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
			throws IOException {
		return false;
	}

	public boolean isAutoCheckUpdates() {
		return false;
	}

	public boolean isAutomaticUpdateChecksEnabledByProperty() {
		return false;
	}

	public void setAutoCheckUpdates(final boolean enabled) {
	}

	public void setSelectedChannel(final UpdateChannel updateChannel) {
	}

}
