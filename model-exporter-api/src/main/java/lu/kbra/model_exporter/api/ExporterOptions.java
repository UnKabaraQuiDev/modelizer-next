package lu.kbra.model_exporter.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ExporterOptions extends Cloneable {

	@JsonProperty("_EXPORTER_ID")
	String getExporterId();

	ExporterOptions clone();

}
