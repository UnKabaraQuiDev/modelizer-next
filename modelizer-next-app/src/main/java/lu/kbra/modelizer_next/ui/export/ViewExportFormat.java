package lu.kbra.modelizer_next.ui.export;

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

	PNG("png", "PNG") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "png", outputFile);
		}

	},

	JPEG("jpg", "JPEG") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "jpg", outputFile);
		}

	},

	BMP("bmp", "BMP") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "bmp", outputFile);
		}

	},

	TIFF("tiff", "TIFF") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "tiff", outputFile);
		}

	},

	TIF("tif", "TIF") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "tif", outputFile);
		}

	},

	WEBP("webp", "WebP") {

		@Override
		public void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
			ViewExportFormat.writeImage(canvas.createExportImage(scope), "webp", outputFile);
		}

	};

	static {
		ImageIO.scanForPlugins();
	}

	private static void writeImage(final BufferedImage image, final String format, final File outputFile) throws IOException {
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

	public abstract void export(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException;

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
