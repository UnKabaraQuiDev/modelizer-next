package lu.kbra.modelizer_next.ui.export;

import java.awt.Color;
import java.io.File;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Immutable request object passed to the exporter.
 *
 * @param format          export format to use
 * @param scope           export scope to use
 * @param panelTypes      values for panel types
 * @param outputDirectory output directory value used by the operation
 * @param fileNamePattern text value for file name pattern
 * @param multiple        whether multiple input files are allowed
 * @param wildcard        whether wildcard path matching is enabled
 * @param backgroundColor background color used when transparent export pixels must be flattened
 * @param options         format-specific export options
 */
public record ViewExportRequest(
		ViewExportFormat format,
		ViewExportScope scope,
		List<PanelType> panelTypes,
		File outputDirectory,
		String fileNamePattern,
		boolean multiple,
		boolean wildcard,
		Color backgroundColor,
		ViewExportOptions options) {

	/**
	 * Creates a request with default background and format-specific options.
	 *
	 * @param format          export format to use
	 * @param scope           export scope to use
	 * @param panelTypes      values for panel types
	 * @param outputDirectory output directory value used by the operation
	 * @param fileNamePattern text value for file name pattern
	 * @param multiple        whether multiple input files are allowed
	 * @param wildcard        whether wildcard path matching is enabled
	 */
	public ViewExportRequest(
			final ViewExportFormat format,
			final ViewExportScope scope,
			final List<PanelType> panelTypes,
			final File outputDirectory,
			final String fileNamePattern,
			final boolean multiple,
			final boolean wildcard) {
		this(format, scope, panelTypes, outputDirectory, fileNamePattern, multiple, wildcard, Color.WHITE, null);
	}

	/**
	 * Normalizes null values to safe defaults.
	 */
	public ViewExportRequest {
		final ViewExportFormat effectiveFormat = format == null ? ViewExportFormat.PNG : format;
		format = effectiveFormat;
		backgroundColor = backgroundColor == null ? Color.WHITE : new Color(backgroundColor.getRed(),
				backgroundColor.getGreen(),
				backgroundColor.getBlue());
		options = options == null ? effectiveFormat.createDefaultOptions() : options;
	}

	/**
	 * Returns the effective image options.
	 *
	 * @return image options or image defaults when another option type is stored
	 */
	public ImageViewExportOptions imageOptions() {
		return this.options() instanceof ImageViewExportOptions imageOptions ? imageOptions : ImageViewExportOptions.defaults();
	}

	/**
	 * Returns the effective PDF options.
	 *
	 * @return PDF options or PDF defaults when another option type is stored
	 */
	public PdfViewExportOptions pdfOptions() {
		return this.options() instanceof PdfViewExportOptions pdfOptions ? pdfOptions : PdfViewExportOptions.defaults();
	}

}
