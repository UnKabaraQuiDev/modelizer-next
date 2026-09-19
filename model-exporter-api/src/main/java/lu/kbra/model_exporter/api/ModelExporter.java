package lu.kbra.model_exporter.api;

public interface ModelExporter {

	UiProvider getUiProvider();
	
	OptionsManager getOptionsManager();
	
	ModelVisitor buildModelVisitor(ExporterOptions options);
	
	ExporterType getExporterType();

	String getExporterId();

}
