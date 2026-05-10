package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;
import java.util.List;

/**
 * Class model and layout data stored in the clipboard snapshot.
 * @param sourceId id of the element to read or modify
 * @param conceptualName name value to use
 * @param technicalName name value to use
 * @param visibleInConceptual whether visible in conceptual is enabled
 * @param visibleInLogical whether visible in logical is enabled
 * @param visibleInPhysical whether visible in physical is enabled
 * @param textColor color value to use
 * @param backgroundColor color value to use
 * @param borderColor color value to use
 * @param fields values for fields
 * @param layout layout object to read or update
 */
public record CopiedClass(String sourceId, String conceptualName, String technicalName, boolean visibleInConceptual,
		boolean visibleInLogical, boolean visibleInPhysical, Color textColor, Color backgroundColor, Color borderColor,
		List<CopiedField> fields, CopiedNodeLayout layout) {
}
