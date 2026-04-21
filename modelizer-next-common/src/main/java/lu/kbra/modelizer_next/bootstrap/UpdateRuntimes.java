package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.util.Objects;

public final class UpdateRuntimes {

	private static final UpdateRuntime NO_OP_RUNTIME = new NoOpUpdateRuntime();
	private static volatile UpdateRuntime runtime = NO_OP_RUNTIME;

	public static UpdateRuntime getInstance() {
		return runtime;
	}

	public static boolean isActive() {
		return runtime.isAvailable();
	}

	public static void install(final UpdateRuntime updateRuntime) {
		runtime = Objects.requireNonNullElse(updateRuntime, NO_OP_RUNTIME);
	}

	private UpdateRuntimes() {
	}

	private static final class NoOpUpdateRuntime implements UpdateRuntime {

		@Override
		public boolean isAvailable() {
			return false;
		}

		@Override
		public boolean isAutomaticUpdateChecksEnabledByProperty() {
			return false;
		}

		@Override
		public boolean isAutoCheckUpdates() {
			return false;
		}

		@Override
		public void setAutoCheckUpdates(final boolean enabled) {
		}

		@Override
		public UpdateChannel getSelectedChannel() {
			return UpdateChannel.RELEASE;
		}

		@Override
		public void setSelectedChannel(final UpdateChannel updateChannel) {
		}

		@Override
		public String getCurrentApplicationVersion() {
			return null;
		}

		@Override
		public AvailableUpdate checkForUpdates() throws IOException {
			return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
		}

		@Override
		public boolean installUpdateAndExit(final Component parentComponent,
				final AvailableUpdate update,
				final UpdatePreparation preparation) throws IOException {
			return false;
		}
	}

}
