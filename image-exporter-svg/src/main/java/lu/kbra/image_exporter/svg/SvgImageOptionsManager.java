package lu.kbra.image_exporter.svg;

import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class SvgImageOptionsManager implements ImageOptionsManager {

	@Override
	@Deprecated
	public void relativizePaths(final ExporterOptions options, final Path documentPath) {
		if (!(options instanceof final SvgImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		if (pngOptions.getOutputPath() == null) {
			return;
		}

		final Path outputPath = pngOptions.getOutputPath();
		if (outputPath.isAbsolute()) {
			pngOptions.setOutputPath(documentPath.relativize(outputPath));
		}
	}

	@Override
	public boolean supportsTransparency() {
		return true;
	}

	@Override
	public boolean supportsFixedSize() {
		return false;
	}

	@Override
	public boolean supportCompression() {
		return false;
	}

	@Override
	public int getImageType() {
		return -1;
	}

	@Override
	public ExporterOptions blankOptions() {
		return new SvgImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return SvgImageExporterOptions.class;
	}

}
