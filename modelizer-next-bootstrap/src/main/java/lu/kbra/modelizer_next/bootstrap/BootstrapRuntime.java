package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JOptionPane;

public class BootstrapRuntime extends AbstractBootstrapRuntime {

	@FunctionalInterface
	public interface UpdatePreparation {
		boolean prepareForExit() throws IOException;
	}

	public static synchronized BootstrapRuntime bootstrap() throws IOException {
		BootstrapApp.init();
		AbstractBootstrapRuntime.INSTANCE = new BootstrapRuntime(BootstrapApp.loadConfiguration(),
				new ApplicationInventory(),
				new RemoteUpdateService(),
				new JarApplicationLauncher(),
				BootstrapApp.ENABLE_UPDATE);
		return (BootstrapRuntime) AbstractBootstrapRuntime.INSTANCE;
	}

	public static synchronized BootstrapRuntime getInstance() {
		return (BootstrapRuntime) AbstractBootstrapRuntime.INSTANCE;
	}

	public static boolean isActive() {
		return AbstractBootstrapRuntime.INSTANCE != null;
	}

	private final BootstrapConfiguration configuration;
	private final ApplicationInventory inventory;

	private final RemoteUpdateService remoteUpdateService;

	private final JarApplicationLauncher applicationLauncher;

	private final boolean automaticUpdatesEnabled;

	private InstalledApplication currentApplication;

	private BootstrapRuntime(
			final BootstrapConfiguration configuration,
			final ApplicationInventory inventory,
			final RemoteUpdateService remoteUpdateService,
			final JarApplicationLauncher applicationLauncher,
			final boolean automaticUpdatesEnabled) {
		this.configuration = configuration;
		this.inventory = inventory;
		this.remoteUpdateService = remoteUpdateService;
		this.applicationLauncher = applicationLauncher;
		this.automaticUpdatesEnabled = automaticUpdatesEnabled;
	}

	@Override
	public AvailableUpdate checkForUpdates() throws IOException {
		try {
			final String currentVersion = this.currentApplication == null ? null : this.currentApplication.version();
			return this.remoteUpdateService.findLatest(this.configuration.getUpdateChannel(), currentVersion);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while checking for updates.", ex);
		}
	}

	@Override
	public String getCurrentApplicationVersion() {
		return this.currentApplication == null ? null : this.currentApplication.version();
	}

	@Override
	public UpdateChannel getSelectedChannel() {
		return this.configuration.getUpdateChannel();
	}

	public boolean installUpdateAndExit(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
			throws IOException {
		if (update == null || !update.isUpdateAvailable()) {
			JOptionPane.showMessageDialog(parentComponent,
					"You are already using the latest version for the selected channel.",
					"No updates available",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		final int choice = JOptionPane.showConfirmDialog(parentComponent,
				"Install version " + update.latestVersion() + " from the " + update.channel().displayName().toLowerCase()
						+ " channel and close the application?",
				"Install update",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.YES_OPTION || preparation != null && !preparation.prepareForExit()) {
			return false;
		}

		final BootstrapLoadingFrame loadingFrame = new BootstrapLoadingFrame();
		loadingFrame.setVisible(true);
		try {
			this.currentApplication = this.inventory.install(update, loadingFrame::update);
		} finally {
			loadingFrame.dispose();
		}

		System.exit(0);
		return true;
	}

	@Override
	public boolean isAutoCheckUpdates() {
		return this.configuration.isAutoCheckUpdates();
	}

	@Override
	public boolean isAutomaticUpdateChecksEnabledByProperty() {
		return this.automaticUpdatesEnabled;
	}

	public void launch() throws Exception {
		final BootstrapLoadingFrame loadingFrame = new BootstrapLoadingFrame();
		loadingFrame.setVisible(true);
		try {
			loadingFrame.update("Checking installed application...", 0, 0);
			this.currentApplication = this.inventory.findLatestInstalled().orElse(null);
			if (this.currentApplication == null) {
				final AvailableUpdate bootstrapInstall = this.requireInstallableUpdate(this.configuration.getUpdateChannel(), null);
				loadingFrame.update("Installing " + bootstrapInstall.latestVersion() + "...", 0, 1);
				this.currentApplication = this.inventory.install(bootstrapInstall, loadingFrame::update);
			} else if (this.automaticUpdatesEnabled && this.configuration.isAutoCheckUpdates()) {
				try {
					final AvailableUpdate update = this.remoteUpdateService.findLatest(this.configuration.getUpdateChannel(),
							this.currentApplication.version());
					if (update.isUpdateAvailable()) {
						loadingFrame.update("Updating to " + update.latestVersion() + "...", 0, 1);
						this.currentApplication = this.inventory.install(update, loadingFrame::update);
					}
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			loadingFrame.dispose();
		}
		this.applicationLauncher.launch(this.currentApplication);
	}

	private AvailableUpdate requireInstallableUpdate(final UpdateChannel channel, final String currentVersion) throws IOException {
		try {
			final AvailableUpdate update = this.remoteUpdateService.findLatest(channel, currentVersion);
			if (update.downloadUri() == null) {
				throw new IOException(
						"No downloadable application is configured for the " + channel.displayName().toLowerCase() + " channel.");
			}
			return update;
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while checking for updates.", ex);
		}
	}

	@Override
	public void setAutoCheckUpdates(final boolean enabled) {
		this.configuration.setAutoCheckUpdates(enabled);
		BootstrapApp.saveConfiguration(this.configuration);
	}

	@Override
	public void setSelectedChannel(final UpdateChannel updateChannel) {
		this.configuration.setUpdateChannel(updateChannel);
		BootstrapApp.saveConfiguration(this.configuration);
	}
}
