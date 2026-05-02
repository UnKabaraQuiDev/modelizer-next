package lu.kbra.modelizer_next.common;

import java.util.Locale;

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

	Platform(final String displayName, final String manifestKey, final String installerExtension, final boolean adminRightsExpected) {
		this.displayName = displayName;
		this.manifestKey = manifestKey;
		this.installerExtension = installerExtension;
		this.adminRightsExpected = adminRightsExpected;
	}

	public static Platform get() {
		return CURRENT;
	}

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

	public boolean isSupported() {
		return this != UNSUPPORTED;
	}

	public String displayName() {
		return this.displayName;
	}

	public String manifestKey() {
		return this.manifestKey;
	}

	public String installerExtension() {
		return this.installerExtension;
	}

	public boolean adminRightsExpected() {
		return this.adminRightsExpected;
	}

}
