package lu.kbra.image_exporter.bmp;

import lu.kbra.model_exporter.api.ExportFailedException;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.ImageModelVisitor;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.document.ModelDocument;

import lombok.Getter;

@Getter
public class BmpImageModelVisitor implements ModelVisitor {

	private static final String format = "bmp";

	private final ImageOptionsManager optionsManager;
	private final ImageExporterOptions options;

	public BmpImageModelVisitor(final ImageOptionsManager optionsManager, final ImageExporterOptions pclibOptions) {
		this.optionsManager = optionsManager;
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDocument(final ModelDocument file, final ExportUpdateCallback callback) throws ExportFailedException {
		ImageModelVisitor
				.export(file, this.optionsManager, this.options, callback, BmpImageModelVisitor.format, this.options.getExtension());
	}

}
