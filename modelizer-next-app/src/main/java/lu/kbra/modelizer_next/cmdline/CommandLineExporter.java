package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.export.ViewExportRequest;
import lu.kbra.modelizer_next.ui.export.ViewExporter;
import lu.kbra.modelizer_next.ui.frame.DocumentSession;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;
import lu.kbra.pclib.datastructure.triplet.Triplet;
import lu.kbra.pclib.pointer.prim.IntPointer;

/**
 * Runs document exports without opening the interactive desktop frame.
 */
public final class CommandLineExporter {

	/**
	 * Immutable value object for loaded document data.
	 *
	 * @param sourceFile file to read or write
	 * @param document   document to read or modify
	 */
	private record LoadedDocument(File sourceFile, ModelDocument document) {
	}

	/**
	 * Defines operations for model document producer behavior.
	 */
	@FunctionalInterface
	private interface ModelDocumentProducer {

		/**
		 * Returns the next value from this producer or iterator.
		 *
		 * @return an optional result when a matching value is available
		 * @throws IOException if the operation cannot be completed
		 */
		Optional<LoadedDocument> next() throws IOException;

	}

	/**
	 * Represents an input file document producer in the command-line export part of the application.
	 */
	private static final class InputFileDocumentProducer implements ModelDocumentProducer {

		private final Iterator<File> inputFiles;
		private final ConsoleDocumentLoadHandler loadHandler;

		/**
		 * Creates an input file document producer instance.
		 *
		 * @param inputFiles values for input files
		 * @param force      whether force is enabled
		 */
		private InputFileDocumentProducer(final List<File> inputFiles, final boolean force) {
			this.inputFiles = inputFiles.iterator();
			this.loadHandler = new ConsoleDocumentLoadHandler(force);
		}

		/**
		 * Returns the next value from this producer or iterator.
		 *
		 * @return an optional result when a matching value is available
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public Optional<LoadedDocument> next() throws IOException {
			if (!this.inputFiles.hasNext()) {
				return Optional.empty();
			}

			final File inputFile = this.inputFiles.next();
			final Optional<DocumentSession> session = MainFrame.createDocument(inputFile, this.loadHandler);

			if (session.isEmpty()) {
				throw new ExportAbortedException(inputFile);
			}

			return Optional.of(new LoadedDocument(inputFile, session.get().getDocument()));
		}

	}

	/**
	 * Exception raised when export aborted fails.
	 */
	private static final class ExportAbortedException extends IOException {

		private static final long serialVersionUID = 513442251233551029L;

		/**
		 * Creates an export aborted exception instance.
		 *
		 * @param inputFile file to read or write
		 */
		private ExportAbortedException(final File inputFile) {
			super("Export aborted for input file: " + inputFile);
		}

	}

