package lu.kbra.modelizer_next.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Detects whether the operating system currently prefers a dark theme.
 */
public final class SystemThemeDetector {

	/**
	 * Checks whether dark is enabled or applies.
	 * @return {@code true} if dark is enabled or applies; otherwise {@code false}
	 */
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

	/**
	 * Checks whether mac dark is enabled or applies.
	 * @return {@code true} if mac dark is enabled or applies; otherwise {@code false}
	 * @throws Exception if the operation cannot be completed
	 */
	private static boolean isMacDark() throws Exception {
		final Process process = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			final String line = reader.readLine();
			return line != null && line.toLowerCase().contains("dark");
		}
	}

	/**
	 * Checks whether windows dark is enabled or applies.
	 * @return {@code true} if windows dark is enabled or applies; otherwise {@code false}
	 * @throws Exception if the operation cannot be completed
	 */
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

	/**
	 * Creates a system theme detector instance.
	 */
	private SystemThemeDetector() {
	}

}
