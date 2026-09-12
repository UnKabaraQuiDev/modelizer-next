package lu.kbra.code_exporter.api.ui;

import java.nio.file.Path;

public interface OptionsManager {

	ExporterOptions relativizePaths(ExporterOptions options, Path path);
	
	ExporterOptions blankOptions();

	Class<? extends ExporterOptions> getClassType();
	
}
