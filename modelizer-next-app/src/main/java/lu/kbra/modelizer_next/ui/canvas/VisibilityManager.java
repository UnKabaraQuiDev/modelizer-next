package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.CommentKind;

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

		final String targetId = commentModel.getBinding().getTargetId();
		final boolean technicalLink;
		LinkModel linkModel = getDocument().getModel()
				.getConceptualLinks()
				.stream()
				.filter(c -> Objects.equals(c.getId(), targetId))
				.findFirst()
				.orElse(null);
		if (linkModel == null) {
			linkModel = getDocument().getModel()
					.getTechnicalLinks()
					.stream()
					.filter(c -> Objects.equals(c.getId(), targetId))
					.findFirst()
					.orElse(null);
			technicalLink = true;
		} else {
			technicalLink = false;
		}

		return getPanelType().isTechnical() == technicalLink;

//		final Graphics2D g2 = this.getCanvas().createGraphicsContext();
//		try {
//			return this.getCanvas().resolveLinkGeometry(g2, linkModel) != null;
//		} finally {
//			g2.dispose();
//		}
	}

}
