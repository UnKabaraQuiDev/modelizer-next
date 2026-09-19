package lu.kbra.model_exporter.api;

import lombok.Data;
import lu.kbra.pclib.PCUtils;

@Data
public class SimpleExporterOptions implements ExporterOptions {

	private String exporterId;
	
	@Override
	public SimpleExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
