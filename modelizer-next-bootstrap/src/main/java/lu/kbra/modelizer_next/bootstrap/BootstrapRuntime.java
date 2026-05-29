package lu.kbra.modelizer_next.bootstrap;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.bootstrap.config.BootstrapApp;
import lu.kbra.modelizer_next.bootstrap.config.BootstrapConfiguration;
import lu.kbra.modelizer_next.bootstrap.remote.RemoteUpdateService;
import lu.kbra.modelizer_next.bootstrap.selfupdate.BootstrapInstallerLauncher;
import lu.kbra.modelizer_next.bootstrap.selfupdate.BootstrapInstallerUpdate;
import lu.kbra.modelizer_next.bootstrap.subapp.AppLaunchException;
import lu.kbra.modelizer_next.bootstrap.subapp.ApplicationInventory;
import lu.kbra.modelizer_next.bootstrap.subapp.ApplicationUpdateStorage;
import lu.kbra.modelizer_next.bootstrap.subapp.InstalledApplication;
import lu.kbra.modelizer_next.bootstrap.subapp.JarApplicationLauncher;
import lu.kbra.modelizer_next.bootstrap.ui.BootstrapLoadingFrame;
import lu.kbra.modelizer_next.common.Platform;
import lu.kbra.modelizer_next.common.UnsupportedBootstrapVersionException;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;
import lu.kbra.pclib.PCUtils;

/**
 * Runtime coordinator that checks updates, prepares the installed application, and launches it.
 */
public class BootstrapRuntime implements UpdateRuntime {

	@Deprecated
	private static final Pattern VERSION_MINUTES_PATTERN = Pattern.compile("^.+-(RELEASE|SNAPSHOT|NIGHTLY)-(\\d+)$");
	private static final long UPDATE_EPOCH_SECONDS = Instant.parse("2026-01-01T00:00:00Z").getEpochSecond();
	private static final DateTimeFormatter VERSION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(ZoneId.systemDefault());

	/**
	 * Starts the bootstrap sequence.
	 *
	 * @return the bootstrap result
	 * @throws IOException if the operation cannot be completed
	 */
	public static synchronized BootstrapRuntime bootstrap() throws IOException {
		BootstrapApp.init();

		System.out.println(BootstrapApp.NAME + " / " + BootstrapApp.VERSION + " [" + BootstrapApp.DISTRIBUTOR + "]");
		System.out.println("Boostrap dir: " + BootstrapApp.getHomeDirectory());

		final boolean firstLaunch = BootstrapApp.isFirstLaunch();
		final BootstrapConfiguration configuration = BootstrapApp.loadConfiguration();

		final BootstrapRuntime runtime = new BootstrapRuntime(configuration,
				new ApplicationInventory(),
				new RemoteUpdateService(),
				new JarApplicationLauncher(),
				new ApplicationUpdateStorage(),
				BootstrapApp.ENABLE_UPDATE,
				BootstrapApp.FORCE_JAR_NAME);

		if (firstLaunch) {
			runtime.promptForInitialChannelSelection();
			BootstrapApp.saveConfiguration(configuration);
		}

		UpdateRuntimes.install(runtime);
		return runtime;
	}

	/**
	 * Builds a first launch message.
	 *
	 * @param manifest update manifest to inspect
	 * @return the built first launch message
	 */
	private static String buildFirstLaunchMessage(final RemoteUpdateService.UpdateManifest manifest) {
		final StringBuilder builder = new StringBuilder();
		builder.append("Choose the update channel to subscribe to.\n\n");
		builder.append("Latest known versions:\n");
		builder.append("• ")
				.append(BootstrapRuntime.describeChannelOption(UpdateChannel.RELEASE, manifest == null ? null : manifest.release))
				.append('\n');
		builder.append("• ")
				.append(BootstrapRuntime.describeChannelOption(UpdateChannel.SNAPSHOT, manifest == null ? null : manifest.snapshot))
				.append('\n');
		builder.append("• ")
				.append(BootstrapRuntime.describeChannelOption(UpdateChannel.NIGHTLY, manifest == null ? null : manifest.nightly))
				.append('\n');
		return builder.toString();
	}

