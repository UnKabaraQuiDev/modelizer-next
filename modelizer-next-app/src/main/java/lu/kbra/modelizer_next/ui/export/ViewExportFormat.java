package lu.kbra.modelizer_next.ui.export;

/**
 * Supported file formats for diagram view export.
 */
public enum ViewExportFormat {

	PNG("png", "PNG"),
	SVG("svg", "SVG");

	private final String extension;
	private final String displayName;

	/**
	 * Creates a view export format instance.
	 *
	 * @param extension   text value for extension
	 * @param displayName name value to use
	 */
	ViewExportFormat(final String extension, final String displayName) {
		this.extension = extension;
		this.displayName = displayName;
	}

	/**
	 * Returns the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Returns the extension.
	 *
	 * @return the extension
	 */
	public String getExtension() {
		return this.extension;
	}

	/**
	 * Builds a debug string for this view export format.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return this.displayName;
	}

}
