package lu.kbra.model_exporter.api;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.document.ModelDocument;
import lu.kbra.pclib.PCUtils;

public interface ModelVisitor {

	ModelVisitResult visitDocument(ModelDocument file, ExportUpdateCallback callback) throws ExportFailedException;

	static Path getPath(final Path outputPath, final String nameFormat, final String extension, final PanelType pt) {
		return Paths.get(ModelVisitor.ensureExtension(
				ModelVisitor
						.replacePlaceholders(ExporterApiContext.getApiContext().relative(outputPath, nameFormat).toString(), extension, pt),
				extension));
	}

	static String ensureExtension(final String string, final String ext) {
		return string.endsWith("." + ext) ? string : string + "." + ext;
	}

	static String replacePlaceholders(final String filename, final String extension, final PanelType panelType) {
		return filename
				.replace("{FILENAME}",
						ExporterApiContext.getApiContext().getCurrentDocument() == null ? "Unnamed"
								: PCUtils.removeFileExtension(new File(ExporterApiContext.getApiContext().getCurrentDocument()).getName()))
				.replace("{PANEL}", panelType == null ? "" : panelType.name())
				.replace("{EXT}", extension);
	}

}
