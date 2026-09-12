package lu.kbra.code_exporter.api.ui;

import java.nio.file.Path;

public interface ExporterOptions extends Cloneable {

	String getExporterId();

	Path getAttachedFile();

	void setAttachedFile(Path p);

	ExporterOptions clone();

}
