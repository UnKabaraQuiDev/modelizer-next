package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.CommentKind;

public record CopiedComment(String sourceId, CommentKind kind, String text, Color textColor, Color backgroundColor, Color borderColor,
		boolean visibleInConceptual, boolean visibleInLogical, boolean visibleInPhysical, BoundTargetType bindingTargetType,
		String bindingTargetId, CopiedNodeLayout layout) {
}
