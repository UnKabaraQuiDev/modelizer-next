package lu.kbra.image_exporter.jpeg;

import lu.kbra.image_exporter.jpeg.ui.JpegImageUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageModelExporter;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.UiProvider;

import lombok.Getter;

@Getter
public class JpegImageExporter implements ImageModelExporter {

	public static final String EXPORTER_ID = "jpeg";

	private final UiProvider uiProvider = new JpegImageUiProvider();
	private final ImageOptionsManager optionsManager = new JpegImageOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final JpegImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new JpegImageModelVisitor(this.optionsManager, pngOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.IMAGE;
	}

	@Override
	public String getExporterId() {
		return JpegImageExporter.EXPORTER_ID;
	}

}
