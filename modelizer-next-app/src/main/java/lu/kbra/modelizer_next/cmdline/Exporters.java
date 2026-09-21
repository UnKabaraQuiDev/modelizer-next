package lu.kbra.modelizer_next.cmdline;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import lombok.Getter;
import lu.kbra.model_exporter.api.ModelExporter;

public class Exporters {

	@Getter
	private static final List<ModelExporter> modelExporters;
	
	public static final String DEFAULT_FILE_PATTERN = "{FILENAME}-{PATTERN}.{EXT}";

	static {
		modelExporters = ServiceLoader.load(ModelExporter.class).stream().map(z -> {
			try {
				return z.get();
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		}).filter(Objects::nonNull).toList();
		System.out.println("Found: " + Exporters.modelExporters.size() + " exporters\n"
				+ Exporters.modelExporters.stream()
						.map(c -> " * [" + c.getExporterType() + "] " + c.getExporterId())
						.collect(Collectors.joining("\n")));
	}

	public static final void init() {
		// noop
		Exporters.class.getCanonicalName();
	}

}
