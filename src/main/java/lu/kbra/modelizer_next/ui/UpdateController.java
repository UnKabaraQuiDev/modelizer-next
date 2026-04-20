package lu.kbra.modelizer_next.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import lu.kbra.modelizer_next.AppConfig;
import lu.kbra.modelizer_next.update.UpdateCheckResult;
import lu.kbra.modelizer_next.update.UpdateInstallerLauncher;
import lu.kbra.modelizer_next.update.UpdateService;

final class UpdateController {

	private final JFrame owner;
	private final AppConfig appConfig;
	private final UpdateService updateService;
	private final CheckedAction beforeInstallAction;

	UpdateController(final JFrame owner, final AppConfig appConfig, final CheckedAction beforeInstallAction) {
		this.owner = owner;
		this.appConfig = appConfig;
		this.beforeInstallAction = beforeInstallAction;
		this.updateService = new UpdateService();
	}

	void checkForUpdatesSilently() {
		if (this.appConfig == null || !this.appConfig.isAutoCheckUpdates()) {
			return;
		}
		this.checkForUpdates(false);
	}

	void checkForUpdatesManually() {
		this.checkForUpdates(true);
	}

	private void checkForUpdates(final boolean userInitiated) {
		new SwingWorker<UpdateCheckResult, Void>() {
			@Override
			protected UpdateCheckResult doInBackground() throws Exception {
				return UpdateController.this.updateService.checkForUpdates();
			}

			@Override
			protected void done() {
				try {
					final UpdateCheckResult result = this.get();
					UpdateController.this.handleUpdateCheckResult(result, userInitiated);
				} catch (final Exception ex) {
					if (userInitiated) {
						final Throwable cause = ex.getCause() == null ? ex : ex.getCause();
						JOptionPane.showMessageDialog(UpdateController.this.owner,
								"Failed to check for updates:\n" + cause.getMessage(),
								"Update check failed",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}.execute();
	}

	private void handleUpdateCheckResult(final UpdateCheckResult result, final boolean userInitiated) {
		if (result == null) {
			return;
		}

		if (!result.updateAvailable()) {
			if (userInitiated) {
				JOptionPane.showMessageDialog(this.owner,
						"You are already using the latest version (" + result.currentVersion() + ").",
						"No updates available",
						JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}

		if (result.latestVersion().equals(this.appConfig.getSkippedUpdateVersion()) && !userInitiated) {
			return;
		}

		final StringBuilder message = new StringBuilder();
		message.append("A new version is available.\n\n");
		message.append("Current version: ").append(result.currentVersion()).append("\n");
		message.append("Latest version: ").append(result.latestVersion()).append("\n");
		if (result.notes() != null && !result.notes().isBlank()) {
			message.append("\n").append(result.notes()).append("\n");
		}
		message.append("\nThe installer package will be downloaded from GitHub Releases.");

		final Object[] options = userInitiated
				? new Object[] { "Download and install", "Later" }
				: new Object[] { "Download and install", "Skip this version", "Later" };

		final int choice = JOptionPane.showOptionDialog(this.owner,
				message.toString(),
				"Update available",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0]);

		if (choice == 0) {
			this.downloadAndInstall(result);
			return;
		}

		if (!userInitiated && choice == 1) {
			this.appConfig.setSkippedUpdateVersion(result.latestVersion());
			lu.kbra.modelizer_next.App.saveConfig(this.appConfig);
		}
	}

	private void downloadAndInstall(final UpdateCheckResult result) {
		new SwingWorker<Path, Void>() {
			@Override
			protected Path doInBackground() throws Exception {
				return UpdateController.this.updateService.downloadUpdate(result);
			}

			@Override
			protected void done() {
				try {
					final Path downloadedFile = this.get();
					if (!UpdateController.this.beforeInstallAction.run()) {
						return;
					}

					UpdateInstallerLauncher.launch(downloadedFile);
					System.exit(0);
				} catch (final Exception ex) {
					final Throwable cause = ex.getCause() == null ? ex : ex.getCause();
					final String message = cause instanceof final UncheckedIOException unchecked ? unchecked.getCause().getMessage()
							: cause.getMessage();
					JOptionPane.showMessageDialog(UpdateController.this.owner,
							"Failed to download or launch the update:\n" + message,
							"Update failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	@FunctionalInterface
	interface CheckedAction {
		boolean run() throws IOException;
	}
}
