package lu.kbra.modelizer_next.ui.export;

import java.io.File;
import java.io.IOException;
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

import lu.kbra.modelizer_next.cmdline.CommandLineExportParser.InvalidArgumentException;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.triplet.Triplet;
import lu.kbra.pclib.datastructure.triplet.Triplets;

/**
 * Exports one or more document views to image files.
 */
public final class ViewExporter {

	public static final String DEFAULT_FILE_PATTERN = "%FILENAME%-%TYPE%.%EXTENSION%";
	public static final List<String> FILE_PATTERN_TOKENS = List.of("%FILENAME%", "%TYPE%", "%EXTENSION%", "%DATE%", "%TIME%");

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm-ss");
	public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

	/**
	 * Returns an unused output path by adding a numeric suffix when needed.
	 *
	 * @param originalFile file to read or write
	 * @param usedPaths    used paths value used by the operation
	 * @return the avoid duplicate path result
	 */
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

	/**
	 * Builds a file name.
	 *
	 * @param rawPattern     text value for raw pattern
	 * @param sourceFileName name value to use
	 * @param panelType      diagram panel type whose model or layout should be used
	 * @param format         export format to use
	 * @return the built file name
	 */
	private static String
			buildFileName(final String rawPattern, final String sourceFileName, final PanelType panelType, final ViewExportFormat format) {

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

	/**
	 * Ensures that the extension exists or is up to date.
	 *
	 * @param file      file to read or write
	 * @param extension text value for extension
	 * @return the ensure extension result
	 */
	private static File ensureExtension(final File file, final String extension) {
		if (file.getName().toLowerCase().endsWith("." + extension.toLowerCase())) {
			return file;
		}
		return new File(file.getParentFile(), file.getName() + "." + extension);
	}

	/**
	 * Exports the views.
	 *
	 * @param canvases       canvases value used by the operation
	 * @param request        export request being processed
	 * @param sourceFileName name value to use
	 * @param callback       callback value used by the operation
	 * @return an optional result when a matching value is available
	 * @throws IOException if the operation cannot be completed
	 */
	public static List<Triplet<Optional<File>, PanelType, File>> exportViews(
			final Map<PanelType, DiagramCanvas> canvases,
			final ViewExportRequest request,
			final Optional<File> sourceFileName,
			final Consumer<Triplet<Optional<File>, PanelType, File>> callback)
			throws IOException {

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

			final ViewExportContext context = new ViewExportContext(sourceFileName, panelType, outputFile);
			request.format().export(canvas, request, context, outputFile);

			final Triplet<Optional<File>, PanelType, File> data = Triplets.readOnly(sourceFileName, panelType, outputFile);
			if (callback != null) {
				callback.accept(data);
			}
			exportedFiles.add(data);
		}

		return exportedFiles;

	}

	/**
	 * Returns a file name without its last extension.
	 *
	 * @param fileName name value to process
	 * @return the base name
	 */
	public static String baseName(final String fileName) {
		return PCUtils.removeFileExtension(fileName == null ? "" : fileName);
	}

	/**
	 * Replaces date and time fields in an export text.
	 *
	 * @param value text value to process
	 * @return text with date and time tokens replaced
	 */
	public static String replaceDateTimeTokens(String value) {
		if (value == null) {
			return "";
		}

		final LocalDateTime now = LocalDateTime.now();
		value = value.replace("%DATE%", ViewExporter.DATE_FORMAT.format(now));
		value = value.replace("%TIME%", ViewExporter.TIME_FORMAT.format(now));
		return value;
	}

	/**
	 * Returns the extension.
	 *
	 * @param fileName name value to use
	 * @return the extension
	 */
	private static String getExtension(final String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return dotIndex < 0 || dotIndex == fileName.length() - 1 ? "" : fileName.substring(dotIndex + 1);
	}

	/**
	 * Sanitizes the file name so it can be used safely.
	 *
	 * @param value value to process
	 * @return the sanitize file name result
	 */
	private static String sanitizeFileName(final String value) {
		if (value == null) {
			return "";
		}

		final String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
		return normalized.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
	}

	/**
	 * Returns the file-name token for a panel type.
	 *
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the type token result
	 */
	public static String typeToken(final PanelType panelType) {
		if (panelType == null) {
			return "view";
		}
		return switch (panelType) {
		case CONCEPTUAL -> "conceptual";
		case LOGICAL -> "logical";
		case PHYSICAL -> "physical";
		};
	}

	/**
	 * Creates a view exporter instance.
	 */
	private ViewExporter() {
	}

}
