package lu.kbra.model_exporter.api;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lu.kbra.modelizer_next.domain.data.PanelType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ExporterApiContext {

	private static final ThreadLocal<ExporterApiContext> API_CONTEXT = ThreadLocal.withInitial(ExporterApiContext::new);

	private URI currentDocument;
	private URI currentConfig;
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

}
