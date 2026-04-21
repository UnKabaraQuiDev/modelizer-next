package lu.kbra.modelizer_next.bootstrap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

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

	public static void main(final String[] args) {
		try {
			BootstrapMain.applyConfiguredLookAndFeel();
			final BootstrapRuntime runtime = BootstrapRuntime.bootstrap();
			runtime.launch();
		} catch (final Exception ex) {
			ex.printStackTrace();
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null, "Failed to start Modelizer Next:\n" + ex.getMessage(),
						"Bootstrap error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			});
		}
	}

	private BootstrapMain() {
	}
}