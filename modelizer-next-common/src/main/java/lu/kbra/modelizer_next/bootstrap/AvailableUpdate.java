package lu.kbra.modelizer_next.bootstrap;

import java.net.URI;

import lu.kbra.modelizer_next.common.ChannelComparator;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Update check result describing the current version, latest version, notes, and download location.
 *
 * @param channel        update channel to query
 * @param currentVersion currently installed version
 * @param latestVersion  latest version value used by the operation
 * @param notes          text value for notes
 * @param downloadUri    URI of the file to download
 * @param releasePageUri URI to use
 */
public record AvailableUpdate(UpdateChannel channel, ParsedVersion currentVersion, ParsedVersion latestVersion, String notes,
		URI downloadUri, URI releasePageUri) {

	/**
	 * Checks whether update available is enabled or applies.
	 *
	 * @return {@code true} if update available is enabled or applies; otherwise {@code false}
	 */
	public boolean isUpdateAvailable() {
		if (this.latestVersion == null || this.downloadUri == null) {
			return false;
		}
		if (this.currentVersion == null) {
			return true;
		}
		return ChannelComparator.PARSED_COMPARATOR.compare(this.latestVersion, this.currentVersion) != 0
				|| VersionComparator.PARSED_COMPARATOR.compare(this.latestVersion, this.currentVersion) > 0;
	}

}