	/**
	 * Runs the full operation represented by this class.
	 *
	 * @param args command-line arguments supplied by the launcher
	 * @return the run result
	 */
	public static int run(final String[] args) {
		System.setProperty("java.awt.headless", "true");

		try {
			final CommandLineExportOptions options = CommandLineExportParser.parse(args);
			final List<File> inputFiles = CommandLineExporter
					.resolveInputFiles(options.inputFile(), options.multiple(), options.wildcard());
			final ModelDocumentProducer documentProducer = new InputFileDocumentProducer(inputFiles, options.force());
			final ViewExportRequest request = new ViewExportRequest(options
					.format(), options.scope(), options.panelTypes(), options.outputDirectory(), options.fileNamePattern(), false, false);

			final IntPointer exportedFileCount = new IntPointer(0);

			final int jobCount = options.jobCount();
			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(jobCount);
			final List<Exception> caughtException = Collections.synchronizedList(new ArrayList<>());

			Optional<LoadedDocument> loadedDocument;
			while ((loadedDocument = documentProducer.next()).isPresent()) {
				final LoadedDocument doc = loadedDocument.get();
				executor.submit(() -> {
					try {
						final Map<PanelType, DiagramCanvas> canvases = CommandLineExporter.createCanvases(doc.document(),
								options.panelTypes());

						if (canvases.isEmpty()) {
							System.err.println("Nothing to export for: " + doc.sourceFile().getPath());
							return;
						}

						final List<Triplet<Optional<File>, PanelType, File>> exportedFiles = ViewExporter.exportViews(canvases,
								request,
								Optional.of(doc.sourceFile()),
								(triplet) -> System.out.println("" + triplet.getFirst().map(File::getPath).orElse("?") + "\t"
										+ triplet.getSecond().name() + "\t" + triplet.getThird().getPath()));

						exportedFileCount.add(exportedFiles.size());
					} catch (Exception e) {
						caughtException.add(e);
						executor.shutdownNow();
					}
				});
			}

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);

			if (!caughtException.isEmpty()) {
				System.err.println("Got: " + caughtException.size() + " errors.");
				caughtException.forEach(e -> e.printStackTrace(System.err));
				return 1;
			}

			if (exportedFileCount.get() == 0) {
				System.err.println("Nothing to export.");
				return 3;
			}

			System.out.println("Exported " + exportedFileCount.get() + " images.");

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
	 * Resolves the input files from the current model and layout state.
	 *
	 * @param rawInputFile file to read or write
	 * @param multiple     whether multiple input files are allowed
	 * @param wildcard     whether wildcard path matching is enabled
	 * @return the resolved input files
	 * @throws IOException if the operation cannot be completed
	 */
	private static List<File> resolveInputFiles(final String rawInputFile, final boolean multiple, final boolean wildcard)
			throws IOException {
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
		final List<File> inputFiles = new ArrayList<>();

		for (final String entry : entries) {
			if (wildcard && CommandLineExporter.containsWildcard(entry)) {
				final List<Path> matchedPaths = CommandLineExporter.resolveWildcardInputFiles(entry);

				if (matchedPaths.isEmpty()) {
					throw new CommandLineExportParser.MissingArgumentException("No input files matched wildcard pattern: " + entry);
				}

				for (final Path matchedPath : matchedPaths) {
					CommandLineExporter.addInputFile(inputFiles, usedPaths, matchedPath.toFile());
				}
			} else {
				CommandLineExporter.addInputFile(inputFiles, usedPaths, CommandLineExportParser.resolveHome(entry).toFile());
			}
		}

		if (inputFiles.isEmpty()) {
			throw new CommandLineExportParser.MissingArgumentException("No input files selected.");
		}

		return inputFiles;
	}

	/**
	 * Adds the input file.
	 *
	 * @param inputFiles values for input files
	 * @param usedPaths  used paths value used by the operation
	 * @param inputFile  file to read or write
	 */
	private static void addInputFile(final List<File> inputFiles, final Set<Path> usedPaths, final File inputFile) {
		final Path inputPath = CommandLineExporter.toNormalizedAbsolutePath(inputFile);

		if (!Files.exists(inputPath)) {
			throw new CommandLineExportParser.MissingArgumentException("Input file does not exist: " + inputFile);
		}

		if (!Files.isRegularFile(inputPath)) {
			throw new CommandLineExportParser.MissingArgumentException("Input path is not a file: " + inputFile);
		}

		if (usedPaths.add(inputPath)) {
			inputFiles.add(inputPath.toFile());
		}
	}

	/**
	 * Resolves the wildcard input files from the current model and layout state.
	 *
	 * @param rawPattern text value for raw pattern
	 * @return the resolved wildcard input files
	 * @throws IOException if the operation cannot be completed
	 */
	private static List<Path> resolveWildcardInputFiles(final String rawPattern) throws IOException {
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
	 */
	private static Path findWildcardSearchRoot(final String pattern) {
		final int firstWildcardIndex = CommandLineExporter.firstWildcardIndex(pattern);

		if (firstWildcardIndex < 0) {
			return CommandLineExporter.toNormalizedAbsolutePath(new File(pattern));
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

		return CommandLineExporter.toNormalizedAbsolutePath(new File(rootText));
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
	 * Checks whether the wildcard is present.
	 *
	 * @param value value to process
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	private static boolean containsWildcard(final String value) {
		return value != null && CommandLineExporter.firstWildcardIndex(value) >= 0;
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
	 * Converts the input to a normalized absolute path.
	 *
	 * @param file file to read or write
	 * @return the to normalized absolute path result
	 */
	private static Path toNormalizedAbsolutePath(final File file) {
		try {
			return file.toPath().toAbsolutePath().normalize();
		} catch (final InvalidPathException ex) {
			throw new CommandLineExportParser.InvalidArgumentException("Invalid input path: " + file, ex);
		}
	}

	/**
	 * Creates a canvases.
	 *
	 * @param document            document to read or modify
	 * @param requestedPanelTypes values for requested panel types
	 * @return the created canvases
	 */
	private static Map<PanelType, DiagramCanvas> createCanvases(final ModelDocument document, final List<PanelType> requestedPanelTypes) {
		final Map<PanelType, DiagramCanvas> canvases = new LinkedHashMap<>();

		final List<PanelType> panelTypes = requestedPanelTypes == null || requestedPanelTypes.isEmpty() ? List.of(PanelType.values())
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
	 * Creates a command line exporter instance.
	 */
	private CommandLineExporter() {
	}

}
