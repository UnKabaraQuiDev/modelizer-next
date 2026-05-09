package lu.kbra.modelizer_next.ui.export;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

	public static List<Triplet<File, PanelType, File>> exportViews(
			final Map<PanelType, DiagramCanvas> canvases,
			final ViewExportRequest request,
			final String sourceFileName,
			final Consumer<Triplet<File, PanelType, File>> callback) throws IOException {

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

		final List<File> sourceFiles = ViewExporter.resolveSourceFiles(sourceFileName, request.multiple(), request.wildcard());

		final List<Triplet<File, PanelType, File>> exportedFiles = new ArrayList<>();
		final Set<String> usedPaths = new HashSet<>();

		for (final File sourceFile : sourceFiles) {
			final String sourceName = sourceFile.getName() == null || sourceFile.getName().isBlank() ? sourceFile.getPath()
					: sourceFile.getName();

			final String baseFileName = ViewExporter
					.sanitizeFileName(ViewExporter.stripExtension(sourceName == null || sourceName.isBlank() ? "Untitled" : sourceName));

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

				final Triplet<File, PanelType, File> data = Triplets.readOnly(sourceFile, panelType, outputFile);
				callback.accept(data);
				exportedFiles.add(data);
			}
		}

		return exportedFiles;
	}

	private static List<File> resolveSourceFiles(final String rawInput, final boolean multipleFiles, final boolean wildcard)
			throws IOException {

		final String input = rawInput == null || rawInput.isBlank() ? "Untitled" : rawInput.trim();

		final List<String> entries = multipleFiles
				? Arrays.stream(input.split(",")).map(String::trim).filter(value -> !value.isBlank()).toList()
				: List.of(input);

		if (entries.isEmpty()) {
			throw new InvalidArgumentException("No source file selected.");
		}

		final List<File> sourceFiles = new ArrayList<>();
		final Set<Path> usedSourcePaths = new LinkedHashSet<>();

		for (final String entry : entries) {
			if (wildcard && ViewExporter.containsWildcard(entry)) {
				final List<Path> matchedPaths = ViewExporter.resolveWildcardPaths(entry);

				if (matchedPaths.isEmpty()) {
					throw new InvalidArgumentException("No source files matched wildcard pattern: " + entry);
				}

				for (final Path matchedPath : matchedPaths) {
					final Path normalizedPath = matchedPath.toAbsolutePath().normalize();
					if (usedSourcePaths.add(normalizedPath)) {
						sourceFiles.add(normalizedPath.toFile());
					}
				}
			} else {
				final Path path = ViewExporter.pathOf(entry);
				final Path normalizedPath = path.toAbsolutePath().normalize();

				if (usedSourcePaths.add(normalizedPath)) {
					sourceFiles.add(path.toFile());
				}
			}
		}

		if (sourceFiles.isEmpty()) {
			throw new InvalidArgumentException("No source file selected.");
		}

		return sourceFiles;
	}

	private static List<Path> resolveWildcardPaths(final String rawPattern) throws IOException {
		final Path patternPath = ViewExporter.pathOf(rawPattern);
		final boolean absolutePattern = patternPath.isAbsolute();

		final Path searchRoot = ViewExporter.findWildcardSearchRoot(patternPath).toAbsolutePath().normalize();

		if (!Files.exists(searchRoot) || !Files.isDirectory(searchRoot)) {
			return List.of();
		}

		final Path baseDirectory = Paths.get("").toAbsolutePath().normalize();

		final List<PathMatcher> matchers = ViewExporter.createWildcardMatchers(rawPattern, absolutePattern);

		try (Stream<Path> stream = Files.walk(searchRoot)) {
			return stream.filter(Files::isRegularFile)
					.filter(path -> ViewExporter.matchesWildcard(path, absolutePattern, baseDirectory, matchers))
					.sorted()
					.toList();
		}
	}

	private static boolean matchesWildcard(
			final Path path,
			final boolean absolutePattern,
			final Path baseDirectory,
			final List<PathMatcher> matchers) {

		final Path normalizedPath = path.toAbsolutePath().normalize();

		final Path pathToMatch = absolutePattern ? normalizedPath : baseDirectory.relativize(normalizedPath);

		for (final PathMatcher matcher : matchers) {
			if (matcher.matches(pathToMatch)) {
				return true;
			}
		}

		return false;
	}

	private static List<PathMatcher> createWildcardMatchers(final String rawPattern, final boolean absolutePattern) {

		final Path patternPath = ViewExporter.pathOf(rawPattern);

		final String globPattern = absolutePattern ? patternPath.toAbsolutePath().normalize().toString() : rawPattern;

		return ViewExporter.expandDoubleStarZeroDirectoryVariants(ViewExporter.normalizeGlobSeparators(globPattern))
				.stream()
				.map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
				.toList();
	}

	private static List<String> expandDoubleStarZeroDirectoryVariants(final String globPattern) {
		final Set<String> variants = new LinkedHashSet<>();
		ViewExporter.expandDoubleStarZeroDirectoryVariants(globPattern, variants);
		return new ArrayList<>(variants);
	}

	private static void expandDoubleStarZeroDirectoryVariants(final String globPattern, final Set<String> variants) {

		if (!variants.add(globPattern)) {
			return;
		}

		final String separator = File.separator;
		final String leadingDoubleStar = "**" + separator;

		if (globPattern.startsWith(leadingDoubleStar)) {
			ViewExporter.expandDoubleStarZeroDirectoryVariants(globPattern.substring(leadingDoubleStar.length()), variants);
		}

		final String middleDoubleStar = separator + "**" + separator;

		int index = globPattern.indexOf(middleDoubleStar);
		while (index >= 0) {
			final String withoutDoubleStar = globPattern.substring(0, index + separator.length())
					+ globPattern.substring(index + middleDoubleStar.length());

			ViewExporter.expandDoubleStarZeroDirectoryVariants(withoutDoubleStar, variants);

			index = globPattern.indexOf(middleDoubleStar, index + 1);
		}
	}

	private static Path findWildcardSearchRoot(final Path patternPath) {
		Path searchRoot = patternPath.getRoot();

		if (searchRoot == null) {
			searchRoot = Paths.get("");
		}

		for (final Path part : patternPath) {
			final String value = part.toString();

			if (ViewExporter.containsWildcard(value)) {
				break;
			}

			searchRoot = searchRoot.resolve(value);
		}

		return searchRoot;
	}

	private static boolean containsWildcard(final String value) {
		return value != null && (value.indexOf('*') >= 0 || value.indexOf('?') >= 0);
	}

	private static Path pathOf(final String value) {
		try {
			return Paths.get(value);
		} catch (final InvalidPathException exception) {
			throw new InvalidArgumentException("Invalid source path: " + value);
		}
	}

	private static String normalizeGlobSeparators(final String value) {
		return value.replace('\\', File.separatorChar).replace('/', File.separatorChar);
	}

	private static File avoidDuplicatePath(final File originalFile, final Set<String> usedPaths) {
		File candidate = originalFile;
		int counter = 2;
		while (!usedPaths.add(candidate.getAbsolutePath())) {
			final String extension = ViewExporter.stripExtension(candidate.getName()).equals(candidate.getName()) ? ""
					: "." + ViewExporter.getExtension(candidate.getName());
			final String nameWithoutExtension = ViewExporter.stripExtension(originalFile.getName());
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

	private static String stripExtension(final String fileName) {
		if (fileName == null) {
			return "";
		}

		return PCUtils.removeFileExtension(fileName);
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
