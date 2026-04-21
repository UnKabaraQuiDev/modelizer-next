package lu.kbra.modelizer_next.bootstrap;

import java.net.URI;

import lu.kbra.modelizer_next.common.VersionComparator;

public record AvailableUpdate(UpdateChannel channel, String currentVersion, String latestVersion, String notes, URI downloadUri,
		URI releasePageUri) {

	public boolean isUpdateAvailable() {
		if (this.latestVersion == null || this.latestVersion.isBlank() || this.downloadUri == null) {
			return false;
		}
		if (this.currentVersion == null || this.currentVersion.isBlank()) {
			return true;
		}
		return VersionComparator.COMPARATOR.compare(this.latestVersion, this.currentVersion) > 0;
	}

}
