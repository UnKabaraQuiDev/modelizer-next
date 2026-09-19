package lu.kbra.image_exporter.png;

import lombok.Getter;
import lu.kbra.image_exporter.png.ui.PngImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

@Getter
public class PngImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "png";

	private final UiProvider uiProvider = new PngImageUiProvider();
	private final ImageOptionsManager optionsManager = new PngImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final PngImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new PngImageModelVisitor(pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return PngImageExporter.EXPORTER_ID;
	}

}
