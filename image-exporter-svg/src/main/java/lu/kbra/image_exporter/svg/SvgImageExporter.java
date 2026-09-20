package lu.kbra.image_exporter.svg;

import lombok.Getter;
import lu.kbra.image_exporter.svg.ui.SvgImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

@Getter
public class SvgImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "svg";

	private final UiProvider uiProvider = new SvgImageUiProvider();
	private final ImageOptionsManager optionsManager = new SvgImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final SvgImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new SvgImageModelVisitor(pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return SvgImageExporter.EXPORTER_ID;
	}

}
