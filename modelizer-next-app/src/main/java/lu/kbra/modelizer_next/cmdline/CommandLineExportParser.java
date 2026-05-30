package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;
import lu.kbra.modelizer_next.ui.export.ViewExporter;

/**
 * Parses and validates command-line arguments for unattended exports.
 */
public final class CommandLineExportParser {

	/**
	 * Exception raised when help requested fails.
	 */
	public static final class HelpRequestedException extends IOException {

		private static final long serialVersionUID = -6864019187574255936L;

		/**
		 * Creates a help requested exception instance.
		 */
		public HelpRequestedException() {
		}

		/**
		 * Creates a help requested exception instance.
		 *
		 * @param message message shown to the caller or user
		 */
		public HelpRequestedException(final String message) {
			super(message);
		}

	}

	/**
	 * Exception raised when invalid argument fails.
	 */
	public static class InvalidArgumentException extends RuntimeException {

		private static final long serialVersionUID = 5775841553077652888L;

		/**
		 * Creates an invalid argument exception instance.
		 *
		 * @param message message shown to the caller or user
		 */
		public InvalidArgumentException(final String message) {
			super(message);
		}

		/**
		 * Creates an invalid argument exception instance.
		 *
		 * @param message message shown to the caller or user
		 * @param cause   cause to attach to the created exception
		 */
		public InvalidArgumentException(final String message, final Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * Exception raised when missing argument fails.
	 */
	public static class MissingArgumentException extends RuntimeException {

		private static final long serialVersionUID = -2849535395312148162L;

		/**
		 * Creates a missing argument exception instance.
		 *
		 * @param message message shown to the caller or user
		 */
		public MissingArgumentException(final String message) {
			super(message);
		}

	}

	/**
	 * Checks whether export request is enabled or applies.
	 *
	 * @param args command-line arguments supplied by the launcher
	 * @return {@code true} if export request is enabled or applies; otherwise {@code false}
	 */
	public static boolean isExportRequest(final String[] args) {
		return Arrays.stream(args).anyMatch(arg -> "--export".equals(arg) || "-e".equals(arg) || "--help".equals(arg) || "-h".equals(arg));
	}

	/**
	 * Parses the supplied text into the value type used by this class.
	 *
	 * @param args command-line arguments supplied by the launcher
	 * @return the parsed value
	 * @throws IOException if the operation cannot be completed
	 */
	public static CommandLineExportOptions parse(final String[] args) throws IOException {
		String inputFile = null;
		ViewExportFormat format = null;
		ViewExportScope scope = ViewExportScope.EVERYTHING;
		List<PanelType> panelTypes = List.of();
		File outputDirectory = new File(".");
		String fileNamePattern = ViewExporter.DEFAULT_FILE_PATTERN;
		boolean force = false;
		boolean multiple = false;
		boolean wildcard = false;
		int jobCount = 1;

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];

			switch (arg) {
			case "-e", "--export" -> inputFile = CommandLineExportParser.requireValue(args, ++i, arg);
			case "-t", "--type" -> format = CommandLineExportParser.parseFormat(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-s", "--scope" -> scope = CommandLineExportParser.parseScope(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-p", "--panels" -> panelTypes = CommandLineExportParser.parsePanels(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-o", "--out" ->
				outputDirectory = CommandLineExportParser.resolveHome(CommandLineExportParser.requireValue(args, ++i, arg)).toFile();
			case "-n", "--pattern" -> fileNamePattern = CommandLineExportParser.requireValue(args, ++i, arg);
			case "-f", "--force" -> force = true;
			case "-m", "--multiple" -> multiple = true;
			case "-w", "--wildcard" -> wildcard = true;
			case "-j", "--jobs" -> jobCount = Integer.parseInt(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-h", "--help" -> {
				CommandLineExportParser.printHelp();
				throw new HelpRequestedException();
			}
			default -> throw new MissingArgumentException("Unknown argument: " + arg);
			}
		}

		if (inputFile == null) {
			throw new MissingArgumentException("Missing required argument: --export <file>");
		}

		if (format == null) {
			throw new MissingArgumentException("Missing required argument: --type <png|jpg|bmp|tiff|webp|pdf>");
		}

		if (!multiple && !wildcard && !CommandLineExportParser.resolveHome(inputFile).toFile().exists()) {
			throw new MissingArgumentException("Input file does not exist: " + inputFile);
		}

		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new MissingArgumentException("Could not create output directory: " + outputDirectory);
		}

		if (panelTypes.isEmpty()) {
			throw new MissingArgumentException("Missing required argument: --panels <conceptual,logical,physical>");
		}

		if (jobCount <= 0) {
			throw new IllegalArgumentException("Job count cannot be zero or negative.");
		}

		return new CommandLineExportOptions(inputFile,
				format,
				scope,
				panelTypes,
				outputDirectory,
				fileNamePattern,
				force,
				multiple,
				wildcard,
				jobCount);
	}

