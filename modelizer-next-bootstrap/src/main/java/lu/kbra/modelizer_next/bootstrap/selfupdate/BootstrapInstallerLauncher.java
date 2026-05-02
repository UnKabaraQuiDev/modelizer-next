package lu.kbra.modelizer_next.bootstrap.selfupdate;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import lu.kbra.modelizer_next.bootstrap.BootstrapMain;
import lu.kbra.modelizer_next.common.Platform;

public final class BootstrapInstallerLauncher {

	public static boolean promptAndStartInstaller(final BootstrapInstallerUpdate update, final Path installerPath) throws IOException {
		final String adminHint = update.platform().adminRightsExpected()
				? "\n\nYour system will ask for administrator rights to install it."
				: "\n\nThe installer may ask for administrator rights when you copy the app into Applications.";
		final int choice = JOptionPane.showConfirmDialog(null,
				"Modelizer Next could not start because the bootstrap launcher is outdated.\n\n"
						+ "A newer bootstrap launcher is available: " + update.latestVersion() + "\n" + "Downloaded installer: "
						+ installerPath.getFileName() + adminHint + "\n\n" + "Install it now?",
				"Install bootstrap update",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return false;
		}

		BootstrapInstallerLauncher.startInstaller(update.platform(), installerPath);
		return true;
	}

	private static void startInstaller(final Platform platform, final Path installerPath) throws IOException {
		final String installer = installerPath.toAbsolutePath().toString();

		final ProcessBuilder process = switch (platform) {
		case WINDOWS -> new ProcessBuilder("cmd",
				"/c",
				"start",
				"\"\"",
				"powershell",
				"-NoProfile",
				"-ExecutionPolicy",
				"Bypass",
				"-Command",
				"Start-Process -FilePath " + BootstrapInstallerLauncher.quotePowerShell(installer) + " -Verb RunAs");

		case LINUX -> new ProcessBuilder("setsid", "sh", "-c", "pkexec dpkg -i " + BootstrapInstallerLauncher.quoteShell(installer));

		case MACOS -> new ProcessBuilder("sh", "-c", "open " + BootstrapInstallerLauncher.quoteShell(installer));

		case UNSUPPORTED -> {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(installerPath.toFile());
				yield null;
			} else {
				throw new IOException("Cannot open installer on this platform: " + installerPath);
			}
		}
		};

		if (process != null) {
			process.inheritIO();
			try {
				process.start().waitFor();
				BootstrapMain.restartSameCommand();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String quoteShell(final String value) {
		return "'" + value.replace("'", "'\"'\"'") + "'";
	}

	private static String quotePowerShell(final String value) {
		return "'" + value.replace("'", "''") + "'";
	}

}
