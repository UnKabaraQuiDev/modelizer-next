package lu.kbra.modelizer_next.ui.export;

/**
 * Common PDF page formats. Values are in PDF points.
 */
public enum PdfPageFormat {

	A5("A5", 420.0, 595.0),
	A4("A4", 595.0, 842.0),
	A3("A3", 842.0, 1191.0),
	LETTER("Letter", 612.0, 792.0),
	LEGAL("Legal", 612.0, 1008.0),
	CUSTOM("Custom", 595.0, 842.0);

	private final String displayName;
	private final double width;
	private final double height;

	PdfPageFormat(final String displayName, final double width, final double height) {
		this.displayName = displayName;
		this.width = width;
		this.height = height;
	}

	public double getHeight() {
		return this.height;
	}

	public double getWidth() {
		return this.width;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

}
