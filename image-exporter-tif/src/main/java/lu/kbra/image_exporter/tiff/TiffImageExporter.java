package lu.kbra.image_exporter.tiff;

import lu.kbra.image_exporter.tif.TifImageExporterOptions;
import lu.kbra.image_exporter.tif.ui.TifImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

import lombok.Getter;

@Getter
public class TiffImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "png";

	private final UiProvider uiProvider = new TifImageUiProvider();
	private final ImageOptionsManager optionsManager = new TiffImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final TifImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new TiffImageModelVisitor(this.optionsManager, pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return TiffImageExporter.EXPORTER_ID;
	}

}
