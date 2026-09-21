package lu.kbra.image_exporter.png;

import lu.kbra.model_exporter.api.ExportFailedException;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.ImageModelVisitor;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitResult;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.document.ModelDocument;

import lombok.Getter;

@Getter
public class PngImageModelVisitor implements ModelVisitor {

	private static final String format = "png";

	private final ImageOptionsManager optionsManager;
	private final ImageExporterOptions options;

	public PngImageModelVisitor(final ImageOptionsManager optionsManager, final ImageExporterOptions pclibOptions) {
		this.optionsManager = optionsManager;
		this.options = pclibOptions.clone();
	}

	@Override
	public ModelVisitResult visitDocument(final ModelDocument file, final ExportUpdateCallback callback) throws ExportFailedException {
		return ImageModelVisitor
				.export(file, this.optionsManager, this.options, callback, PngImageModelVisitor.format, this.options.getExtension());
	}

}
