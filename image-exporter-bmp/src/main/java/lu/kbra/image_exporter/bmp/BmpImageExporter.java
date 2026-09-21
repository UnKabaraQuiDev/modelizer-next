package lu.kbra.image_exporter.bmp;

import lu.kbra.image_exporter.bmp.ui.BmpImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

import lombok.Getter;

@Getter
public class BmpImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "bmp";

	private final UiProvider uiProvider = new BmpImageUiProvider();
	private final ImageOptionsManager optionsManager = new BmpImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final BmpImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new BmpImageModelVisitor(this.optionsManager, pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return BmpImageExporter.EXPORTER_ID;
	}

}
