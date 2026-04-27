package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

interface DiagramModelLookup extends DiagramCanvasExt {

	default ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	default CommentModel findCommentById(final String commentId) {
		for (final CommentModel commentModel : this.getDocument().getModel().getComments()) {
			if (commentModel.getId().equals(commentId)) {
				return commentModel;
			}
		}
		return null;
	}

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

	default LinkModel findLinkByAssociationClassId(final String classId) {
		return this.getDocument()
				.getModel()
				.getConceptualLinks()
				.stream()
				.filter(link -> Objects.equals(link.getAssociationClassId(), classId))
				.findFirst()
				.orElse(null);
	}

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

	default Object findType(final SelectedElement selectedElement) {
		return switch (selectedElement.type()) {
		case CLASS -> this.findClassById(selectedElement.classId());
		case COMMENT -> this.findCommentById(selectedElement.commentId());
		case FIELD -> this.findFieldById(selectedElement.classId(), selectedElement.fieldId());
		case LINK -> this.findLinkById(selectedElement.linkId());
		default -> null;
		};
	}

	default boolean linkEndpointExists(final String classId, final String fieldId) {
		if (classId == null || this.findClassById(classId) == null) {
			return false;
		}

		return fieldId == null || this.findFieldById(classId, fieldId) != null;
	}
}
