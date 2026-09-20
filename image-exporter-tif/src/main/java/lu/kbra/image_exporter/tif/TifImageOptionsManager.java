package lu.kbra.image_exporter.tif;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class TifImageOptionsManager implements ImageOptionsManager {

	@Override
	@Deprecated
	public void relativizePaths(final ExporterOptions options, final Path documentPath) {
		if (!(options instanceof final TifImageExporterOptions pngOptions)) {
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
		return true;
	}

	@Override
	public boolean supportCompression() {
		return false;
	}

	@Override
	public int getImageType() {
		return BufferedImage.TYPE_INT_ARGB;
	}

	@Override
	public ExporterOptions blankOptions() {
		return new TifImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return TifImageExporterOptions.class;
	}

}
