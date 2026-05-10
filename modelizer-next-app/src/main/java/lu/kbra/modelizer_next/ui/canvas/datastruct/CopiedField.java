package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

/**
 * Field model stored in the clipboard snapshot.
 * @param ownerClassId id of the element to read or modify
 * @param sourceId id of the element to read or modify
 * @param name name value to read, write, or display
 * @param technicalName name value to use
 * @param notConceptual whether not conceptual is enabled
 * @param primaryKey whether primary key is enabled
 * @param unique whether unique is enabled
 * @param notNull whether not null is enabled
 * @param type type value that selects the operation mode
 * @param textColor color value to use
 * @param backgroundColor color value to use
 */
public record CopiedField(String ownerClassId, String sourceId, String name, String technicalName, boolean notConceptual,
		boolean primaryKey, boolean unique, boolean notNull, String type, Color textColor, Color backgroundColor) {
}
