package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;

/**
 * Contains visibility checks for classes, comments, fields, and links.
 */
public interface VisibilityManager extends DiagramCanvasExt {

	default boolean isCommentVisible(final CommentModel commentModel) {
		final boolean visibleInPanel = commentModel.isVisible(this.getPanelType());

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
			final ClassModel classModel = this.getCanvas().findClassById(commentModel.getBinding().getTargetId());
			return classModel != null && classModel.isVisible(this.getPanelType());
		}

		final LinkModel linkModel = this.getCanvas().findLinkById(commentModel.getBinding().getTargetId());
		if (linkModel == null) {
			return false;
		}

		final Graphics2D g2 = this.getCanvas().createGraphicsContext();
		try {
			return this.getCanvas().resolveLinkGeometry(g2, linkModel) != null;
		} finally {
			g2.dispose();
		}
	}

}
