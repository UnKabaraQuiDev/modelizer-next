package lu.kbra.image_exporter.tiff;

import lombok.Getter;
import lu.kbra.model_exporter.api.ExportFailedException;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.ImageModelVisitor;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.document.ModelDocument;

@Getter
public class TiffImageModelVisitor implements ModelVisitor {

	private static final String format = "tif";

	private final ImageOptionsManager optionsManager;
	private final ImageExporterOptions options;

	public TiffImageModelVisitor(final ImageOptionsManager optionsManager, final ImageExporterOptions pclibOptions) {
		this.optionsManager = optionsManager;
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDocument(final ModelDocument file, final ExportUpdateCallback callback) throws ExportFailedException {
		ImageModelVisitor
				.export(file, this.optionsManager, this.options, callback, TiffImageModelVisitor.format, this.options.getExtension());
	}
}
