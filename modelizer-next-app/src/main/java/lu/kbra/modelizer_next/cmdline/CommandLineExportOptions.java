package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

/**
 * Parsed command-line export request. It groups the input pattern, export type, output destination,
 * view scope, file naming pattern, overwrite flag, and glob behavior.
 *
 * @param inputFile       file to read or write
 * @param format          export format to use
 * @param scope           export scope to use
 * @param panelTypes      values for panel types
 * @param outputDirectory output directory value used by the operation
 * @param fileNamePattern text value for file name pattern
 * @param force           whether force is enabled
 * @param multiple        whether multiple input files are allowed
 * @param wildcard        whether wildcard path matching is enabled
 * @param jobCount        count value to use
 */
public record CommandLineExportOptions(String inputFile, ViewExportFormat format, ViewExportScope scope, List<PanelType> panelTypes,
		File outputDirectory, String fileNamePattern, boolean force, boolean multiple, boolean wildcard, int jobCount) {

}
