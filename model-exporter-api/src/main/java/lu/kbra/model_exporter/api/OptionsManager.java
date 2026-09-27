package lu.kbra.model_exporter.api;

public interface OptionsManager {

	ExporterOptions blankOptions();

	Class<? extends ExporterOptions> getClassType();

}
