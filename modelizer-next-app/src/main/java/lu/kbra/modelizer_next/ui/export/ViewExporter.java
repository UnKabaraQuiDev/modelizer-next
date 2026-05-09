package lu.kbra.modelizer_next.ui.export;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import lu.kbra.modelizer_next.cmdline.CommandLineExportParser.InvalidArgumentException;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.triplet.Triplet;
import lu.kbra.pclib.datastructure.triplet.Triplets;

public final class ViewExporter {

	public static final String DEFAULT_FILE_PATTERN = "%FILENAME%-%TYPE%.%EXTENSION%";
	public static final List<String> FILE_PATTERN_TOKENS = List.of("%FILENAME%", "%TYPE%", "%EXTENSION%", "%DATE%", "%TIME%");

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm-ss");
	private static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

	public static List<Triplet<Optional<File>, PanelType, File>> exportViews(
			final Map<PanelType, DiagramCanvas> canvases,
			final ViewExportRequest request,
			final Optional<File> sourceFileName,
			final Consumer<Triplet<Optional<File>, PanelType, File>> callback) throws IOException {

		if (request == null || request.panelTypes() == null || request.panelTypes().isEmpty()) {
			throw new InvalidArgumentException("No panel type selected.");
		}
		if (request.outputDirectory() == null) {
			throw new InvalidArgumentException("No output directory selected.");
		}
		if (!request.outputDirectory().exists()) {
			Files.createDirectories(request.outputDirectory().toPath());
		}
		if (!request.outputDirectory().isDirectory()) {
			throw new InvalidArgumentException("The selected output path is not a directory.");
		}

		final List<Triplet<Optional<File>, PanelType, File>> exportedFiles = new ArrayList<>();
		final Set<String> usedPaths = new HashSet<>();

		final String baseFileName = ViewExporter
				.sanitizeFileName(PCUtils.removeFileExtension(sourceFileName.map(File::getName).orElse("Untitled")));

		for (final PanelType panelType : request.panelTypes()) {
			final DiagramCanvas canvas = canvases.get(panelType);
			if (canvas == null) {
				continue;
			}

			final String fileName = ViewExporter.buildFileName(request.fileNamePattern(), baseFileName, panelType, request.format());

			File outputFile = new File(request.outputDirectory(), fileName);
			outputFile = ViewExporter.ensureExtension(outputFile, request.format().getExtension());
			outputFile = ViewExporter.avoidDuplicatePath(outputFile, usedPaths);

			switch (request.format()) {
			case PNG -> ViewExporter.writePng(canvas, request.scope(), outputFile);
			case SVG -> ViewExporter.writeSvg(canvas, request.scope(), outputFile);
			}

			final Triplet<Optional<File>, PanelType, File> data = Triplets.readOnly(sourceFileName, panelType, outputFile);
			if (callback != null) {
				callback.accept(data);
			}
			exportedFiles.add(data);
		}

		return exportedFiles;

	}

	private static File avoidDuplicatePath(final File originalFile, final Set<String> usedPaths) {
		File candidate = originalFile;
		int counter = 2;
		while (!usedPaths.add(candidate.getAbsolutePath())) {
			final String extension = PCUtils.removeFileExtension(candidate.getName()).equals(candidate.getName()) ? ""
					: "." + ViewExporter.getExtension(candidate.getName());
			final String nameWithoutExtension = PCUtils.removeFileExtension(originalFile.getName());
			candidate = new File(originalFile.getParentFile(), nameWithoutExtension + "-" + counter + extension);
			counter++;
		}
		return candidate;
	}

	private static String buildFileName(
			final String rawPattern,
			final String sourceFileName,
			final PanelType panelType,
			final ViewExportFormat format) {

		String pattern = rawPattern == null || rawPattern.isBlank() ? ViewExporter.DEFAULT_FILE_PATTERN : rawPattern;
		final LocalDateTime now = LocalDateTime.now();
		pattern = pattern.replace("%FILENAME%", sourceFileName);
		pattern = pattern.replace("%TYPE%", ViewExporter.typeToken(panelType));
		pattern = pattern.replace("%EXTENSION%", format.getExtension());
		pattern = pattern.replace("%DATE%", ViewExporter.DATE_FORMAT.format(now));
		pattern = pattern.replace("%TIME%", ViewExporter.TIME_FORMAT.format(now));

		final String cleaned = ViewExporter.sanitizeFileName(pattern);
		return cleaned.isBlank() ? sourceFileName + "-" + ViewExporter.typeToken(panelType) + "." + format.getExtension() : cleaned;
	}

	private static File ensureExtension(final File file, final String extension) {
		if (file.getName().toLowerCase().endsWith("." + extension.toLowerCase())) {
			return file;
		}
		return new File(file.getParentFile(), file.getName() + "." + extension);
	}

	private static String getExtension(final String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return dotIndex < 0 || dotIndex == fileName.length() - 1 ? "" : fileName.substring(dotIndex + 1);
	}

	private static String sanitizeFileName(final String value) {
		if (value == null) {
			return "";
		}

		final String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
		return normalized.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
	}

	private static String typeToken(final PanelType panelType) {
		return switch (panelType) {
		case CONCEPTUAL -> "conceptual";
		case LOGICAL -> "logical";
		case PHYSICAL -> "physical";
		};
	}

	private static void writePng(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
		final BufferedImage image = canvas.createExportImage(scope);
		try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
			if (!ImageIO.write(image, "png", outputStream)) {
				throw new IOException("No PNG writer is available.");
			}
		}
	}

	private static void writeSvg(final DiagramCanvas canvas, final ViewExportScope scope, final File outputFile) throws IOException {
		final DOMImplementation domImplementation = GenericDOMImplementation.getDOMImplementation();
		final Document document = domImplementation.createDocument(ViewExporter.SVG_NAMESPACE_URI, "svg", null);
		final SVGGraphics2D svgGraphics = new SVGGraphics2D(document);
		final Dimension exportSize = canvas.getExportSize(scope);
		svgGraphics.setSVGCanvasSize(exportSize);
		canvas.paintExport(svgGraphics, scope);

		try (FileWriter writer = new FileWriter(outputFile)) {
			svgGraphics.stream(writer, true);
		}
	}

	private ViewExporter() {
	}

}
