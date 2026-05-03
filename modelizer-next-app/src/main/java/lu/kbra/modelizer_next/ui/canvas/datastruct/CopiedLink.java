package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.data.Cardinality;

public record CopiedLink(String sourceId, String name, Color lineColor, String associationClassId, String fromClassId, String fromFieldId,
		String toClassId, String toFieldId, Cardinality cardinalityFrom, Cardinality cardinalityTo, String labelFrom, String labelTo,
		CopiedLinkLayout layout) {
}
