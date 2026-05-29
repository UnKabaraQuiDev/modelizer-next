package lu.kbra.modelizer_next.ui.export;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

/**
 * Supported file formats for diagram view export.
 */
public enum ViewExportFormat {

	PNG("png", "PNG", true) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "png", outputFile, this.supportsTransparency());
		}

	},

	JPEG("jpg", "JPEG", false) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "jpg", outputFile, this.supportsTransparency());
		}

	},

	BMP("bmp", "BMP", false) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "bmp", outputFile, this.supportsTransparency());
		}

	},

	TIFF("tiff", "TIFF", true) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "tiff", outputFile, this.supportsTransparency());
		}

	},

	TIF("tif", "TIF", true) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "tif", outputFile, this.supportsTransparency());
		}

	},

	WEBP("webp", "WebP", true) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(request.scope()), request, "webp", outputFile, this.supportsTransparency());
		}

	},

	PDF("pdf", "PDF", false) {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context,
				final File outputFile) throws IOException {
			PdfViewExporter.export(canvas, request, context, outputFile);
		}

	};

	static {
		ImageIO.scanForPlugins();
	}

	/**
	 * Flattens transparent pixels over an RGB background color.
	 *
	 * @param image raw image to flatten
	 * @param backgroundColor background color to use
	 * @return an RGB image
	 */
	static BufferedImage flattenImage(final BufferedImage image, final Color backgroundColor) {
		final Color rgbBackground = backgroundColor == null ? Color.WHITE
				: new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
		final BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = rgbImage.createGraphics();
		try {
			g2.setColor(rgbBackground);
			g2.fillRect(0, 0, image.getWidth(), image.getHeight());
			g2.drawImage(image, 0, 0, null);
		} finally {
			g2.dispose();
		}
		return rgbImage;
	}

	private static BufferedImage imageForFormat(
			final BufferedImage rawImage,
			final ViewExportRequest request,
			final boolean formatSupportsTransparency) {

		if (formatSupportsTransparency && request.imageOptions().transparentBackground()) {
			return rawImage;
		}

		return ViewExportFormat.flattenImage(rawImage, request.backgroundColor());
	}

	private static void writeImage(
			final BufferedImage rawImage,
			final ViewExportRequest request,
			final String format,
			final File outputFile,
			final boolean formatSupportsTransparency) throws IOException {

		final BufferedImage image = ViewExportFormat.imageForFormat(rawImage, request, formatSupportsTransparency);
		final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
		if (!writers.hasNext()) {
			throw new IOException("No ImageWriter found for format: " + format);
		}
		final ImageWriter writer = writers.next();

		try (OutputStream os = Files.newOutputStream(outputFile.toPath()); ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
			writer.setOutput(ios);

			final ImageWriteParam param = writer.getDefaultWriteParam();
			writer.write(null, new IIOImage(image, null, null), param);
		} finally {
			writer.dispose();
		}
	}

	private final String extension;
	private final String displayName;
	private final boolean supportsTransparency;

	/**
	 * Creates a view export format instance.
	 *
	 * @param extension   text value for extension
	 * @param displayName name value to use
	 * @param supportsTransparency whether this format can store transparent pixels
	 */
	ViewExportFormat(final String extension, final String displayName, final boolean supportsTransparency) {
		this.extension = extension;
		this.displayName = displayName;
		this.supportsTransparency = supportsTransparency;
	}

	/**
	 * Creates the default options for this file format.
	 *
	 * @return the created default options
	 */
	public ViewExportOptions createDefaultOptions() {
		return this == ViewExportFormat.PDF ? PdfViewExportOptions.defaults() : ImageViewExportOptions.defaults();
	}

	/**
	 * Exports a canvas to a file.
	 *
	 * @param canvas canvas instance to export
	 * @param request request with common and format-specific settings
	 * @param context export context values
	 * @param outputFile target file
	 * @throws IOException if the file cannot be written
	 */
	public abstract void export(
			DiagramCanvas canvas,
			ViewExportRequest request,
			ViewExportContext context,
			File outputFile) throws IOException;

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
	 * Checks whether this format can store transparent pixels.
	 *
	 * @return {@code true} when alpha can be preserved
	 */
	public boolean supportsTransparency() {
		return this.supportsTransparency;
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
