package lu.kbra.modelizer_next.ui.export;

public enum ViewExportFormat {

	PNG("png", "PNG"),
	SVG("svg", "SVG");

	private final String extension;
	private final String displayName;

	ViewExportFormat(final String extension, final String displayName) {
		this.extension = extension;
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getExtension() {
		return this.extension;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

}
