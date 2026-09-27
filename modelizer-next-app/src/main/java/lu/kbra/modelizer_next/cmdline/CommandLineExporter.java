package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lu.kbra.model_exporter.api.ExportContext;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ExporterApiContext;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.model_exporter.api.ModelVisitResult;
import lu.kbra.modelizer_next.common.DefaultExportUpdateCallback;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.document.ModelDocument;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;
import lu.kbra.pclib.pointer.prim.IntPointer;

/**
 * Runs document exports without opening the interactive desktop frame.
 */
public final class CommandLineExporter {

	public static int run(final String[] args) {
		System.setProperty("java.awt.headless", "true");

		try {
			ExporterApiContext.getApiContext().setContext(ExportContext.CLI);
			Exporters.init();

			final CommandLineExportOptions options = CommandLineExportParser.parse(args);
			final List<URI> inputFiles = CommandLineExporter.resolveInputFiles(options.inputFile(), options.multiple(), options.wildcard());
			final ModelDocumentProducer documentProducer = new InputFileDocumentProducer(inputFiles, options.force());

			final IntPointer exportedFileCount = new IntPointer(0);

			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(options.jobCount());
			final List<Throwable> caughtException = Collections.synchronizedList(new ArrayList<>());

			final ModelExporter exporter = options.exporter();

			System.out.println("\n");
			final ProgressRenderer renderer = options.batch() ? new BatchProgressRenderer() : new InteractiveProgressRenderer();

			final ExportUpdateCallback updateCallback = DefaultExportUpdateCallback.create(documentProducer.getExpectedCount() + " file(s)",
					renderer);
			updateCallback.setAggregateChildProgress(true);
			updateCallback.setExpectedChildCount(documentProducer.getExpectedCount());

			final ExporterOptions config = (ExporterOptions) options.options();

			Optional<LoadedDocument> loadedDocument;
			while ((loadedDocument = documentProducer.next()).isPresent()) {
				final LoadedDocument doc = loadedDocument.get();
				executor.submit(() -> {
					try {
						ExporterApiContext.clearApiContext();
						ExporterApiContext.getApiContext().setContext(ExportContext.CLI);
						ExporterApiContext.getApiContext().setCurrentConfig(options.configFile());
						ExporterApiContext.getApiContext().setCurrentDocument(doc.sourceFile());
						ExporterApiContext.getApiContext()
								.setOutputDirectory(options.outputDirectory() != null ? options.outputDirectory().toPath() : null);
						ExporterApiContext.getApiContext().setRenderers(pt -> CommandLineExporter.createCanvases(doc.document(), pt));

						final ModelVisitResult result = exporter.buildModelVisitor(config).visitDocument(doc.document(), updateCallback);

						exportedFileCount.add(result.exportedFiles().size());
					} catch (final Throwable e) {
						caughtException.add(e);
						executor.shutdownNow();
					}
				});
			}

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
			renderer.close();
			System.out.println("\n");

			if (!caughtException.isEmpty()) {
				System.err.println("Got: " + caughtException.size() + " errors.");
				caughtException.forEach(e -> e.printStackTrace(System.err));
				return 1;
			}

			if (exportedFileCount.get() == 0) {
				System.err.println("Exported nothing.");
				return 3;
			}

			System.out.println("Exported " + exportedFileCount.get() + " files.");

			return 0;
		} catch (final CommandLineExportParser.HelpRequestedException ex) {
			return 0;
		} catch (final ExportAbortedException ex) {
			System.err.println(ex.getMessage());
			return 2;
		} catch (final Exception ex) {
			System.err.println("Export failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
			return 1;
		}
	}

	/**
	 * Adds the input file.
	 *
	 * @param inputFiles values for input files
	 * @param usedPaths  used paths value used by the operation
	 * @param inputFile  file to read or write
	 */
	private static void addInputFile(final List<URI> inputFiles, final Set<Path> usedPaths, final URI inputFile) {
		final Path inputPath = CommandLineExporter.toNormalizedAbsolutePath(inputFile);

		if (!Files.exists(inputPath)) {
			throw new CommandLineExportParser.MissingArgumentException("Input file does not exist: " + inputFile);
		}

		if (!Files.isRegularFile(inputPath)) {
			throw new CommandLineExportParser.MissingArgumentException("Input path is not a file: " + inputFile);
		}

		if (usedPaths.add(inputPath)) {
			inputFiles.add(inputPath.toUri());
		}
	}

	/**
	 * Checks whether the wildcard is present.
	 *
	 * @param value value to process
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	private static boolean containsWildcard(final String value) {
		return value != null && CommandLineExporter.firstWildcardIndex(value) >= 0;
	}

	/**
	 * Creates a canvases.
	 *
	 * @param document            document to read or modify
	 * @param requestedPanelTypes values for requested panel types
	 * @return the created canvases
	 */
	private static Map<PanelType, DiagramCanvas> createCanvases(final ModelDocument document, final Set<PanelType> requestedPanelTypes) {
		final Map<PanelType, DiagramCanvas> canvases = new LinkedHashMap<>();

		final Set<PanelType> panelTypes = requestedPanelTypes == null || requestedPanelTypes.isEmpty() ? EnumSet.allOf(PanelType.class)
				: requestedPanelTypes;

		for (final PanelType panelType : panelTypes) {
			final DiagramCanvas canvas = new DiagramCanvas(null, document, panelType, DocumentChangeListener.NOOP);

			if (canvas != null) {
				canvas.revalidate();
				canvas.doLayout();

				canvases.put(panelType, canvas);
			}
		}

		return canvases;
	}

	/**
	 * Creates a wildcard matchers.
	 *
	 * @param pattern pattern used for matching or formatting
	 * @return the created wildcard matchers
	 */
	private static List<PathMatcher> createWildcardMatchers(final String pattern) {
		return CommandLineExporter.expandDoubleStarZeroDirectoryVariants(pattern)
				.stream()
				.map(glob -> FileSystems.getDefault().getPathMatcher("glob:" + glob))
				.toList();
	}

	/**
	 * Expands the double star zero directory variants.
	 *
	 * @param pattern pattern used for matching or formatting
	 * @return the matching values
	 */
	private static List<String> expandDoubleStarZeroDirectoryVariants(final String pattern) {
		final Set<String> variants = new LinkedHashSet<>();
		CommandLineExporter.expandDoubleStarZeroDirectoryVariants(pattern, variants);
		return new ArrayList<>(variants);
	}

	/**
	 * Expands the double star zero directory variants.
	 *
	 * @param pattern  pattern used for matching or formatting
	 * @param variants variants value used by the operation
	 */
	private static void expandDoubleStarZeroDirectoryVariants(final String pattern, final Set<String> variants) {
		if (!variants.add(pattern)) {
			return;
		}

		final String separator = File.separator;
		final String leadingDoubleStar = "**" + separator;

		if (pattern.startsWith(leadingDoubleStar)) {
			CommandLineExporter.expandDoubleStarZeroDirectoryVariants(pattern.substring(leadingDoubleStar.length()), variants);
		}

		final String middleDoubleStar = separator + "**" + separator;
		int index = pattern.indexOf(middleDoubleStar);

		while (index >= 0) {
			final String withoutDoubleStar = pattern.substring(0, index + separator.length())
					+ pattern.substring(index + middleDoubleStar.length());

			CommandLineExporter.expandDoubleStarZeroDirectoryVariants(withoutDoubleStar, variants);
			index = pattern.indexOf(middleDoubleStar, index + 1);
		}
	}

	/**
	 * Finds the wildcard search root that matches the supplied input.
	 *
	 * @param pattern pattern used for matching or formatting
	 * @return the matching wildcard search root, or {@code null} when no match exists
	 * @throws URISyntaxException
	 */
	private static Path findWildcardSearchRoot(final String pattern) throws URISyntaxException {
		final int firstWildcardIndex = CommandLineExporter.firstWildcardIndex(pattern);

		if (firstWildcardIndex < 0) {
			return CommandLineExporter.toNormalizedAbsolutePath(new URI(pattern));
		}

		final int lastSeparatorBeforeWildcard = pattern.lastIndexOf(File.separatorChar, firstWildcardIndex);
		final String rootText;

		if (lastSeparatorBeforeWildcard < 0) {
			rootText = ".";
		} else if (lastSeparatorBeforeWildcard == 0) {
			rootText = File.separator;
		} else {
			rootText = pattern.substring(0, lastSeparatorBeforeWildcard);
		}

		return CommandLineExporter.toNormalizedAbsolutePath(new URI(rootText));
	}

	/**
	 * Returns the first wildcard index.
	 *
	 * @param value value to process
	 * @return the first wildcard index result
	 */
	private static int firstWildcardIndex(final String value) {
		final int starIndex = value.indexOf('*');
		final int questionIndex = value.indexOf('?');

		if (starIndex < 0) {
			return questionIndex;
		}
		if (questionIndex < 0) {
			return starIndex;
		}

		return Math.min(starIndex, questionIndex);
	}

	/**
	 * Checks whether the wildcard matches the expected pattern.
	 *
	 * @param path            file system path to read or write
	 * @param absolutePattern whether absolute pattern is enabled
	 * @param baseDirectory   base directory value used by the operation
	 * @param matchers        values for matchers
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	private static boolean
			matchesWildcard(final Path path, final boolean absolutePattern, final Path baseDirectory, final List<PathMatcher> matchers) {

		final Path absolutePath = path.toAbsolutePath().normalize();
		final Path pathToMatch = absolutePattern ? absolutePath : baseDirectory.relativize(absolutePath);

		for (final PathMatcher matcher : matchers) {
			if (matcher.matches(pathToMatch)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Normalizes the wildcard separators.
	 *
	 * @param value value to process
	 * @return the normalize wildcard separators result
	 */
	private static String normalizeWildcardSeparators(final String value) {
		return value.replace('/', File.separatorChar).replace('\\', File.separatorChar);
	}

	/**
	 * Resolves the input files from the current model and layout state.
	 *
	 * @param rawInputFile file to read or write
	 * @param multiple     whether multiple input files are allowed
	 * @param wildcard     whether wildcard path matching is enabled
	 * @return the resolved input files
	 * @throws IOException        if the operation cannot be completed
	 * @throws URISyntaxException
	 */
	private static List<URI> resolveInputFiles(final String rawInputFile, final boolean multiple, final boolean wildcard)
			throws IOException,
				URISyntaxException {
		final String rawInput = rawInputFile == null ? "" : rawInputFile.trim();

		if (rawInput.isBlank()) {
			throw new CommandLineExportParser.MissingArgumentException("Missing required argument: --export <file>");
		}

		final List<String> entries = multiple
				? Arrays.stream(rawInput.split(",")).map(String::trim).filter(entry -> !entry.isBlank()).toList()
				: List.of(rawInput.trim());

		if (entries.isEmpty()) {
			throw new CommandLineExportParser.MissingArgumentException("No input files selected.");
		}

		final Set<Path> usedPaths = new LinkedHashSet<>();
		final List<URI> inputFiles = new ArrayList<>();

		for (final String entry : entries) {
			if (wildcard && CommandLineExporter.containsWildcard(entry)) {
				final List<Path> matchedPaths = CommandLineExporter.resolveWildcardInputFiles(entry);

				if (matchedPaths.isEmpty()) {
					throw new CommandLineExportParser.MissingArgumentException("No input files matched wildcard pattern: " + entry);
				}

				for (final Path matchedPath : matchedPaths) {
					CommandLineExporter.addInputFile(inputFiles, usedPaths, matchedPath.toUri());
				}
			} else {
				CommandLineExporter.addInputFile(inputFiles, usedPaths, CommandLineExportParser.resolveHome(entry).toUri());
			}
		}

		if (inputFiles.isEmpty()) {
			throw new CommandLineExportParser.MissingArgumentException("No input files selected.");
		}

		return inputFiles;
	}

	/**
	 * Resolves the wildcard input files from the current model and layout state.
	 *
	 * @param rawPattern text value for raw pattern
	 * @return the resolved wildcard input files
	 * @throws IOException        if the operation cannot be completed
	 * @throws URISyntaxException
	 */
	private static List<Path> resolveWildcardInputFiles(final String rawPattern) throws IOException, URISyntaxException {
		final String pattern = CommandLineExporter.normalizeWildcardSeparators(CommandLineExportParser.resolveHome(rawPattern).toString());
		final Path searchRoot = CommandLineExporter.findWildcardSearchRoot(pattern);

		if (!Files.exists(searchRoot) || !Files.isDirectory(searchRoot)) {
			return List.of();
		}

		final boolean absolutePattern = Paths.get(pattern).isAbsolute();
		final Path baseDirectory = Paths.get("").toAbsolutePath().normalize();
		final List<PathMatcher> matchers = CommandLineExporter.createWildcardMatchers(pattern);

		try (Stream<Path> stream = Files.walk(searchRoot)) {
			return stream.filter(Files::isRegularFile)
					.filter(path -> CommandLineExporter.matchesWildcard(path, absolutePattern, baseDirectory, matchers))
					.sorted()
					.toList();
		}
	}

	/**
	 * Converts the input to a normalized absolute path.
	 *
	 * @param file file to read or write
	 * @return the to normalized absolute path result
	 */
	private static Path toNormalizedAbsolutePath(final URI file) {
		try {
			return Paths.get(file).normalize();
		} catch (final InvalidPathException ex) {
			throw new CommandLineExportParser.InvalidArgumentException("Invalid input path: " + file, ex);
		}
	}

	/**
	 * Creates a command line exporter instance.
	 */
	private CommandLineExporter() {
	}

}
