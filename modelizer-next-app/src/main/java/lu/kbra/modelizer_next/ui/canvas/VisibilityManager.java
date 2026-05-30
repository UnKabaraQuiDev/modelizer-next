package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.CommentKind;

/**
 * Contains visibility checks for classes, comments, fields, and links.
 */
public interface VisibilityManager extends DiagramCanvasExt {

	/**
	 * Checks whether comment visible is enabled or applies.
	 *
	 * @param commentModel comment model affected by the operation
	 * @return {@code true} if comment visible is enabled or applies; otherwise {@code false}
	 */
	default boolean isCommentVisible(final CommentModel commentModel) {
		if (!commentModel.isVisible(this.getPanelType())) {
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

		final String targetLinkId = commentModel.getBinding().getTargetId();
		final boolean technicalLink = this.getDocument().getModel().validateTechnicalLinksByIdIndex().containsKey(targetLinkId);

		return this.getPanelType().isTechnical() == technicalLink;
	}

}
