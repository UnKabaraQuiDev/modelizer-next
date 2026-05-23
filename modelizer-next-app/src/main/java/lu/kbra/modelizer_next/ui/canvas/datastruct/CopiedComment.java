package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.CommentKind;

/**
 * Comment model and layout data stored in the clipboard snapshot.
 *
 * @param sourceId            id of the element to read or modify
 * @param kind                kind value used by the operation
 * @param text                text to display or edit
 * @param textColor           color value to use
 * @param backgroundColor     color value to use
 * @param borderColor         color value to use
 * @param visibleInConceptual whether visible in conceptual is enabled
 * @param visibleInLogical    whether visible in logical is enabled
 * @param visibleInPhysical   whether visible in physical is enabled
 * @param bindingTargetType   type value to use
 * @param bindingTargetId     id of the element to read or modify
 * @param layout              layout object to read or update
 */
public record CopiedComment(String sourceId, CommentKind kind, String text, Color textColor, Color backgroundColor, Color borderColor,
		boolean visibleInConceptual, boolean visibleInLogical, boolean visibleInPhysical, BoundTargetType bindingTargetType,
		String bindingTargetId, CopiedNodeLayout layout) {
}
