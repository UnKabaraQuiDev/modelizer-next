package lu.kbra.modelizer_next.bootstrap;

public enum UpdateChannel {

	RELEASE,
	SNAPSHOT,
	NIGHTLY;

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

}
