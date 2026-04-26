package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

final class DiagramModelLookup {

	private final DiagramCanvasModuleRegistry registry;

	DiagramModelLookup(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(canvas, "canvas");
		this.registry.setModelLookup(this);
	}

	ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.registry.document().getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	CommentModel findCommentById(final String commentId) {
		for (final CommentModel commentModel : this.registry.document().getModel().getComments()) {
			if (commentModel.getId().equals(commentId)) {
				return commentModel;
			}
		}
		return null;
	}

	FieldModel findFieldById(final String classId, final String fieldId) {
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

	LinkModel findLinkByAssociationClassId(final String classId) {
		return this.registry.document()
				.getModel()
				.getConceptualLinks()
				.stream()
				.filter(link -> Objects.equals(link.getAssociationClassId(), classId))
				.findFirst()
				.orElse(null);
	}

	LinkModel findLinkById(final String id) {
		for (final LinkModel linkModel : this.registry.document().getModel().getConceptualLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		for (final LinkModel linkModel : this.registry.document().getModel().getTechnicalLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		return null;
	}

	ClassModel findOwnerClassOfField(final String fieldId) {
		for (final ClassModel classModel : this.registry.document().getModel().getClasses()) {
			for (final FieldModel fieldModel : classModel.getFields()) {
				if (fieldModel.getId().equals(fieldId)) {
					return classModel;
				}
			}
		}
		return null;
	}

	FieldModel findPrimaryKeyField(final String classId) {
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

	Object findType(final SelectedElement selectedElement) {
		return switch (selectedElement.type()) {
		case CLASS -> this.findClassById(selectedElement.classId());
		case COMMENT -> this.findCommentById(selectedElement.commentId());
		case FIELD -> this.findFieldById(selectedElement.classId(), selectedElement.fieldId());
		case LINK -> this.findLinkById(selectedElement.linkId());
		default -> null;
		};
	}

	boolean linkEndpointExists(final String classId, final String fieldId) {
		if (classId == null || this.findClassById(classId) == null) {
			return false;
		}

		return fieldId == null || this.findFieldById(classId, fieldId) != null;
	}
}
