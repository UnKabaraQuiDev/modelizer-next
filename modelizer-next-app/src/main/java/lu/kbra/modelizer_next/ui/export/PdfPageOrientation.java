package lu.kbra.modelizer_next.ui.export;

/**
 * Orientation used by PDF page formats.
 */
public enum PdfPageOrientation {

	PORTRAIT("Portrait"),
	LANDSCAPE("Landscape");

	private final String displayName;

	PdfPageOrientation(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

}
