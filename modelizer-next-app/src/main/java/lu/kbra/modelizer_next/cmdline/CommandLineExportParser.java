package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

public final class CommandLineExportParser {

	private CommandLineExportParser() {
	}

	public static boolean isExportRequest(final String[] args) {
		for (final String arg : args) {
			if ("--export".equals(arg) || "-e".equals(arg)) {
				return true;
			}
		}

		return false;
	}

	public static CommandLineExportOptions parse(final String[] args) throws IOException {
		File inputFile = null;
		ViewExportFormat format = null;
		ViewExportScope scope = ViewExportScope.EVERYTHING;
		List<PanelType> panelTypes = List.of();
		File outputDirectory = new File(".");
		String fileNamePattern = "{source}-{panel}.{ext}";
		boolean force = false;

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];

			switch (arg) {
			case "-e", "--export" -> inputFile = new File(requireValue(args, ++i, arg));
			case "-t", "--type" -> format = parseFormat(requireValue(args, ++i, arg));
			case "-s", "--scope" -> scope = parseScope(requireValue(args, ++i, arg));
			case "-p", "--panels" -> panelTypes = parsePanels(requireValue(args, ++i, arg));
			case "-o", "--out" -> outputDirectory = new File(requireValue(args, ++i, arg));
			case "--pattern" -> fileNamePattern = requireValue(args, ++i, arg);
			case "-f", "--force" -> force = true;
			case "-h", "--help" -> {
				printHelp();
				throw new HelpRequestedException();
			}
			default -> throw new IOException("Unknown argument: " + arg);
			}
		}

		if (inputFile == null) {
			throw new IOException("Missing required argument: --export <file>");
		}

		if (format == null) {
			throw new IOException("Missing required argument: --type <svg|png>");
		}

		if (!inputFile.exists()) {
			throw new IOException("Input file does not exist: " + inputFile);
		}

		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IOException("Could not create output directory: " + outputDirectory);
		}

		return new CommandLineExportOptions(
				inputFile,
				format,
				scope,
				panelTypes,
				outputDirectory,
				fileNamePattern,
				force);
	}

	private static String requireValue(final String[] args, final int index, final String option) throws IOException {
		if (index >= args.length) {
			throw new IOException("Missing value for " + option);
		}

		return args[index];
	}

	private static ViewExportFormat parseFormat(final String value) throws IOException {
		for (final ViewExportFormat format : ViewExportFormat.values()) {
			if (format.getExtension().equalsIgnoreCase(value)
					|| format.name().equalsIgnoreCase(value)) {
				return format;
			}
		}

		throw new IOException("Unsupported export type: " + value);
	}

	private static ViewExportScope parseScope(final String value) throws IOException {
		return switch (value.toLowerCase()) {
		case "selection" -> ViewExportScope.SELECTION;
		case "view" -> ViewExportScope.VIEW;
		case "everything", "all" -> ViewExportScope.EVERYTHING;
		default -> throw new IOException("Unsupported export scope: " + value);
		};
	}

	private static List<PanelType> parsePanels(final String value) throws IOException {
		if (value == null || value.isBlank()) {
			return List.of();
		}

		final List<PanelType> result = new ArrayList<>();

		for (final String rawPanel : value.split(",")) {
			final String panel = rawPanel.trim();

			try {
				result.add(PanelType.valueOf(panel.toUpperCase()));
			} catch (final IllegalArgumentException ex) {
				throw new IOException("Unsupported panel type: " + panel, ex);
			}
		}

		return result;
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
				  -p, --panels <list>        Comma-separated PanelType names
				      --pattern <pattern>    File name pattern, default: {source}-{panel}.{ext}
				  -f, --force                Continue on legacy/newer-version warnings
				  -h, --help                 Print this help
				""");
	}

	public static final class HelpRequestedException extends IOException {
	}

}