	/**
	 * Prints the help.
	 */
	public static void printHelp() {
		System.out.println("""
				Usage:
				  modelizer --export <file> --type <png|jpg|bmp|tiff|webp|pdf> [options]

				Options:
				  -e, --export <file>        File to load and export
				  -t, --type <png|jpg|bmp|tiff|webp|pdf>       Export format
				  -o, --out <directory>      Output directory, default: current directory
				  -s, --scope <scope>        selection (s), view (v), everything/all (a), default: everything
				  -p, --panels <list>        Comma-separated PanelType names: conceptual (c), logical (l), physical (p)
				  -n, --pattern <pattern>    File name pattern, default: '%DEFAULT_FILE_PATTER%', available: %FILE_PATTERN_TOKENS%
				  -f, --force                Continue on legacy/newer-version warnings
				  -h, --help                 Print this help
				  -m, --multiple             Multiple input files, separated by commas "path1,path2,path3..."
				  -w, --wildcard             Enable wildcard support for input files, supports: *, **, ?
				  -j, --jobs <count>         Dispatch multiple threads to speed up the export process
				""".replace("%DEFAULT_FILE_PATTER%", ViewExporter.DEFAULT_FILE_PATTERN)
				.replace("%FILE_PATTERN_TOKENS%", ViewExporter.FILE_PATTERN_TOKENS.stream().collect(Collectors.joining(", "))));
	}

	/**
	 * Resolves the home from the current model and layout state.
	 *
	 * @param path file system path to read or write
	 * @return the resolved home
	 */
	public static Path resolveHome(String path) {
		if (path.startsWith("~")) {
			path = System.getProperty("user.home") + path.substring(1);
		}
		return Paths.get(path).normalize();
	}

	/**
	 * Parses the format from the supplied input.
	 *
	 * @param value value to process
	 * @return the parsed format
	 */
	private static ViewExportFormat parseFormat(final String value) {
		for (final ViewExportFormat format : ViewExportFormat.values()) {
			if (format.getExtension().equalsIgnoreCase(value) || format.name().equalsIgnoreCase(value)) {
				return format;
			}
		}

		throw new MissingArgumentException("Unsupported export type: " + value);
	}

	/**
	 * Parses the panels from the supplied input.
	 *
	 * @param value value to process
	 * @return the parsed panels
	 */
	private static List<PanelType> parsePanels(final String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}

		final List<PanelType> result = new ArrayList<>();

		for (final String rawPanel : value.split(",")) {
			final String panel = rawPanel.trim().toUpperCase();

			try {
				if (panel.length() == 1) {
					result.add(switch (panel.charAt(0)) {
					case 'C' -> PanelType.CONCEPTUAL;
					case 'L' -> PanelType.LOGICAL;
					case 'P' -> PanelType.PHYSICAL;
					default -> throw new InvalidArgumentException("Unsupported panel type: " + panel);
					});
				} else {
					result.add(PanelType.valueOf(panel));
				}
			} catch (final IllegalArgumentException ex) {
				throw new InvalidArgumentException("Unsupported panel type: " + panel, ex);
			}
		}

		return result.stream().distinct().toList();
	}

	/**
	 * Parses the scope from the supplied input.
	 *
	 * @param value value to process
	 * @return the parsed scope
	 */
	private static ViewExportScope parseScope(final String value) {
		return switch (value.toLowerCase()) {
		case "selection", "e" -> ViewExportScope.SELECTION;
		case "view", "v" -> ViewExportScope.VIEW;
		case "everything", "all", "a" -> ViewExportScope.EVERYTHING;
		default -> throw new MissingArgumentException("Unsupported export scope: " + value);
		};
	}

	/**
	 * Reads and validates the required value.
	 *
	 * @param args   command-line arguments supplied by the launcher
	 * @param index  zero-based index to read or update
	 * @param option text value for option
	 * @return the require value result
	 * @throws IOException if the operation cannot be completed
	 */
	private static String requireValue(final String[] args, final int index, final String option) throws IOException {
		if (index >= args.length) {
			throw new MissingArgumentException("Missing value for " + option);
		}

		return args[index];
	}

	/**
	 * Creates a command line export parser instance.
	 */
	private CommandLineExportParser() {
	}

}
