package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;
import lu.kbra.modelizer_next.ui.export.ViewExporter;

public final class CommandLineExportParser {

	public static class InvalidArgumentException extends RuntimeException {

		public InvalidArgumentException(String message, Throwable cause) {
			super(message, cause);
		}

		public InvalidArgumentException(String message) {
			super(message);
		}

	}

	private CommandLineExportParser() {
	}

	public static boolean isExportRequest(final String[] args) {
		return Arrays.stream(args).anyMatch(arg -> "--export".equals(arg) || "-e".equals(arg) || "--help".equals(arg) || "-h".equals(arg));
	}

	public static CommandLineExportOptions parse(final String[] args) throws IOException {
		File inputFile = null;
		ViewExportFormat format = null;
		ViewExportScope scope = ViewExportScope.EVERYTHING;
		List<PanelType> panelTypes = List.of();
		File outputDirectory = new File(".");
		String fileNamePattern = ViewExporter.DEFAULT_FILE_PATTERN;
		boolean force = false;

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];

			switch (arg) {
			case "-e", "--export" -> inputFile = new File(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-t", "--type" -> format = CommandLineExportParser.parseFormat(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-s", "--scope" -> scope = CommandLineExportParser.parseScope(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-p", "--panels" -> panelTypes = CommandLineExportParser.parsePanels(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-o", "--out" -> outputDirectory = new File(CommandLineExportParser.requireValue(args, ++i, arg));
			case "-n", "--pattern" -> fileNamePattern = CommandLineExportParser.requireValue(args, ++i, arg);
			case "-f", "--force" -> force = true;
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
			throw new MissingArgumentException("Missing required argument: --type <svg|png>");
		}

		if (!inputFile.exists()) {
			throw new MissingArgumentException("Input file does not exist: " + inputFile);
		}

		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new MissingArgumentException("Could not create output directory: " + outputDirectory);
		}

		if (panelTypes.isEmpty()) {
			throw new MissingArgumentException("Missing required argument: --panels <conceptual,logical,physical>");
		}

		return new CommandLineExportOptions(inputFile, format, scope, panelTypes, outputDirectory, fileNamePattern, force);
	}

	private static String requireValue(final String[] args, final int index, final String option) throws IOException {
		if (index >= args.length) {
			throw new MissingArgumentException("Missing value for " + option);
		}

		return args[index];
	}

	private static ViewExportFormat parseFormat(final String value) {
		for (final ViewExportFormat format : ViewExportFormat.values()) {
			if (format.getExtension().equalsIgnoreCase(value) || format.name().equalsIgnoreCase(value)) {
				return format;
			}
		}

		throw new MissingArgumentException("Unsupported export type: " + value);
	}

	private static ViewExportScope parseScope(final String value) {
		return switch (value.toLowerCase()) {
		case "selection" -> ViewExportScope.SELECTION;
		case "view" -> ViewExportScope.VIEW;
		case "everything", "all" -> ViewExportScope.EVERYTHING;
		default -> throw new MissingArgumentException("Unsupported export scope: " + value);
		};
	}

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

	public static void printHelp() {
		System.out.println("""
				Usage:
				  modelizer --export <file> --type <svg|png> [options]

				Options:
				  -e, --export <file>        File to load and export
				  -t, --type <svg|png>       Export format
				  -o, --out <directory>      Output directory, default: current directory
				  -s, --scope <scope>        selection|view|everything, default: everything
				  -p, --panels <list>        Comma-separated PanelType names: conceptual (c), logical (l), physical (p)
				  -n, --pattern <pattern>    File name pattern, default: '%DEFAULT_FILE_PATTER%', available: %FILE_PATTERN_TOKENS%
				  -f, --force                Continue on legacy/newer-version warnings
				  -h, --help                 Print this help
				""".replace("%DEFAULT_FILE_PATTER%", ViewExporter.DEFAULT_FILE_PATTERN)
				.replace("%FILE_PATTERN_TOKENS%", ViewExporter.FILE_PATTERN_TOKENS.stream().collect(Collectors.joining(", "))));
	}

	public static final class HelpRequestedException extends IOException {

		private static final long serialVersionUID = -6864019187574255936L;

		public HelpRequestedException() {
		}

		public HelpRequestedException(String message) {
			super(message);
		}

	}

	public static class MissingArgumentException extends RuntimeException {

		public MissingArgumentException(String message) {
			super(message);
		}

	}

}
