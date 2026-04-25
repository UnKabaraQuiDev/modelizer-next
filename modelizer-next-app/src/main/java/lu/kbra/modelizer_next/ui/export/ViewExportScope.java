package lu.kbra.modelizer_next.ui.export;

public enum ViewExportScope {

	SELECTION("Selection only"),
	VIEW("Current view only"),
	EVERYTHING("Everything");

	private final String displayName;

	ViewExportScope(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

}
