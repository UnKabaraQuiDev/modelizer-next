package lu.kbra.modelizer_next.ui.export;

import java.io.File;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Immutable request object passed to the exporter.
 *
 * @param format          export format to use
 * @param scope           export scope to use
 * @param panelTypes      values for panel types
 * @param outputDirectory output directory value used by the operation
 * @param fileNamePattern text value for file name pattern
 * @param multiple        whether multiple input files are allowed
 * @param wildcard        whether wildcard path matching is enabled
 */
public record ViewExportRequest(
		ViewExportFormat format,
		ViewExportScope scope,
		List<PanelType> panelTypes,
		File outputDirectory,
		String fileNamePattern,
		boolean multiple,
		boolean wildcard) {

}
