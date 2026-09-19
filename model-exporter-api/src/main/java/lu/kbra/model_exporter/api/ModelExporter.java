package lu.kbra.model_exporter.api;

import lu.kbra.model_exporter.api.ui.ExporterOptions;
import lu.kbra.model_exporter.api.ui.OptionsManager;
import lu.kbra.model_exporter.api.ui.UIProvider;

public interface ModelExporter {

	UIProvider getUiProvider();
	
	OptionsManager getOptionsManager();
	
	ModelVisitor buildModelVisitor(ExporterOptions options);
	
	ExporterType getExporterType();

	String getExporterId();

}
