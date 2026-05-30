package lu.kbra.modelizer_next.ui.export;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

/**
 * Writes a single-page image based PDF export.
 */
final class PdfViewExporter {

	/**
	 * Holds image object data for PDF output.
	 *
	 * @param name     resource name
	 * @param objectId PDF object id
	 * @param width    image width in pixels
	 * @param height   image height in pixels
	 */
	private record PdfImageResource(String name, int objectId, int width, int height) {
	}

	/**
	 * Small PDF writer for the image-only exports used here.
	 */
	private static final class PdfWriter {

		private final List<byte[]> objects = new ArrayList<>();

		private int addObject(final byte[] body) {
			this.objects.add(body);
			return this.objects.size();
		}

		private int addObject(final String body) {
			return this.addObject(body.getBytes(StandardCharsets.ISO_8859_1));
		}

		private int addStreamObject(final String dictionary, final byte[] streamData) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				out.write((dictionary + "\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
				out.write(streamData);
				out.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
			} catch (final IOException ex) {
				throw new IllegalStateException("Could not create PDF stream object.", ex);
			}
			return this.addObject(out.toByteArray());
		}

		private int reserveObject() {
			this.objects.add(new byte[0]);
			return this.objects.size();
		}

		private void setObject(final int objectId, final String body) {
			this.objects.set(objectId - 1, body.getBytes(StandardCharsets.ISO_8859_1));
		}

