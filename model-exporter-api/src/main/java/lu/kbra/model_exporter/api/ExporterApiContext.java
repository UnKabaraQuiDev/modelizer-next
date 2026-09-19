package lu.kbra.model_exporter.api;

import java.io.File;

import lombok.Data;

@Data
public final class ExporterApiContext {

	private static final ThreadLocal<ExporterApiContext> API_CONTEXT = ThreadLocal.withInitial(ExporterApiContext::new);

	private File currentDocument;
	private File currentConfig;

	public static void setApiContext(final ExporterApiContext apiContext) {
		ExporterApiContext.API_CONTEXT.set(apiContext);
	}

	public static ExporterApiContext getApiContext() {
		return ExporterApiContext.API_CONTEXT.get();
	}

	public static void clearApiContext() {
		API_CONTEXT.set(new ExporterApiContext());
	}

}
