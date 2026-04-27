package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

public record CopiedField(String ownerClassId, String sourceId, String name, String technicalName, boolean notConceptual, String comment,
		boolean primaryKey, boolean unique, boolean notNull, String type, Color textColor, Color backgroundColor) {
}