	/**
	 * Returns a readable description of the selected update channel.
	 *
	 * @param channel update channel to query
	 * @param release release information to inspect
	 * @return the describe channel option result
	 */
	private static String describeChannelOption(final UpdateChannel channel, final RemoteUpdateService.UpdateRelease release) {
		final StringBuilder builder = new StringBuilder(channel.displayName());

		if (release == null || release.version == null) {
			builder.append(" — no published version");
			return builder.toString();
		}

		builder.append(" — latest ").append(release.version);
		final String publishedAt = BootstrapRuntime.extractPublishedAt(release);
		if (publishedAt != null) {
			builder.append(" (").append(publishedAt).append(")");
		}
		return builder.toString();
	}

	/**
	 * Extracts the publication timestamp from the update manifest entry.
	 *
	 * @param release release information to inspect
	 * @return the extract published at result
	 */
	private static String extractPublishedAt(final RemoteUpdateService.UpdateRelease release) {
		if (release == null) {
			return null;
		}

		final String candidate = release.tag != null && !release.tag.isBlank() ? release.tag : release.version.toString();
		if (candidate == null || candidate.isBlank()) {
			return null;
		}

		final Matcher matcher = BootstrapRuntime.VERSION_MINUTES_PATTERN.matcher(candidate.toUpperCase(Locale.ROOT));
		if (!matcher.matches()) {
			return null;
		}

		try {
			final long minutesSinceEpoch = Long.parseLong(matcher.group(2));
			final Instant publishedAt = Instant.ofEpochSecond(BootstrapRuntime.UPDATE_EPOCH_SECONDS + minutesSinceEpoch * 60L);
			return BootstrapRuntime.VERSION_DATE_FORMATTER.format(publishedAt);
		} catch (final NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Returns the instance during bootstrap/update processing.
	 *
	 * @return the instance
	 */
	public static synchronized BootstrapRuntime getInstance() {
		return (BootstrapRuntime) UpdateRuntimes.getInstance();
	}

	/**
	 * Checks whether active is enabled or applies during bootstrap/update processing.
	 *
	 * @return {@code true} if active is enabled or applies; otherwise {@code false}
	 */
	public static boolean isActive() {
		return UpdateRuntimes.isActive();
	}

	private final BootstrapConfiguration configuration;
	private final ApplicationInventory inventory;
	private final RemoteUpdateService remoteUpdateService;
	private final JarApplicationLauncher applicationLauncher;
	private final ApplicationUpdateStorage updateStorage;
	private final boolean automaticUpdatesEnabled;
	private final String forceJarName;

	private InstalledApplication currentApplication;

	/**
	 * Creates a bootstrap runtime instance.
	 *
	 * @param configuration           configuration value used by the operation
	 * @param inventory               inventory value used by the operation
	 * @param remoteUpdateService     remote update service value used by the operation
	 * @param applicationLauncher     application launcher value used by the operation
	 * @param updateStorage           update storage value used by the operation
	 * @param automaticUpdatesEnabled whether automatic updates enabled is enabled
	 * @param forceJarName            name value to use
	 */
	private BootstrapRuntime(
			final BootstrapConfiguration configuration,
			final ApplicationInventory inventory,
			final RemoteUpdateService remoteUpdateService,
			final JarApplicationLauncher applicationLauncher,
			final ApplicationUpdateStorage updateStorage,
			final boolean automaticUpdatesEnabled,
			final String forceJarName) {
		this.configuration = configuration;
		this.inventory = inventory;
		this.remoteUpdateService = remoteUpdateService;
		this.applicationLauncher = applicationLauncher;
		this.updateStorage = updateStorage;
		this.automaticUpdatesEnabled = automaticUpdatesEnabled;
		this.forceJarName = forceJarName;
	}

	/**
	 * Checks the for updates.
	 *
	 * @return the check for updates result
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public AvailableUpdate checkForUpdates() throws IOException {
		try {
			final ParsedVersion currentVersion = this.currentApplication == null ? null : this.currentApplication.version();
			System.out.println("Comparing " + currentVersion + " on " + this.configuration.getUpdateChannel());
			return this.remoteUpdateService.findLatest(this.configuration.getUpdateChannel(), currentVersion);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while checking for updates.", ex);
		}
	}

	/**
	 * Deletes installed update folders that are no longer needed.
	 *
	 * @return the free unused installed updates result
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public long freeUnusedInstalledUpdates() throws IOException {
		return this.updateStorage.freeUnusedUpdates(this.configuration.getUpdateChannel(), this.currentApplication);
	}

	/**
	 * Returns the bootstrap config.
	 *
	 * @return the bootstrap config
	 */
	@Override
	public BootstrapConfig getBootstrapConfig() {
		return BootstrapApp.BOOTSTRAP_CONFIG;
	}

	/**
	 * Returns the bootstrap JSON.
	 *
	 * @return the bootstrap JSON
	 */
	@Override
	public JsonNode getBootstrapJson() {
		return BootstrapApp.JSON;
	}

	/**
	 * Returns the current application version.
	 *
	 * @return the current application version
	 */
	@Override
	public ParsedVersion getCurrentApplicationVersion() {
		return this.currentApplication == null ? null : this.currentApplication.version();
	}

	/**
	 * Returns the force jar name during bootstrap/update processing.
	 *
	 * @return the force jar name
	 */
	public String getForceJarName() {
		return this.forceJarName;
	}

	/**
	 * Returns the installed updates directory.
	 *
	 * @return the installed updates directory
	 */
	@Override
	public Path getInstalledUpdatesDirectory() {
		return this.updateStorage.getUpdatesDirectory();
	}

	/**
	 * Returns the installed updates disk usage bytes.
	 *
	 * @return the installed updates disk usage bytes
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public long getInstalledUpdatesDiskUsageBytes() throws IOException {
		return this.updateStorage.calculateDiskUsageBytes();
	}

	/**
	 * Returns the installed updates file count.
	 *
	 * @return the installed updates file count
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public int getInstalledUpdatesFileCount() throws IOException {
		return this.updateStorage.countFiles();
	}

	/**
	 * Returns the selected channel during bootstrap/update processing.
	 *
	 * @return the selected channel
	 */
	@Override
	public UpdateChannel getSelectedChannel() {
		return this.configuration.getUpdateChannel();
	}

	/**
	 * Handles the outdated bootstrap launcher.
	 *
	 * @param launchException launch exception value used by the operation
	 * @param forced          whether forced is enabled
	 * @throws Exception if the operation cannot be completed
	 */
	private void handleOutdatedBootstrapLauncher(final AppLaunchException launchException, final boolean forced) throws Exception {
		final ParsedVersion currentBootstrapVersion = VersionComparator.parse(BootstrapApp.VERSION);
		final BootstrapLoadingFrame loadingFrame = new BootstrapLoadingFrame();
		loadingFrame.setVisible(true);
		try {
			loadingFrame.update("Checking bootstrap launcher update...", 0, 0);
			final BootstrapInstallerUpdate update = this.remoteUpdateService
					.findLatestBootstrapInstaller(this.configuration.getUpdateChannel(), currentBootstrapVersion);
			if (!update.isUpdateAvailable() && !forced) {
				throw new AppLaunchException("The application needs a newer bootstrap launcher, but no bootstrap update is available.",
						launchException);
			}
			if (update.platform() == Platform.UNSUPPORTED) {
				throw new AppLaunchException("The application needs a newer bootstrap launcher, but this platform is not supported.",
						launchException);
			}

			final String safeVersion = update.latestVersion().toString().replaceAll("[^A-Za-z0-9._-]", "_");
			final Path installerPath = BootstrapApp.getTempDirectory()
					.toPath()
					.resolve("modelizer-next-bootstrap-" + safeVersion + update.platform().installerExtension());
			this.remoteUpdateService
					.download(update.installerUri(), installerPath, update.latestVersion().toString(), loadingFrame::update);
			loadingFrame.dispose();

			if (BootstrapInstallerLauncher.promptAndStartInstaller(update, installerPath)) {
				System.exit(0);
			}
			throw new AppLaunchException(
					"The application needs a newer bootstrap launcher. Install the downloaded installer to continue: " + installerPath,
					launchException);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AppLaunchException("Interrupted while checking for a bootstrap launcher update.", ex);
		} finally {
			loadingFrame.dispose();
		}
	}

	/**
	 * Installs the update and restart.
	 *
	 * @param parentComponent parent component value used by the operation
	 * @param update          update metadata to download or install
	 * @param preparation     preparation value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public boolean
			installUpdateAndRestart(final Component parentComponent, final AvailableUpdate update, final UpdatePreparation preparation)
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

		try {
			BootstrapMain.restartSameCommand();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parentComponent,
					"Couldn't restart process, you may do it manually.\n" + PCUtils.toString(e),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return true;
	}

	/**
	 * Checks whether auto check updates is enabled or applies.
	 *
	 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
	 */
	@Override
	public boolean isAutoCheckUpdates() {
		return this.configuration.isAutoCheckUpdates();
	}

	/**
	 * Checks whether automatic update checks enabled by property is enabled or applies.
	 *
	 * @return {@code true} if automatic update checks enabled by property is enabled or applies;
	 *         otherwise {@code
	 *         false}
	 */
	@Override
	public boolean isAutomaticUpdateChecksEnabledByProperty() {
		return this.automaticUpdatesEnabled;
	}

	/**
	 * Checks whether available is enabled or applies during bootstrap/update processing.
	 *
	 * @return {@code true} if available is enabled or applies; otherwise {@code false}
	 */
	@Override
	public boolean isAvailable() {
		return true;
	}

	/**
	 * Launches the installed application.
	 *
	 * @param args       command-line arguments supplied by the launcher
	 * @param toBeOpened to be opened value used by the operation
	 * @throws Exception if the operation cannot be completed
	 */
	public void launch(final String[] args, Queue<File> toBeOpened) throws Exception {
		final BootstrapLoadingFrame loadingFrame = new BootstrapLoadingFrame();
		loadingFrame.setVisible(true);
		try {
			loadingFrame.update("Checking installed application...", 0, 0);

			if (this.getForceJarName() != null
					&& Files.exists(BootstrapApp.getApplicationsDirectory().toPath().resolve(this.getForceJarName()))) {
				final Path path = BootstrapApp.getApplicationsDirectory().toPath().resolve(this.getForceJarName());
				this.currentApplication = this.inventory.readInstalledApplication(path)
						.orElseThrow(
								() -> new IllegalArgumentException("File: '" + this.getForceJarName() + "' not found, resolved: " + path));
			} else {
				this.currentApplication = this.inventory.findLatestInstalled(this.configuration.getUpdateChannel()).orElse(null);
			}

			if (this.currentApplication == null) {
				final AvailableUpdate bootstrapInstall = this.requireInstallableUpdate(this.configuration.getUpdateChannel(), null);
				loadingFrame.update("Installing " + bootstrapInstall.latestVersion() + "...", 0, 1);
				this.currentApplication = this.inventory.install(bootstrapInstall, loadingFrame::update);
			} else if (this.automaticUpdatesEnabled && this.configuration.isAutoCheckUpdates()) {
				try {
					this.promptForBootstrapReinstallIfRequired();
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

		try {
			if (BootstrapApp.FORCE_BOOTSTRAP_UPDATE) {
				this.handleOutdatedBootstrapLauncher(null, true);
			}
			this.applicationLauncher.launch(args, toBeOpened, this.currentApplication);
		} catch (final AppLaunchException ex) {
			if (!this.needsBootstrappUpdate(ex)) {
				throw ex;
			}
			this.handleOutdatedBootstrapLauncher(ex, false);
		}
	}

	/**
	 * Checks whether the bootstrapper needs an update.
	 *
	 * @param throwable throwable value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	private boolean needsBootstrappUpdate(final Throwable throwable) {
		for (Throwable current = throwable; current != null; current = current.getCause()) {
			if (current instanceof ClassNotFoundException || current instanceof NoClassDefFoundError
					|| current instanceof UnsupportedBootstrapVersionException) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prompts the user to reinstall the bootstrapper when a newer bootstrap installer is available.
	 *
	 * @throws Exception if the operation cannot be completed
	 */
	private void promptForBootstrapReinstallIfRequired() throws Exception {
		final RemoteUpdateService.UpdateManifest manifest = this.remoteUpdateService.fetchManifest();
		if (manifest.bootstrapVersion == null) {
			return;
		}
		final ParsedVersion currentBootstrapVersion = VersionComparator.parse(BootstrapApp.VERSION);
		if (VersionComparator.PARSED_COMPARATOR.compare(manifest.bootstrapVersion, currentBootstrapVersion) <= 0) {
			return;
		}

		final int choice = JOptionPane.showConfirmDialog(null,
				"This app version needs a newer bootstrap install.\n\n" + "Required bootstrap version: " + manifest.bootstrapVersion + "\n"
						+ "Current bootstrap version: " + currentBootstrapVersion + "\n\n" + "Update the bootstrap now?",
				"Bootstrap update required",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			this.handleOutdatedBootstrapLauncher(null, false);
		}
	}

	/**
	 * Prompts the user for for initial channel selection.
	 *
	 * @throws IOException if the operation cannot be completed
	 */
	private void promptForInitialChannelSelection() throws IOException {
		RemoteUpdateService.UpdateManifest manifest = null;
		try {
			manifest = this.remoteUpdateService.fetchManifest();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while fetching available channels.", ex);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}

		final Object[] options = {
				UpdateChannel.RELEASE.displayName(),
				UpdateChannel.SNAPSHOT.displayName(),
				UpdateChannel.NIGHTLY.displayName() };

		final int choice = JOptionPane.showOptionDialog(null,
				BootstrapRuntime.buildFirstLaunchMessage(manifest),
				"Choose update channel",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

		final UpdateChannel selectedChannel = switch (choice) {
		case 0 -> UpdateChannel.RELEASE;
		case 1 -> UpdateChannel.SNAPSHOT;
		case 2 -> UpdateChannel.NIGHTLY;
		default -> null;
		};

		if (selectedChannel == null) {
			throw new IOException("Initial setup was cancelled.");
		}

		this.configuration.setUpdateChannel(selectedChannel);
	}

	/**
	 * Reads and validates the required installable update.
	 *
	 * @param channel        update channel to query
	 * @param currentVersion currently installed version
	 * @return the require installable update result
	 * @throws IOException if the operation cannot be completed
	 */
	private AvailableUpdate requireInstallableUpdate(final UpdateChannel channel, final ParsedVersion currentVersion) throws IOException {
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

	/**
	 * Sets the auto check updates.
	 *
	 * @param enabled whether enabled is enabled
	 */
	@Override
	public void setAutoCheckUpdates(final boolean enabled) {
		this.configuration.setAutoCheckUpdates(enabled);
		BootstrapApp.saveConfiguration(this.configuration);
	}

	/**
	 * Sets the selected channel during bootstrap/update processing.
	 *
	 * @param updateChannel update channel value used by the operation
	 */
	@Override
	public void setSelectedChannel(final UpdateChannel updateChannel) {
		this.configuration.setUpdateChannel(updateChannel);
		BootstrapApp.saveConfiguration(this.configuration);
	}

}
