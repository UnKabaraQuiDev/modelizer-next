package lu.kbra.modelizer_next.ui.canvas;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;

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
		this.getCanvas().getPanelState().removeClassLayout(classId);

		final Set<LinkModel> tbr = new HashSet<>();
		if (this.getPanelType().isTechnical()) {
			this.getDocument()
					.getModel()
					.getTechnicalLinks()
					.parallelStream()
					.filter(l -> classId.equals(l.getFrom().getClassId()) || classId.equals(l.getTo().getClassId()))
					.peek(tbr::add)
					.forEach(c -> this.getDocument().getModel().removeTechnicalLink(c));
		} else {
			this.getDocument()
					.getModel()
					.getConceptualLinks()
					.parallelStream()
					.filter(l -> classId.equals(l.getFrom().getClassId()) || classId.equals(l.getTo().getClassId())
							|| classId.equals(l.getAssociationClassId()))
					.peek(tbr::add)
					.forEach(c -> this.getDocument().getModel().removeConceptualLink(c));
		}

		tbr.parallelStream()
				.map(c -> this.getCanvas().findLinkLayout(classId))
				.filter(Optional::isPresent)
				.forEach(c -> this.getCanvas().getPanelState().removeLinkLayout(c.get()));
	}

	/**
	 * Deletes the comment.
	 *
	 * @param commentId id of the comment to look up or modify
	 */
	default void deleteComment(final String commentId) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		this.getDocument().getModel().removeComment(commentModel);
		this.getCanvas().getPanelState().removeCommentLayout(commentId);
	}

	/**
	 * Deletes the field.
	 *
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 */
	default void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		final FieldModel fieldModel = this.getCanvas().findFieldById(classModel, fieldId);

		classModel.removeField(fieldModel);
		final Set<LinkModel> tbr = new HashSet<>();
		this.getDocument()
				.getModel()
				.getTechnicalLinks()
				.parallelStream()
				.filter(link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()))
				.peek(tbr::add)
				.forEach(l -> this.getDocument().getModel().removeTechnicalLink(l));
		tbr.parallelStream()
				.map(c -> this.getCanvas().findLinkLayout(classId))
				.filter(Optional::isPresent)
				.forEach(c -> this.getCanvas().getPanelState().removeLinkLayout(c.get()));
	}

	/**
	 * Deletes the link.
	 *
	 * @param linkId id of the link to look up or modify
	 */
	default void deleteLink(final String linkId) {
		final LinkModel linkModel = this.getCanvas().findLinkById(linkId);
		this.getCanvas().getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.getDocument().getModel().removeConceptualLink(linkModel);
		this.getDocument().getModel().removeTechnicalLink(linkModel);
		this.getCanvas().getPanelState().removeLinkLayout(linkId);
	}

}
