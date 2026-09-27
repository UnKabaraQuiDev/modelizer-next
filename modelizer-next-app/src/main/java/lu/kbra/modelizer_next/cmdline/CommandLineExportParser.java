package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.model_exporter.api.OptionsManager;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.pclib.PCUtils;

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
		boolean batch = false;

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
			case "-b", "--batch" -> batch = true;
			case "-h", "--help" -> {
				CommandLineExportParser.printHelp();
				if (args.length > i) {
					final ModelExporter me = CommandLineExportParser.parseFormat(args[i + 1]);
					CommandLineExportParser.printOptions(args[i + 1].toLowerCase(),
							me.getOptionsManager().getClassType(),
							me.getOptionsManager().blankOptions());
				}
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

		final ExporterOptions config = CommandLineExportParser.loadConfig(configFile, args, index, exporter.getOptionsManager());

		return new CommandLineExportOptions(inputFile,
				exporter,
				outputDirectory,
				force,
				multiple,
				wildcard,
				jobCount,
				config,
				configFile,
				batch);
	}

	public static void printOptions(final String exporter, final Class<?> optionsClass, final Object options) {
		final ObjectMapper mapper = MNMain.OBJECT_MAPPER;
		final JavaType javaType = mapper.getTypeFactory().constructType(optionsClass);
		final BeanDescription description = mapper.getSerializationConfig().introspect(javaType);

		final JsonNode optionsNode = mapper.valueToTree(options);

		final List<String[]> rows = new ArrayList<>();

		for (final BeanPropertyDefinition property : description.findProperties()) {

			final String name = property.getName();
			final String type = property.getPrimaryType().getRawClass().getSimpleName();

			final JsonNode value = optionsNode.get(name);
			final String defaultValue = value == null ? "null" : value.toString();

			rows.add(new String[] { name, type, defaultValue });
		}

		int nameWidth = "Name".length();
		int typeWidth = "Type".length();
		int defaultWidth = "Default".length();

		for (final String[] row : rows) {
			nameWidth = Math.max(nameWidth, row[0].length());
			typeWidth = Math.max(typeWidth, row[1].length());
			defaultWidth = Math.max(defaultWidth, row[2].length());
		}

		System.out.println("Available options for: " + exporter);
		final String[] header = { "Name", "Type", "Default" };
		System.out.print(PCUtils.formatTable(header, rows.toArray(String[][]::new)));
	}

	private static ExporterOptions loadConfig(final URI configFile, final String[] args, final int index, final OptionsManager exporter)
			throws IOException {
		final ObjectNode configNode = MNMain.OBJECT_MAPPER.createObjectNode();

		if (configFile != null) {
			final JsonNode fileNode = MNMain.OBJECT_MAPPER.readTree(new File(configFile));

			if (!fileNode.isObject()) {
				throw new IllegalArgumentException("Config file must contain a JSON object.");
			}

			configNode.setAll((ObjectNode) fileNode);
		}

		if (index > -1) {
			final ObjectNode argsNode = ArgsMapper.parseNode(Arrays.copyOfRange(args, index + 1, args.length), MNMain.OBJECT_MAPPER);

			configNode.setAll(argsNode);
		}

		final ExporterOptions config;

		if (configNode.isEmpty()) {
			config = exporter.blankOptions();
		} else {
			try {
				config = MNMain.OBJECT_MAPPER.treeToValue(configNode, exporter.getClassType());
			} catch (final Exception e) {
				throw new IllegalArgumentException("Could not convert options to " + exporter.getClassType().getSimpleName(), e);
			}
		}

		return config;
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
				  -h, --help [<exporter>]    Print this help or configuration options for the selected exporter
				  -m, --multiple             Multiple input files, separated by commas "path1,path2,path3..."
				  -w, --wildcard             Enable wildcard support for input files, supports: *, **, ?
				  -j, --jobs <count>         Dispatch multiple threads to speed up the export process
				  -b, --batch                Disables interactive mode, the output won't contains ANSI control characters

				Examples:
				  modelizer-next -h png
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
