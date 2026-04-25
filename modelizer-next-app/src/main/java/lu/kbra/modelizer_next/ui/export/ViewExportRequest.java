package lu.kbra.modelizer_next.ui.export;

import java.io.File;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;

public record ViewExportRequest(ViewExportFormat format, ViewExportScope scope, List<PanelType> panelTypes, File outputDirectory,
		String fileNamePattern) {

}
