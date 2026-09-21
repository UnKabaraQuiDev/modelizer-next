package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.net.URI;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;

public record CommandLineExportOptions(
		String inputFile,
		ModelExporter exporter,
		File outputDirectory,
		boolean force,
		boolean multiple,
		boolean wildcard,
		int jobCount,
		ExporterOptions options,
		URI configFile) {

}
