package lu.kbra.image_exporter.webp;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class WebpImageOptionsManager implements ImageOptionsManager {

	@Override
	@Deprecated
	public void relativizePaths(final ExporterOptions options, final Path documentPath) {
		if (!(options instanceof final WebpImageExporterOptions pngOptions)) {
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
		return false;
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
		return BufferedImage.TYPE_INT_RGB;
	}

	@Override
	public ExporterOptions blankOptions() {
		return new WebpImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return WebpImageExporterOptions.class;
	}

}
