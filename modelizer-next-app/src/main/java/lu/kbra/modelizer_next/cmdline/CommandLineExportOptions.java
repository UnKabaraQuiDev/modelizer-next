package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

public record CommandLineExportOptions(
		File inputFile,
		ViewExportFormat format,
		ViewExportScope scope,
		List<PanelType> panelTypes,
		File outputDirectory,
		String fileNamePattern,
		boolean force) {

}