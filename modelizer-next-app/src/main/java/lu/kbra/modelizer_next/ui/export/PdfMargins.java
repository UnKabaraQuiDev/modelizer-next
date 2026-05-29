package lu.kbra.modelizer_next.ui.export;

/**
 * PDF page margins in points.
 *
 * @param top top margin in points
 * @param right right margin in points
 * @param bottom bottom margin in points
 * @param left left margin in points
 */
public record PdfMargins(double top, double right, double bottom, double left) {

	/**
	 * Returns default PDF margins.
	 *
	 * @return the default margins
	 */
	public static PdfMargins defaults() {
		return new PdfMargins(36.0, 36.0, 36.0, 36.0);
	}

}
