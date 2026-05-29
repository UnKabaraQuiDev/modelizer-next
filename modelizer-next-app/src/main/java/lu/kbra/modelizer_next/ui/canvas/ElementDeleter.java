package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;

/**
 * Contains deletion actions for the current canvas selection.
 */
public interface ElementDeleter extends DiagramCanvasExt {

	/**
	 * Deletes the class.
	 *
	 * @param classId id of the class to look up or modify
	 */
	default void deleteClass(final String classId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		this.getDocument().getModel().removeClass(classModel);
		this.getCanvas()
				.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.CLASS && layout.getObjectId().equals(classId));

		this.getCanvas()
				.getActiveLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.getDocument()
				.getModel()
				.getConceptualLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));
		this.getDocument()
				.getModel()
				.getTechnicalLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.getCanvas()
				.getPanelState()
				.getLinkLayouts()
				.removeIf(linkLayout -> this.getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	/**
	 * Deletes the comment.
	 *
	 * @param commentId id of the comment to look up or modify
	 */
	default void deleteComment(final String commentId) {
		final CommentModel commentModel = getCanvas().findCommentById(commentId);
		this.getDocument().getModel().removeComment(commentModel);
		this.getCanvas()
				.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	/**
	 * Deletes the field.
	 *
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 */
	default void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		final FieldModel fieldModel = getCanvas().findFieldById(classModel, fieldId);

		classModel.removeField(fieldModel);
		this.getDocument()
				.getModel()
				.getTechnicalLinks()
				.removeIf(link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()));
		this.getCanvas()
				.getPanelState()
				.getLinkLayouts()
				.removeIf(linkLayout -> this.getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	/**
	 * Deletes the link.
	 *
	 * @param linkId id of the link to look up or modify
	 */
	default void deleteLink(final String linkId) {
		final LinkModel linkModel = getCanvas().findLinkById(linkId);
		this.getCanvas().getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.getDocument().getModel().removeConceptualLink(linkModel);
		this.getDocument().getModel().removeTechnicalLink(linkModel);
		this.getCanvas().getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

}
