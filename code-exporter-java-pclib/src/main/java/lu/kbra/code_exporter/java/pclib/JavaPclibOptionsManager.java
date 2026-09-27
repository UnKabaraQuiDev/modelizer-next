package lu.kbra.code_exporter.java.pclib;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.OptionsManager;

public class JavaPclibOptionsManager implements OptionsManager {

	@Override
	public ExporterOptions blankOptions() {
		return new JavaPclibExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return JavaPclibExporterOptions.class;
	}

}
