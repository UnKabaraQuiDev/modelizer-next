package lu.kbra.image_exporter.jpeg;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class JpegImageOptionsManager implements ImageOptionsManager {

	@Override
	@Deprecated
	public void relativizePaths(final ExporterOptions options, final Path documentPath) {
		if (!(options instanceof final JpegImageExporterOptions pngOptions)) {
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
		return new JpegImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return JpegImageExporterOptions.class;
	}

}
