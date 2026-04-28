package lu.kbra.modelizer_next.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class SystemThemeDetector {

	public static boolean isDark() {
		final String os = System.getProperty("os.name", "").toLowerCase();

		try {
			if (os.contains("win")) {
				return SystemThemeDetector.isWindowsDark();
			}
			if (os.contains("mac")) {
				return SystemThemeDetector.isMacDark();
			}
		} catch (final Exception ignored) {
			// fall through
		}

		return false;
	}

	private static boolean isMacDark() throws Exception {
		final Process process = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			final String line = reader.readLine();
			return line != null && line.toLowerCase().contains("dark");
		}
	}

	private static boolean isWindowsDark() throws Exception {
		final Process process = new ProcessBuilder("reg",
				"query",
				"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
				"/v",
				"AppsUseLightTheme").start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("AppsUseLightTheme")) {
					return line.trim().endsWith("0x0");
				}
			}
		}

		return false;
	}

	private SystemThemeDetector() {
	}

}
