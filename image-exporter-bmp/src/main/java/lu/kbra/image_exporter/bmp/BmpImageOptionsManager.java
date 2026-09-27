package lu.kbra.image_exporter.bmp;

import java.awt.image.BufferedImage;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class BmpImageOptionsManager implements ImageOptionsManager {

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
		return BufferedImage.TYPE_3BYTE_BGR;
	}

	@Override
	public ExporterOptions blankOptions() {
		return new BmpImageExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return BmpImageExporterOptions.class;
	}

}
