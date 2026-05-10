package lu.kbra.modelizer_next.common;

import java.util.Locale;

/**
 * Operating system and package platform values used by update manifests.
 */
public enum Platform {

	WINDOWS("Windows", "windows", ".exe", true),
	LINUX("Debian/Linux", "linux", ".deb", true),
	MACOS("macOS", "macos", ".dmg", false),
	UNSUPPORTED("Unsupported", "unsupported", "", false);

	private static final Platform CURRENT = detect(System.getProperty("os.name", ""));

	private final String displayName;
	private final String manifestKey;
	private final String installerExtension;
	private final boolean adminRightsExpected;

	/**
	 * Creates a platform instance.
	 *
	 * @param displayName         name value to use
	 * @param manifestKey         text value for manifest key
	 * @param installerExtension  text value for installer extension
	 * @param adminRightsExpected whether admin rights expected is enabled
	 */
	Platform(final String displayName, final String manifestKey, final String installerExtension, final boolean adminRightsExpected) {
		this.displayName = displayName;
		this.manifestKey = manifestKey;
		this.installerExtension = installerExtension;
		this.adminRightsExpected = adminRightsExpected;
	}

	/**
	 * Returns the value for the requested panel or key.
	 *
	 * @return the value
	 */
	public static Platform get() {
		return CURRENT;
	}

	/**
	 * Detects the current platform.
	 *
	 * @param osName name value to use
	 * @return the detect result
	 */
	public static Platform detect(final String osName) {
		final String os = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			return WINDOWS;
		}
		if (os.contains("mac") || os.contains("darwin")) {
			return MACOS;
		}
		if (os.contains("linux")) {
			return LINUX;
		}
		return UNSUPPORTED;
	}

	/**
	 * Checks whether supported is enabled or applies.
	 *
	 * @return {@code true} if supported is enabled or applies; otherwise {@code false}
	 */
	public boolean isSupported() {
		return this != UNSUPPORTED;
	}

	/**
	 * Returns the user-facing display name.
	 *
	 * @return the display name result
	 */
	public String displayName() {
		return this.displayName;
	}

	/**
	 * Returns the string key used in update manifests.
	 *
	 * @return the manifest key result
	 */
	public String manifestKey() {
		return this.manifestKey;
	}

	/**
	 * Installs the er extension.
	 *
	 * @return the installer extension result
	 */
	public String installerExtension() {
		return this.installerExtension;
	}

	/**
	 * Checks whether installer actions usually require administrator rights on this platform.
	 *
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public boolean adminRightsExpected() {
		return this.adminRightsExpected;
	}

}
