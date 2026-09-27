package lu.kbra.image_exporter.svg;

import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;

public class SvgImageOptionsManager implements ImageOptionsManager {


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
