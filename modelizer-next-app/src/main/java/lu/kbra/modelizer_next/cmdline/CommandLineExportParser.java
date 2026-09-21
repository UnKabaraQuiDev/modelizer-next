package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.modelizer_next.MNMain;

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

	private static final String exporterOptions = Exporters.getModelExporters()
			.stream()
			.map(ModelExporter::getExporterId)
			.collect(Collectors.joining("|"));

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
		ModelExporter exporter = null;
		File outputDirectory = new File(".");
		URI configFile = null;
		boolean force = false;
		boolean multiple = false;
		boolean wildcard = false;
		int jobCount = 1;

		int index = -1;
		for (int i = 0; i < args.length && index == -1; i++) {
			final String arg = args[i];

			switch (arg) {
			case "-e", "--export" -> inputFile = CommandLineExportParser.requireValue(args, ++i, arg);
			case "-t", "--type" -> exporter = CommandLineExportParser.parseFormat(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-o", "--out" ->
				outputDirectory = CommandLineExportParser.resolveHome(CommandLineExportParser.requireValue(args, ++i, arg)).toFile();
			case "-c", "--config" ->
				configFile = CommandLineExportParser.resolveHome(CommandLineExportParser.requireValue(args, ++i, arg)).toUri();
			case "-f", "--force" -> force = true;
			case "-m", "--multiple" -> multiple = true;
			case "-w", "--wildcard" -> wildcard = true;
			case "-j", "--jobs" -> jobCount = Integer.parseInt(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-h", "--help" -> {
				CommandLineExportParser.printHelp();
				throw new HelpRequestedException();
			}
			case "--" -> index = i;
			default -> throw new MissingArgumentException("Unknown argument: " + arg);
			}
		}

		if (inputFile == null) {
			throw new MissingArgumentException("Missing required argument: --export <file>");
		}

		if (exporter == null) {
			throw new MissingArgumentException("Missing required argument: --type <" + CommandLineExportParser.exporterOptions + ">");
		}

		if (!multiple && !wildcard && !CommandLineExportParser.resolveHome(inputFile).toFile().exists()) {
			throw new MissingArgumentException("Input file does not exist: " + inputFile);
		}

		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new MissingArgumentException("Could not create output directory: " + outputDirectory);
		}

		if (configFile != null && !new File(configFile).exists()) {
			throw new MissingArgumentException("Configuration file does not exist: " + configFile);
		}

		if (jobCount <= 0) {
			throw new IllegalArgumentException("Job count cannot be zero or negative.");
		}

		final ExporterOptions config;
		if (configFile != null && index > -1) {
			throw new IllegalArgumentException("Cannot specify --config and manual -- inline options.");
		} else if (configFile != null) {
			config = MNMain.OBJECT_MAPPER.readValue(new File(configFile), exporter.getOptionsManager().getClassType());
		} else if (index > -1) {
			config = ArgsMapper
					.parse(Arrays.copyOfRange(args, index, args.length), exporter.getOptionsManager().getClassType(), MNMain.OBJECT_MAPPER);
		} else {
			config = exporter.getOptionsManager().blankOptions();
		}

		return new CommandLineExportOptions(inputFile, exporter, outputDirectory, force, multiple, wildcard, jobCount, config, configFile);
	}

	/**
	 * Prints the help.
	 */
	public static void printHelp() {
		System.out.println("""
				Usage:
				  modelizer-next --export <file> --type <%EXPORTERS%> ([other options]) (-- [specific configurations])

				Options:
				  -e, --export <file>        File to load and export
				  -t, --type <%EXPORTERS%>   Export format
				  -c, --config <file>        Export configuratio file
				  -o, --out <directory>      Output directory, default: current directory
				  -f, --force                Continue on legacy/newer-version warnings
				  -h, --help                 Print this help
				  -m, --multiple             Multiple input files, separated by commas "path1,path2,path3..."
				  -w, --wildcard             Enable wildcard support for input files, supports: *, **, ?
				  -j, --jobs <count>         Dispatch multiple threads to speed up the export process

				Examples:
				  modelizer-next -e *.mn -w -t png
				  modelizer-next -e oneFile.mn -t png -c png-export.mnie -- panels=c,l
				  modelizer-next -e *.mn -w -t svg -- backgroundColor=#aabbcc
				""".replace("%EXPORTERS%", CommandLineExportParser.exporterOptions));
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
	private static ModelExporter parseFormat(final String value) {
		for (final ModelExporter format : Exporters.getModelExporters()) {
			if (format.getExporterId().equalsIgnoreCase(value)) {
				return format;
			}
		}

		throw new MissingArgumentException("Unsupported export type: " + value);
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