		private void writeTo(final File file, final int catalogObjectId) throws IOException {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final List<Integer> offsets = new ArrayList<>();

			out.write("%PDF-1.5\n%\u00e2\u00e3\u00cf\u00d3\n".getBytes(StandardCharsets.ISO_8859_1));
			offsets.add(0);

			for (int i = 0; i < this.objects.size(); i++) {
				offsets.add(out.size());
				out.write((i + 1 + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));
				out.write(this.objects.get(i));
				out.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
			}

			final int xrefOffset = out.size();
			out.write(("xref\n0 " + (this.objects.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
			out.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
			for (int i = 1; i < offsets.size(); i++) {
				out.write(String.format(Locale.US, "%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
			}
			out.write(("trailer\n<< /Size " + (this.objects.size() + 1) + " /Root " + catalogObjectId + " 0 R >>\nstartxref\n" + xrefOffset
					+ "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

			Files.write(file.toPath(), out.toByteArray());
		}

	}

	private static final double TEXT_SIZE = 10.0;
	private static final double TEXT_LINE_HEIGHT = 14.0;
	private static final double TEXT_PADDING = 8.0;

	/**
	 * Exports the canvas as a PDF file.
	 *
	 * @param canvas     canvas to export
	 * @param request    export request
	 * @param context    export context
	 * @param outputFile target file
	 * @throws IOException if the PDF cannot be written
	 */
	public static void
			export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context, final File outputFile)
					throws IOException {

		final PdfViewExportOptions options = request.pdfOptions();
		final double pageWidth = Math.max(1.0, options.effectivePageWidth());
		final double pageHeight = Math.max(1.0, options.effectivePageHeight());
		final PdfMargins margins = options.margins() == null ? PdfMargins.defaults() : options.margins();
		final Color backgroundColor = request.backgroundColor() == null ? Color.WHITE : request.backgroundColor();

		final BufferedImage diagramImage = ViewExportFormat.flattenImage(canvas.createExportImage(request.scope()), backgroundColor);
		final BufferedImage underTemplateImage = PdfViewExporter.readOptionalTemplate(options.underTemplateFile());
		final BufferedImage overTemplateImage = PdfViewExporter.readOptionalTemplate(options.overTemplateFile());

		final PdfWriter writer = new PdfWriter();
		final PdfImageResource diagram = PdfViewExporter.addImage(writer, "ImDiagram", diagramImage);
		final PdfImageResource underTemplate = underTemplateImage == null ? null
				: PdfViewExporter.addImage(writer, "ImTemplateUnder", underTemplateImage);
		final PdfImageResource overTemplate = overTemplateImage == null ? null
				: PdfViewExporter.addImage(writer, "ImTemplateOver", overTemplateImage);

		final int underOcgId = underTemplate == null ? 0 : writer.addObject("<< /Type /OCG /Name (Template under) >>");
		final int overOcgId = overTemplate == null ? 0 : writer.addObject("<< /Type /OCG /Name (Template over) >>");
		final int fontId = writer.addObject("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

		final String content = PdfViewExporter.createContentStream(request,
				context,
				options,
				pageWidth,
				pageHeight,
				margins,
				backgroundColor,
				diagram,
				underTemplate,
				overTemplate);
		final byte[] compressedContent = PdfViewExporter.deflate(content.getBytes(StandardCharsets.ISO_8859_1));
		final int contentId = writer.addStreamObject("<< /Length " + compressedContent.length + " /Filter /FlateDecode >>",
				compressedContent);

		final int pagesId = writer.reserveObject();
		final int pageId = writer.addObject(PdfViewExporter.createPageObject(pagesId,
				pageWidth,
				pageHeight,
				contentId,
				fontId,
				diagram,
				underTemplate,
				overTemplate,
				underOcgId,
				overOcgId));
		writer.setObject(pagesId, "<< /Type /Pages /Kids [" + pageId + " 0 R] /Count 1 >>");
		final int catalogId = writer.addObject(PdfViewExporter.createCatalogObject(pagesId, underOcgId, overOcgId));
		writer.writeTo(outputFile, catalogId);
	}

	private static PdfImageResource addImage(final PdfWriter writer, final String name, final BufferedImage rawImage) throws IOException {
		final BufferedImage rgbImage = ViewExportFormat.flattenImage(rawImage, Color.WHITE);
		final byte[] rgbBytes = PdfViewExporter.toRgbBytes(rgbImage);
		final byte[] compressedBytes = PdfViewExporter.deflate(rgbBytes);
		final String dictionary = "<< /Type /XObject /Subtype /Image /Width " + rgbImage.getWidth() + " /Height " + rgbImage.getHeight()
				+ " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length " + compressedBytes.length + " >>";
		final int objectId = writer.addStreamObject(dictionary, compressedBytes);
		return new PdfImageResource(name, objectId, rgbImage.getWidth(), rgbImage.getHeight());
	}

	private static void appendImageCommand(
			final StringBuilder builder,
			final PdfImageResource image,
			final double x,
			final double y,
			final double width,
			final double height) {
		builder.append(PdfViewExporter.format("q %.4f 0 0 %.4f %.4f %.4f cm /%s Do Q\n", width, height, x, y, image.name()));
	}

	private static void
			appendText(final StringBuilder builder, final String rawText, final double x, final double y, final double maxWidth) {
		final String text = PdfViewExporter.truncate(rawText, (int) Math.max(1, Math.floor(maxWidth / (PdfViewExporter.TEXT_SIZE * 0.55))));
		builder.append("BT\n/F1 ").append(PdfViewExporter.format("%.4f", PdfViewExporter.TEXT_SIZE)).append(" Tf\n");
		builder.append("0 0 0 rg\n");
		builder.append(PdfViewExporter.format("%.4f %.4f Td\n", x, y));
		builder.append('(').append(PdfViewExporter.escapePdfText(text)).append(") Tj\nET\n");
	}

	private static String createCatalogObject(final int pagesId, final int underOcgId, final int overOcgId) {
		final StringBuilder builder = new StringBuilder("<< /Type /Catalog /Pages ").append(pagesId).append(" 0 R");
		final List<Integer> ocgIds = new ArrayList<>();
		if (underOcgId > 0) {
			ocgIds.add(underOcgId);
		}
		if (overOcgId > 0) {
			ocgIds.add(overOcgId);
		}
		if (!ocgIds.isEmpty()) {
			builder.append(" /OCProperties << /OCGs [");
			for (final int ocgId : ocgIds) {
				builder.append(ocgId).append(" 0 R ");
			}
			builder.append("] /D << /Order [");
			for (final int ocgId : ocgIds) {
				builder.append(ocgId).append(" 0 R ");
			}
			builder.append("] /ON [");
			for (final int ocgId : ocgIds) {
				builder.append(ocgId).append(" 0 R ");
			}
			builder.append("] >> >>");
		}
		builder.append(" >>");
		return builder.toString();
	}

	private static String createContentStream(
			final ViewExportRequest request,
			final ViewExportContext context,
			final PdfViewExportOptions options,
			final double pageWidth,
			final double pageHeight,
			final PdfMargins margins,
			final Color backgroundColor,
			final PdfImageResource diagram,
			final PdfImageResource underTemplate,
			final PdfImageResource overTemplate) {

		final String headerText = PdfViewExporter.resolveText(options.headerText(), request, context, 1, 1);
		final String footerText = PdfViewExporter.resolveText(options.footerText(), request, context, 1, 1);
		final double headerHeight = headerText.isBlank() ? 0.0 : PdfViewExporter.TEXT_LINE_HEIGHT + PdfViewExporter.TEXT_PADDING;
		final double footerHeight = footerText.isBlank() ? 0.0 : PdfViewExporter.TEXT_LINE_HEIGHT + PdfViewExporter.TEXT_PADDING;
		final double contentX = Math.max(0.0, margins.left());
		final double contentY = Math.max(0.0, margins.bottom() + footerHeight);
		final double contentWidth = Math.max(1.0, pageWidth - margins.left() - margins.right());
		final double contentHeight = Math.max(1.0, pageHeight - margins.top() - margins.bottom() - headerHeight - footerHeight);
		final double scale = Math.min(contentWidth / diagram.width(), contentHeight / diagram.height());
		final double imageWidth = diagram.width() * scale;
		final double imageHeight = diagram.height() * scale;
		final double imageX = contentX + (contentWidth - imageWidth) / 2.0;
		final double imageY = contentY + (contentHeight - imageHeight) / 2.0;

		final StringBuilder builder = new StringBuilder();
		builder.append(PdfViewExporter.fillColorCommand(backgroundColor));
		builder.append(PdfViewExporter.format("0 0 %.4f %.4f re f\n", pageWidth, pageHeight));

		if (underTemplate != null) {
			builder.append("/OC /TPL_UNDER BDC\n");
			PdfViewExporter.appendImageCommand(builder, underTemplate, 0.0, 0.0, pageWidth, pageHeight);
			builder.append("EMC\n");
		}

		PdfViewExporter.appendImageCommand(builder, diagram, imageX, imageY, imageWidth, imageHeight);

		if (overTemplate != null) {
			builder.append("/OC /TPL_OVER BDC\n");
			PdfViewExporter.appendImageCommand(builder, overTemplate, 0.0, 0.0, pageWidth, pageHeight);
			builder.append("EMC\n");
		}

		if (!headerText.isBlank()) {
			PdfViewExporter.appendText(builder,
					headerText,
					margins.left(),
					pageHeight - margins.top() - PdfViewExporter.TEXT_SIZE,
					Math.max(1.0, pageWidth - margins.left() - margins.right()));
		}
		if (!footerText.isBlank()) {
			PdfViewExporter.appendText(builder,
					footerText,
					margins.left(),
					Math.max(PdfViewExporter.TEXT_SIZE, margins.bottom()),
					Math.max(1.0, pageWidth - margins.left() - margins.right()));
		}
		return builder.toString();
	}

	private static String createPageObject(
			final int pagesId,
			final double pageWidth,
			final double pageHeight,
			final int contentId,
			final int fontId,
			final PdfImageResource diagram,
			final PdfImageResource underTemplate,
			final PdfImageResource overTemplate,
			final int underOcgId,
			final int overOcgId) {

		final StringBuilder xObjects = new StringBuilder("<< /").append(diagram.name())
				.append(' ')
				.append(diagram.objectId())
				.append(" 0 R ");
		if (underTemplate != null) {
			xObjects.append('/').append(underTemplate.name()).append(' ').append(underTemplate.objectId()).append(" 0 R ");
		}
		if (overTemplate != null) {
			xObjects.append('/').append(overTemplate.name()).append(' ').append(overTemplate.objectId()).append(" 0 R ");
		}
		xObjects.append(">>");

		final StringBuilder properties = new StringBuilder("<< ");
		if (underOcgId > 0) {
			properties.append("/TPL_UNDER ").append(underOcgId).append(" 0 R ");
		}
		if (overOcgId > 0) {
			properties.append("/TPL_OVER ").append(overOcgId).append(" 0 R ");
		}
		properties.append(">>");

		return PdfViewExporter.format(
				"<< /Type /Page /Parent %d 0 R /MediaBox [0 0 %.4f %.4f] /Resources << /XObject %s /Font << /F1 %d 0 R >> /Properties %s >> /Contents %d 0 R >>",
				pagesId,
				pageWidth,
				pageHeight,
				xObjects,
				fontId,
				properties,
				contentId);
	}

	private static byte[] deflate(final byte[] input) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflater = new DeflaterOutputStream(out)) {
			deflater.write(input);
		}
		return out.toByteArray();
	}

	private static String escapePdfText(final String value) {
		return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\r", " ").replace("\n", " ");
	}

	private static String fillColorCommand(final Color color) {
		final Color rgbColor = color == null ? Color.WHITE : color;
		return PdfViewExporter
				.format("%.4f %.4f %.4f rg\n", rgbColor.getRed() / 255.0, rgbColor.getGreen() / 255.0, rgbColor.getBlue() / 255.0);
	}

	private static String format(final String format, final Object... args) {
		return String.format(Locale.US, format, args);
	}

	private static BufferedImage readOptionalTemplate(final File file) throws IOException {
		if (file == null || file.getPath().isBlank()) {
			return null;
		}
		if (!file.exists()) {
			throw new IOException("PDF template file does not exist: " + file.getAbsolutePath());
		}
		final BufferedImage image = ImageIO.read(file);
		if (image == null) {
			throw new IOException("Could not read PDF template as an image: " + file.getAbsolutePath());
		}
		return image;
	}

	private static String resolveText(
			final String rawValue,
			final ViewExportRequest request,
			final ViewExportContext context,
			final int page,
			final int pages) {

		String value = rawValue == null ? "" : rawValue;
		final Optional<File> sourceFile = context == null || context.sourceFile() == null ? Optional.empty() : context.sourceFile();
		final String fileName = sourceFile.map(File::getName)
				.orElse(context == null || context.outputFile() == null ? "Untitled" : context.outputFile().getName());
		final PanelType panelType = context == null ? null : context.panelType();
		value = value.replace("%FILENAME%", ViewExporter.baseName(fileName));
		value = value.replace("%TYPE%", ViewExporter.typeToken(panelType));
		value = value.replace("%PANEL%", ViewExporter.typeToken(panelType));
		value = value.replace("%EXTENSION%", request == null || request.format() == null ? "pdf" : request.format().getExtension());
		value = value.replace("%PAGE%", Integer.toString(page));
		value = value.replace("%PAGES%", Integer.toString(pages));
		return ViewExporter.replaceDateTimeTokens(value);
	}

	private static byte[] toRgbBytes(final BufferedImage image) {
		final byte[] bytes = new byte[image.getWidth() * image.getHeight() * 3];
		int index = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int rgb = image.getRGB(x, y);
				bytes[index++] = (byte) (rgb >> 16 & 0xFF);
				bytes[index++] = (byte) (rgb >> 8 & 0xFF);
				bytes[index++] = (byte) (rgb & 0xFF);
			}
		}
		return bytes;
	}

	private static String truncate(final String value, final int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, Math.max(0, maxLength - 1)) + "...";
	}

	private PdfViewExporter() {
	}

}
