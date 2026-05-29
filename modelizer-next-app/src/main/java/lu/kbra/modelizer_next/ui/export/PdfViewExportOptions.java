package lu.kbra.modelizer_next.ui.export;

import java.io.File;

/**
 * Options used by the PDF exporter.
 *
 * @param pageFormat selected page format
 * @param orientation selected page orientation
 * @param customPageWidth custom page width in points, used when pageFormat is CUSTOM
 * @param customPageHeight custom page height in points, used when pageFormat is CUSTOM
 * @param margins page margins in points
 * @param underTemplateFile optional image template drawn below the diagram content
 * @param overTemplateFile optional image template drawn above the diagram content
 * @param headerText header text pattern
 * @param footerText footer text pattern
 */
public record PdfViewExportOptions(
		PdfPageFormat pageFormat,
		PdfPageOrientation orientation,
		double customPageWidth,
		double customPageHeight,
		PdfMargins margins,
		File underTemplateFile,
		File overTemplateFile,
		String headerText,
		String footerText) implements ViewExportOptions {

	/**
	 * Returns default PDF export options.
	 *
	 * @return the default options
	 */
	public static PdfViewExportOptions defaults() {
		return new PdfViewExportOptions(PdfPageFormat.A4,
				PdfPageOrientation.PORTRAIT,
				PdfPageFormat.A4.getWidth(),
				PdfPageFormat.A4.getHeight(),
				PdfMargins.defaults(),
				null,
				null,
				"",
				"");
	}

	/**
	 * Returns the effective page height after applying custom size and orientation.
	 *
	 * @return the effective page height
	 */
	public double effectivePageHeight() {
		if (this.pageFormat() == PdfPageFormat.CUSTOM) {
			return this.baseHeight();
		}
		return this.orientation() == PdfPageOrientation.LANDSCAPE
				? Math.min(this.baseWidth(), this.baseHeight())
				: Math.max(this.baseWidth(), this.baseHeight());
	}

	/**
	 * Returns the effective page width after applying custom size and orientation.
	 *
	 * @return the effective page width
	 */
	public double effectivePageWidth() {
		if (this.pageFormat() == PdfPageFormat.CUSTOM) {
			return this.baseWidth();
		}
		return this.orientation() == PdfPageOrientation.LANDSCAPE
				? Math.max(this.baseWidth(), this.baseHeight())
				: Math.min(this.baseWidth(), this.baseHeight());
	}

	private double baseHeight() {
		return this.pageFormat() == PdfPageFormat.CUSTOM ? Math.max(1.0, this.customPageHeight()) : this.pageFormat().getHeight();
	}

	private double baseWidth() {
		return this.pageFormat() == PdfPageFormat.CUSTOM ? Math.max(1.0, this.customPageWidth()) : this.pageFormat().getWidth();
	}

}
