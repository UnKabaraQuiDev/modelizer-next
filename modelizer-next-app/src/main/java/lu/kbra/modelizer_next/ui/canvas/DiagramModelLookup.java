package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

/**
 * Contains lookup helpers for classes, comments, fields, links, layouts, and panel state.
 */
interface DiagramModelLookup extends DiagramCanvasExt {

	/**
	 * Finds the class by ID that matches the supplied input.
	 * @param id stable id of the model element
	 * @return the matching class by ID, or {@code null} when no match exists
	 */
	default ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	/**
	 * Finds the comment by ID that matches the supplied input.
	 * @param commentId id of the comment to look up or modify
	 * @return the matching comment by ID, or {@code null} when no match exists
	 */
	default CommentModel findCommentById(final String commentId) {
		for (final CommentModel commentModel : this.getDocument().getModel().getComments()) {
			if (commentModel.getId().equals(commentId)) {
				return commentModel;
			}
		}
		return null;
	}

	/**
	 * Finds the field by ID that matches the supplied input.
	 * @param classModel class model affected by the operation
	 * @param fieldId id of the field to look up or modify
	 * @return the matching field by ID, or {@code null} when no match exists
	 */
	default FieldModel findFieldById(final ClassModel classModel, final String fieldId) {
		if (classModel == null) {
			return null;
		}

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.getId().equals(fieldId)) {
				return fieldModel;
			}
		}

		return null;
	}

	/**
	 * Finds the field by ID that matches the supplied input.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return the matching field by ID, or {@code null} when no match exists
	 */
	default FieldModel findFieldById(final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return null;
		}

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.getId().equals(fieldId)) {
				return fieldModel;
			}
		}

		return null;
	}

	/**
	 * Finds the link by association class ID that matches the supplied input.
	 * @param classId id of the class to look up or modify
	 * @return the matching link by association class ID, or {@code null} when no match exists
	 */
	default LinkModel findLinkByAssociationClassId(final String classId) {
		return this.getDocument()
				.getModel()
				.getConceptualLinks()
				.stream()
				.filter(link -> Objects.equals(link.getAssociationClassId(), classId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Finds the link by ID that matches the supplied input.
	 * @param id stable id of the model element
	 * @return the matching link by ID, or {@code null} when no match exists
	 */
	default LinkModel findLinkById(final String id) {
		for (final LinkModel linkModel : this.getDocument().getModel().getConceptualLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		for (final LinkModel linkModel : this.getDocument().getModel().getTechnicalLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		return null;
	}

	/**
	 * Finds the owner class of field that matches the supplied input.
	 * @param fieldId id of the field to look up or modify
	 * @return the matching owner class of field, or {@code null} when no match exists
	 */
	default ClassModel findOwnerClassOfField(final String fieldId) {
		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			for (final FieldModel fieldModel : classModel.getFields()) {
				if (fieldModel.getId().equals(fieldId)) {
					return classModel;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the primary key field that matches the supplied input.
	 * @param classId id of the class to look up or modify
	 * @return the matching primary key field, or {@code null} when no match exists
	 */
	default FieldModel findPrimaryKeyField(final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return null;
		}

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.isPrimaryKey()) {
				return fieldModel;
			}
		}

		return null;
	}

	/**
	 * Finds the type that matches the supplied input.
	 * @param selectedElement selected element to read or update
	 * @return the matching type, or {@code null} when no match exists
	 */
	default Object findType(final SelectedElement selectedElement) {
		return switch (selectedElement.type()) {
		case CLASS -> this.findClassById(selectedElement.classId());
		case COMMENT -> this.findCommentById(selectedElement.commentId());
		case FIELD -> this.findFieldById(selectedElement.classId(), selectedElement.fieldId());
		case LINK -> this.findLinkById(selectedElement.linkId());
		default -> null;
		};
	}

	/**
	 * Checks whether the link endpoint exists.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	default boolean linkEndpointExists(final String classId, final String fieldId) {
		if (classId == null || this.findClassById(classId) == null) {
			return false;
		}

		return fieldId == null || this.findFieldById(classId, fieldId) != null;
	}
}
