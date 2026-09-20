package lu.kbra.image_exporter.webp;

import lombok.Getter;
import lu.kbra.image_exporter.webp.ui.WebpImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

@Getter
public class WebpImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "webp";

	private final UiProvider uiProvider = new WebpImageUiProvider();
	private final ImageOptionsManager optionsManager = new WebpImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final WebpImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new WebpImageModelVisitor(this.optionsManager, pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return WebpImageExporter.EXPORTER_ID;
	}

}
