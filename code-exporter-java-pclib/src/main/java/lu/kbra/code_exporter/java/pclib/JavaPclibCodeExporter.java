package lu.kbra.code_exporter.java.pclib;

import lu.kbra.code_exporter.java.pclib.ui.JavaPclibUiProvider;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.model_exporter.api.OptionsManager;
import lu.kbra.model_exporter.api.UiProvider;

import lombok.Getter;

@Getter
public class JavaPclibCodeExporter implements ModelExporter {

	public static final String EXPORTER_ID = "java-pclib";

	private final UiProvider uiProvider = new JavaPclibUiProvider();
	private final OptionsManager optionsManager = new JavaPclibOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(final ExporterOptions options) {
		if (!(options instanceof final JavaPclibExporterOptions pclibOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		return new JavaPclibModelVisitor(pclibOptions);
	}

	@Override
	public ExporterType getExporterType() {
		return ExporterType.CODE;
	}

	@Override
	public String getExporterId() {
		return JavaPclibCodeExporter.EXPORTER_ID;
	}

}
