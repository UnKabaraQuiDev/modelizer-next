package lu.kbra.modelizer_next.bootstrap;

import java.net.URI;

import lu.kbra.modelizer_next.common.ChannelComparator;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public record AvailableUpdate(UpdateChannel channel, ParsedVersion currentVersion, ParsedVersion latestVersion, String notes,
		URI downloadUri, URI releasePageUri) {

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
