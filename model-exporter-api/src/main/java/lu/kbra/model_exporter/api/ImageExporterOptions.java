package lu.kbra.model_exporter.api;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;

public interface ImageExporterOptions extends ExporterOptions {

	String DEFAULT_NAME_FORMAT = "{FILENAME}-{PANEL}.{EXT}";

	String getNameFormat();

	void setNameFormat(String name);

	ViewExportScope getScope();

	void setScope(ViewExportScope scope);

	/**
	 * @return relative to the opened DOCUMENT else absolute
	 */
	Path getOutputPath();

	void setOutputPath(Path path);

	Set<PanelType> getPanels();

	void setPanels(Set<PanelType> panels);

	Optional<Color> getBackgroundColor();

	void setBackgroundColor(Optional<Color> color);

	String getExtension();

	@Override
	ImageExporterOptions clone();

}
