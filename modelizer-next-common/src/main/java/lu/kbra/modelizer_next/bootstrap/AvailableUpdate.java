package lu.kbra.modelizer_next.bootstrap;

import java.net.URI;

public record AvailableUpdate(UpdateChannel channel, String currentVersion, String latestVersion, String notes, URI downloadUri,
		URI releasePageUri) {

	public boolean isUpdateAvailable() {
		return false;
	}

}
