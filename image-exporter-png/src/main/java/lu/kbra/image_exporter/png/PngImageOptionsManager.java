package lu.kbra.image_exporter.png;

import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.OptionsManager;

public class PngImageOptionsManager implements OptionsManager {

	@Override
	public void relativizePaths(final ExporterOptions options, final Path documentPath) {
		if (!(options instanceof final PngImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		if (pngOptions.getOutputPath() != null) {
			final Path outputPath = pngOptions.getOutputPath();

			if (outputPath.isAbsolute()) {
				pngOptions.setOutputPath(documentPath.relativize(outputPath));
			}
		}
	}

	@Override
	public ExporterOptions blankOptions() {
		return new PngImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return PngImageExporterOptions.class;
	}

}
