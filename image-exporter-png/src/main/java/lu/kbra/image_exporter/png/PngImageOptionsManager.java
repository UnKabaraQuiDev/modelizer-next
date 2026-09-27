package lu.kbra.image_exporter.png;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class PngImageOptionsManager implements ImageOptionsManager {


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
		return true;
	}

	@Override
	public int getImageType() {
		return BufferedImage.TYPE_INT_ARGB;
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
