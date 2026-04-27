package lu.kbra.modelizer_next.bootstrap;

import java.net.URI;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

record BootstrapInstallerUpdate(ParsedVersion currentVersion, ParsedVersion latestVersion, URI installerUri, URI releasePageUri,
		BootstrapInstallerUpdate.Platform platform) {

	enum Platform {
		WINDOWS("Windows", ".exe", true),
		LINUX("Debian/Linux", ".deb", true),
		MACOS("macOS", ".dmg", false),
		UNSUPPORTED("Unsupported", "", false);

		private final String displayName;
		private final String extension;
		private final boolean adminRightsExpected;

		Platform(final String displayName, final String extension, final boolean adminRightsExpected) {
			this.displayName = displayName;
			this.extension = extension;
			this.adminRightsExpected = adminRightsExpected;
		}

		String displayName() {
			return this.displayName;
		}

		String extension() {
			return this.extension;
		}

		boolean adminRightsExpected() {
			return this.adminRightsExpected;
		}
	}

	boolean isUpdateAvailable() {
		return true;
//		if (this.latestVersion == null || this.installerUri == null) {
//			return false;
//		}
//		if (this.currentVersion == null) {
//			return true;
//		}
//		return lu.kbra.modelizer_next.common.VersionComparator.PARSED_COMPARATOR.compare(this.latestVersion, this.currentVersion) > 0;
	}

}