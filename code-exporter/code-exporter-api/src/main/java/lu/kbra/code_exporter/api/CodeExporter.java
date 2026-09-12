package lu.kbra.code_exporter.api;

import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.code_exporter.api.ui.OptionsManager;
import lu.kbra.code_exporter.api.ui.UIProvider;

public interface CodeExporter {

	UIProvider getUiProvider();
	
	OptionsManager getOptionsManager();
	
	ModelVisitor buildModelVisitor(ExporterOptions options);

	String getExporterId();

}
