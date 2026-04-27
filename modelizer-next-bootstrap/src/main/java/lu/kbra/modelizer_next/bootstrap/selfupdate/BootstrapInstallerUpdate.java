package lu.kbra.modelizer_next.bootstrap.selfupdate;

import java.net.URI;

import lu.kbra.modelizer_next.common.Platform;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public record BootstrapInstallerUpdate(ParsedVersion currentVersion, ParsedVersion latestVersion, URI installerUri, URI releasePageUri,
		Platform platform) {

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