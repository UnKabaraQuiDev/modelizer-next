package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;
import java.util.List;

public record CopiedClass(String sourceId, String conceptualName, String technicalName, String group, boolean visibleInConceptual,
		boolean visibleInLogical, boolean visibleInPhysical, Color textColor, Color backgroundColor, Color borderColor,
		List<CopiedField> fields, CopiedNodeLayout layout) {
}
