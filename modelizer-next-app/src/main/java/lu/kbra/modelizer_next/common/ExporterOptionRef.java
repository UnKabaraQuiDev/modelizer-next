package lu.kbra.modelizer_next.common;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;
import lu.kbra.model_exporter.api.ExporterOptions;

@Data
@AllArgsConstructor
public class ExporterOptionRef {

	private File file;
	private ExporterOptions options;

	public boolean hasFile() {
		return this.file != null;
	}

	public boolean hasOptions() {
		return this.options != null;
	}

}
