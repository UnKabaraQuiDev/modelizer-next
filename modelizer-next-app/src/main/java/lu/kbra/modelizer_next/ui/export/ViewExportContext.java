package lu.kbra.modelizer_next.ui.export;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Context values available to format-specific exporters and text fields.
 *
 * @param sourceFile source document file, when available
 * @param panelType  exported panel type
 * @param outputFile file being written
 */
public record ViewExportContext(Optional<URI> sourceFile, PanelType panelType, File outputFile) {

}
