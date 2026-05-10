package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.data.Cardinality;

/**
 * Link model and layout data stored in the clipboard snapshot.
 * @param sourceId id of the element to read or modify
 * @param name name value to read, write, or display
 * @param lineColor color value to use
 * @param associationClassId id of the element to read or modify
 * @param fromClassId id of the element to read or modify
 * @param fromFieldId id of the element to read or modify
 * @param toClassId id of the element to read or modify
 * @param toFieldId id of the element to read or modify
 * @param cardinalityFrom cardinality from value used by the operation
 * @param cardinalityTo cardinality to value used by the operation
 * @param labelFrom text value for label from
 * @param labelTo text value for label to
 * @param layout layout object to read or update
 */
public record CopiedLink(String sourceId, String name, Color lineColor, String associationClassId, String fromClassId, String fromFieldId,
		String toClassId, String toFieldId, Cardinality cardinalityFrom, Cardinality cardinalityTo, String labelFrom, String labelTo,
		CopiedLinkLayout layout) {
}
