package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public final class UpdateRuntimes {

	private static final class NoOpUpdateRuntime implements UpdateRuntime {

		@Override
		public AvailableUpdate checkForUpdates() throws IOException {
			return new AvailableUpdate(UpdateChannel.RELEASE, null, null, null, null, null);
		}

		@Override
		public long getInstalledUpdatesDiskUsageBytes() throws IOException {
			return 0L;
		}

		@Override
		public int getInstalledUpdatesFileCount() throws IOException {
			return 0;
		}

		@Override
		public Path getInstalledUpdatesDirectory() {
			return null;
		}

		@Override
		public long freeUnusedInstalledUpdates() throws IOException {
			return 0L;
		}

		@Override
		public BootstrapConfig getBootstrapConfig() {
			return null;
		}

		@Override
		public JsonNode getBootstrapJson() {
			return null;
		}

		@Override
		public ParsedVersion getCurrentApplicationVersion() {
			return null;
		}

		@Override
		public UpdateChannel getSelectedChannel() {
			return UpdateChannel.RELEASE;
		}

		@Override
		public boolean installUpdateAndExit(
				final Component parentComponent,
				final AvailableUpdate update,
				final UpdatePreparation preparation) throws IOException {
			return false;
		}

		@Override
		public boolean isAutoCheckUpdates() {
			return false;
		}

		@Override
		public boolean isAutomaticUpdateChecksEnabledByProperty() {
			return false;
		}

		@Override
		public boolean isAvailable() {
			return false;
		}

		@Override
		public void setAutoCheckUpdates(final boolean enabled) {
		}

		@Override
		public void setSelectedChannel(final UpdateChannel updateChannel) {
		}

	}

	private static final UpdateRuntime NO_OP_RUNTIME = new NoOpUpdateRuntime();

	private static volatile UpdateRuntime runtime = UpdateRuntimes.NO_OP_RUNTIME;

	public static UpdateRuntime getInstance() {
		return UpdateRuntimes.runtime;
	}

	public static void install(final UpdateRuntime updateRuntime) {
		UpdateRuntimes.runtime = Objects.requireNonNullElse(updateRuntime, UpdateRuntimes.NO_OP_RUNTIME);
	}

	public static boolean isActive() {
		return UpdateRuntimes.runtime.isAvailable();
	}

	private UpdateRuntimes() {
	}

}
