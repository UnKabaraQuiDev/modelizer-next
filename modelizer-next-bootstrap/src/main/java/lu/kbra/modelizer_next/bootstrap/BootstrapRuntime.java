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
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public class BootstrapRuntime implements UpdateRuntime {

	@Deprecated
	private static final Pattern VERSION_MINUTES_PATTERN = Pattern.compile("^.+-(RELEASE|SNAPSHOT|NIGHTLY)-(\\d+)$");
	private static final long UPDATE_EPOCH_SECONDS = Instant.parse("2026-01-01T00:00:00Z").getEpochSecond();
	private static final DateTimeFormatter VERSION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(ZoneId.systemDefault());

	public static synchronized BootstrapRuntime bootstrap() throws IOException {
		BootstrapApp.init();

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

	public static synchronized BootstrapRuntime getInstance() {
		return (BootstrapRuntime) UpdateRuntimes.getInstance();
	}

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

	@Override
	public AvailableUpdate checkForUpdates() throws IOException {
		try {
			final ParsedVersion currentVersion = this.currentApplication == null ? null : this.currentApplication.version();
			System.out.println("Comparing " + currentVersion + " on " + configuration.getUpdateChannel());
			return this.remoteUpdateService.findLatest(this.configuration.getUpdateChannel(), currentVersion);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while checking for updates.", ex);
		}
	}

	@Override
	public long getInstalledUpdatesDiskUsageBytes() throws IOException {
		return this.updateStorage.calculateDiskUsageBytes();
	}

	@Override
	public int getInstalledUpdatesFileCount() throws IOException {
		return this.updateStorage.countFiles();
	}

	@Override
	public Path getInstalledUpdatesDirectory() {
		return this.updateStorage.getUpdatesDirectory();
	}

	@Override
	public long freeUnusedInstalledUpdates() throws IOException {
		return this.updateStorage.freeUnusedUpdates(this.configuration.getUpdateChannel(), this.currentApplication);
	}

	@Override
	public BootstrapConfig getBootstrapConfig() {
		return BootstrapApp.BOOTSTRAP_CONFIG;
	}

	@Override
	public JsonNode getBootstrapJson() {
		return BootstrapApp.JSON;
	}

	@Override
	public ParsedVersion getCurrentApplicationVersion() {
		return this.currentApplication == null ? null : this.currentApplication.version();
	}

	@Override
	public UpdateChannel getSelectedChannel() {
		return this.configuration.getUpdateChannel();
	}

	@Override
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

	@Override
	public boolean isAvailable() {
		return true;
	}

	public String getForceJarName() {
		return forceJarName;
	}

	public void launch(final String[] args, Queue<File> toBeOpened) throws Exception {
		final BootstrapLoadingFrame loadingFrame = new BootstrapLoadingFrame();
		loadingFrame.setVisible(true);
		try {
			loadingFrame.update("Checking installed application...", 0, 0);
			if (getForceJarName() != null && Files.exists(BootstrapApp.getApplicationsDirectory().toPath().resolve(getForceJarName()))) {
				final Path path = BootstrapApp.getApplicationsDirectory().toPath().resolve(getForceJarName());
				this.currentApplication = inventory.readInstalledApplication(path)
						.orElseThrow(() -> new IllegalArgumentException("File: '" + getForceJarName() + "' not found, resolved: " + path));
			} else {
				System.err.println(configuration.getUpdateChannel());
				this.currentApplication = this.inventory.findLatestInstalled(configuration.getUpdateChannel()).orElse(null);
				System.err.println(currentApplication);
			}
			System.out.println("Current version" + (getForceJarName() == null ? "" : " (forced)") + ": "
					+ (currentApplication == null ? "none" : currentApplication.version()));
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
			if (!this.isCausedByClassNotFound(ex)) {
				throw ex;
			}
			this.handleOutdatedBootstrapLauncher(ex, false);
		}
	}

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

	private void handleOutdatedBootstrapLauncher(final AppLaunchException launchException, final boolean forced) throws Exception {
		final ParsedVersion currentBootstrapVersion = lu.kbra.modelizer_next.common.VersionComparator.parse(BootstrapApp.VERSION);
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

			if (new BootstrapInstallerLauncher().promptAndStartInstaller(update, installerPath)) {
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

	private boolean isCausedByClassNotFound(final Throwable throwable) {
		for (Throwable current = throwable; current != null; current = current.getCause()) {
			if (current instanceof ClassNotFoundException || current instanceof NoClassDefFoundError) {
				return true;
			}
		}
		return false;
	}

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
