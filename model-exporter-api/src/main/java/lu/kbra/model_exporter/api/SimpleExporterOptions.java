package lu.kbra.model_exporter.api;

import lu.kbra.pclib.PCUtils;

import lombok.Data;

@Data
public class SimpleExporterOptions implements ExporterOptions {

	private String exporterId;

	@Override
	public SimpleExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
