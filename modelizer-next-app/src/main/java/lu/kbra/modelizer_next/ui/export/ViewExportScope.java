package lu.kbra.modelizer_next.ui.export;

/**
 * Parts of the document that can be exported.
 */
public enum ViewExportScope {

	SELECTION("Selection only"),
	VIEW("Current view only"),
	EVERYTHING("Everything");

	private final String displayName;

	/**
	 * Creates a view export scope instance.
	 *
	 * @param displayName name value to use
	 */
	ViewExportScope(final String displayName) {
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
	 * Builds a debug string for this view export scope.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return this.displayName;
	}

}
