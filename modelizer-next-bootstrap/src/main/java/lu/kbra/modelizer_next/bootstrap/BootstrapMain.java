package lu.kbra.modelizer_next.bootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import lu.kbra.modelizer_next.common.FileOpenBridge;
import lu.kbra.modelizer_next.common.SystemThemeDetector;

public final class BootstrapMain {

	public static void applyConfiguredLookAndFeel() {
		try {
			if (SystemThemeDetector.isDark()) {
				FlatDarkLaf.setup();
			} else {
				FlatLightLaf.setup();
			}
		} catch (final Exception ignored) {
			ignored.printStackTrace();
		}
	}

	public static void main(String[] args) {
		for (final String arg : args) {
			if (arg.startsWith("-D")) {
				final String withoutPrefix = arg.substring(2);
				final int eq = withoutPrefix.indexOf('=');

				if (eq > 0) {
					final String key = withoutPrefix.substring(0, eq);
					final String value = withoutPrefix.substring(eq + 1);
					System.setProperty(key, value);
				} else {
					System.setProperty(withoutPrefix, "true");
				}
			}
		}

		args = Arrays.stream(args).filter(a -> !a.startsWith("-D")).toArray(String[]::new);

		try {
			BootstrapMain.applyConfiguredLookAndFeel();
			FileOpenBridge.installFileHandler();
			final BootstrapRuntime runtime = BootstrapRuntime.bootstrap();
			runtime.launch(args, FileOpenBridge.TO_BE_OPENED);
		} catch (final Exception ex) {
			ex.printStackTrace();
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null,
						"Failed to start Modelizer Next:\n" + ex.getMessage(),
						"Bootstrap error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			});
		}
	}

	public static void restartSameCommand() throws Exception {
		final ProcessHandle.Info info = ProcessHandle.current().info();

		final String command = info.command().orElseThrow(() -> new IllegalStateException("Could not read current Java command"));

		final String[] arguments = info.arguments().orElseThrow(() -> new IllegalStateException("Could not read current Java arguments"));

		final List<String> restartCommand = new ArrayList<>();
		restartCommand.add(command);
		restartCommand.addAll(Arrays.asList(arguments));

		new ProcessBuilder(restartCommand).directory(new File(System.getProperty("user.dir"))).inheritIO().start();

		System.exit(0);
	}

	private BootstrapMain() {
	}
}
