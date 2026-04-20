package lu.kbra.modelizer_next.update;

import java.net.URI;

public record UpdateCheckResult(boolean updateAvailable, String currentVersion, String latestVersion, String assetName, URI downloadUri,
		URI releasePageUri, String notes) {

	public static UpdateCheckResult upToDate(final String currentVersion, final URI releasePageUri) {
		return new UpdateCheckResult(false, currentVersion, currentVersion, null, null, releasePageUri, null);
	}
}
