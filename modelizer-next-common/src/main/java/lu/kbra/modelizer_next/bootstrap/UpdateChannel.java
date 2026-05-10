package lu.kbra.modelizer_next.bootstrap;

/**
 * Supported release channels for updates.
 */
public enum UpdateChannel {

	RELEASE,
	SNAPSHOT,
	NIGHTLY;

	public static final int CHANNEL_NIGHTLY = 1;
	public static final int CHANNEL_SNAPSHOT = 2;
	public static final int CHANNEL_RELEASE = 3;

	/**
	 * Returns the human-readable display name.
	 * @return the display name result
	 */
	public String displayName() {
		return switch (this) {
		case RELEASE -> "Release";
		case SNAPSHOT -> "Snapshot";
		case NIGHTLY -> "Nightly";
		};
	}

	/**
	 * Returns the string key used in update manifests.
	 * @return the manifest key result
	 */
	public String manifestKey() {
		return this.name().toLowerCase();
	}

	/**
	 * Returns the update channel with the supplied numeric id.
	 * @param channelRank numeric channel rank value
	 * @return the by ID result
	 */
	public static UpdateChannel byId(int channelRank) {
		return switch (channelRank) {
		case CHANNEL_NIGHTLY -> NIGHTLY;
		case CHANNEL_SNAPSHOT -> SNAPSHOT;
		case CHANNEL_RELEASE -> RELEASE;
		default -> null;
		};
	}

}
