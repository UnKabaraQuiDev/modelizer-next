package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;

import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;

/**
 * Contains visibility checks for classes, comments, fields, and links.
 */
public interface VisibilityManager extends DiagramCanvasExt {

	default boolean isVisible(ClassModel c) {
		return c.getVisibility().isVisible(getPanelType());
	}

	default boolean isCommentVisible(final CommentModel commentModel) {
		final boolean visibleInPanel = commentModel.getVisibility().isVisible(getPanelType());

		if (!visibleInPanel) {
			return false;
		}

		if (commentModel.getKind() == CommentKind.STANDALONE) {
			return true;
		}

		if (commentModel.getBinding() == null) {
			return false;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = getCanvas().findClassById(commentModel.getBinding().getTargetId());
			return classModel != null && getCanvas().isVisible(classModel);
		}

		final LinkModel linkModel = getCanvas().findLinkById(commentModel.getBinding().getTargetId());
		if (linkModel == null) {
			return false;
		}

		final Graphics2D g2 = getCanvas().createGraphicsContext();
		try {
			return getCanvas().resolveLinkGeometry(g2, linkModel) != null;
		} finally {
			g2.dispose();
		}
	}

}
