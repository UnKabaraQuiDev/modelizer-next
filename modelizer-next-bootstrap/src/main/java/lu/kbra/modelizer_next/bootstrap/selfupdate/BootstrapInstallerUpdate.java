package lu.kbra.modelizer_next.bootstrap.selfupdate;

import java.net.URI;

import lu.kbra.modelizer_next.common.Platform;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Description of the newest bootstrap installer available for the current platform.
 *
 * @param currentVersion currently installed version
 * @param latestVersion  latest version value used by the operation
 * @param installerUri   URI to use
 * @param releasePageUri URI to use
 * @param platform       target platform to match
 */
public record BootstrapInstallerUpdate(
		ParsedVersion currentVersion,
		ParsedVersion latestVersion,
		URI installerUri,
		URI releasePageUri,
		Platform platform) {

	/**
	 * Checks whether update available is enabled or applies.
	 *
	 * @return {@code true} if update available is enabled or applies; otherwise {@code false}
	 */
	public boolean isUpdateAvailable() {
		if (this.latestVersion == null || this.installerUri == null) {
			return false;
		}
		if (this.currentVersion == null) {
			return true;
		}
		return VersionComparator.PARSED_COMPARATOR.compare(this.latestVersion, this.currentVersion) > 0;
	}

}
