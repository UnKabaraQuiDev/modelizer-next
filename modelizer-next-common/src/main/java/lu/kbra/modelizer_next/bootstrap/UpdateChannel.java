package lu.kbra.modelizer_next.bootstrap;

public enum UpdateChannel {

	RELEASE,
	SNAPSHOT,
	NIGHTLY;

	public static final int CHANNEL_NIGHTLY = 1;
	public static final int CHANNEL_SNAPSHOT = 2;
	public static final int CHANNEL_RELEASE = 3;

	public String displayName() {
		return switch (this) {
		case RELEASE -> "Release";
		case SNAPSHOT -> "Snapshot";
		case NIGHTLY -> "Nightly";
		};
	}

	public String manifestKey() {
		return this.name().toLowerCase();
	}

	public static UpdateChannel byId(int channelRank) {
		return switch (channelRank) {
		case CHANNEL_NIGHTLY -> NIGHTLY;
		case CHANNEL_SNAPSHOT -> SNAPSHOT;
		case CHANNEL_RELEASE -> RELEASE;
		default -> null;
		};
	}

}
