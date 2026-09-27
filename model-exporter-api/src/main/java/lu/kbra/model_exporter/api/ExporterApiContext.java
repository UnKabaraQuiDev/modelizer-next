package lu.kbra.model_exporter.api;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lu.kbra.modelizer_next.domain.data.PanelType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ExporterApiContext {

	private static final InheritableThreadLocal<ExporterApiContext> API_CONTEXT = new InheritableThreadLocal<>();

	static {
		ExporterApiContext.clearApiContext();
	}

	private URI currentDocument;
	private URI currentConfig;
	private Path outputDirectory;
	private ExportContext context = ExportContext.GUI;
	private Function<Set<PanelType>, Map<PanelType, ? extends CanvasRenderer>> renderers;

	public static void setApiContext(final ExporterApiContext apiContext) {
		ExporterApiContext.API_CONTEXT.set(apiContext);
	}

	public static ExporterApiContext getApiContext() {
		return ExporterApiContext.API_CONTEXT.get();
	}

	public static void clearApiContext() {
		ExporterApiContext.API_CONTEXT.set(new ExporterApiContext());
	}

	public Path relative(final Path outputPath, final String nameFormat) {
		if (outputPath != null && outputPath.isAbsolute()) {
			return outputPath.resolve(nameFormat);
		}
		if (this.outputDirectory != null) {
			if (outputPath != null) {
				return this.outputDirectory.resolve(outputPath).resolve(nameFormat);
			} else {
				return this.outputDirectory.resolve(nameFormat);
			}
		}
		if (this.currentDocument != null) {
			if (outputPath != null) {
				return Paths.get(this.currentDocument).getParent().resolve(outputPath).resolve(nameFormat);
			} else {
				return Paths.get(this.currentDocument).getParent().resolve(nameFormat);
			}
		}
		return Paths.get(nameFormat);
	}

}
