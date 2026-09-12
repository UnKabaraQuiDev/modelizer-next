package lu.kbra.code_exporter.java.pclib;

import lombok.Getter;
import lu.kbra.code_exporter.api.CodeExporter;
import lu.kbra.code_exporter.api.ModelVisitor;
import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.code_exporter.api.ui.OptionsManager;
import lu.kbra.code_exporter.api.ui.UIProvider;
import lu.kbra.code_exporter.java.pclib.ui.JavaPclibUiProvider;

@Getter
public class JavaPclibCodeExporter implements CodeExporter {

	public static final String EXPORTER_ID = "java-pclib";

	private final UIProvider uiProvider = new JavaPclibUiProvider();
	private final OptionsManager optionsManager = new JavaPclibOptionsManager();

	@Override
	public ModelVisitor buildModelVisitor(ExporterOptions options) {
		if (!(options instanceof final JavaPclibExporterOptions pclibOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}
		
		return new JavaPclibModelVisitor(pclibOptions);
	}
	
	@Override
	public String getExporterId() {
		return EXPORTER_ID;
	}

}
