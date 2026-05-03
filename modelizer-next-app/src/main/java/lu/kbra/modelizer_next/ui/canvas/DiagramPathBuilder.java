package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Contains geometry helpers that build link paths and self-link paths.
 */
interface DiagramPathBuilder extends DiagramCanvasExt {

	default String buildForeignKeyFieldName(final ClassModel targetClass, final FieldModel targetField) {
		final String className = this.getCanvas()
				.blankToFallback(targetClass.getTechnicalName(), targetClass.getConceptualName(), "target");
		final String fieldName = this.getCanvas().blankToFallback(targetField.getTechnicalName(), targetField.getConceptualName(), "id");
		return className + "_" + fieldName;
	}

	default String buildForeignKeyFieldTechnicalName(final ClassModel targetClass, final FieldModel targetField) {
		final String rawName = this.getCanvas().buildForeignKeyFieldName(targetClass, targetField);
		return rawName.trim().replaceAll("[^A-Za-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "").toLowerCase();
	}

	default String buildLinkPath(final LinkModel linkModel) {
		final ClassModel fromClass = this.getCanvas().findClassById(linkModel.getFrom().getClassId());
		final ClassModel toClass = this.getCanvas().findClassById(linkModel.getTo().getClassId());

		final String fromName = fromClass == null ? "?" : this.getCanvas().resolveClassTitle(fromClass);
		final String toName = toClass == null ? "?" : this.getCanvas().resolveClassTitle(toClass);

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			String middle = linkModel.getName() == null || linkModel.getName().isBlank() ? "link" : linkModel.getName();

			if (linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank()) {
				final ClassModel associationClass = this.getCanvas().findClassById(linkModel.getAssociationClassId());
				middle += "[" + (associationClass == null ? linkModel.getAssociationClassId()
						: this.getCanvas().resolveClassTitle(associationClass)) + "]";
			}

			return fromName + " > " + middle + " < " + toName;
		}

		final FieldModel fromField = this.getCanvas().findFieldById(linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId());
		final FieldModel toField = this.getCanvas().findFieldById(linkModel.getTo().getClassId(), linkModel.getTo().getFieldId());

		final String fromFieldName = fromField == null ? "?" : this.getCanvas().resolveFieldName(fromField);
		final String toFieldName = toField == null ? "?" : this.getCanvas().resolveFieldName(toField);

		return fromName + " > " + fromFieldName + " -> " + toFieldName + " < " + toName;
	}

	default String buildSelectionPath() {
		if (this.getCanvas().selectedElement == null) {
			return "";
		}

		switch (this.getCanvas().selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
			return classModel == null ? "" : this.getCanvas().resolveClassTitle(classModel);
		}
		case FIELD -> {
			final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
			final FieldModel fieldModel = this.getCanvas()
					.findFieldById(this.getCanvas().selectedElement.classId(), this.getCanvas().selectedElement.fieldId());
			if (classModel == null || fieldModel == null) {
				return "";
			}
			return this.getCanvas().resolveClassTitle(classModel) + " > " + this.getCanvas().resolveFieldName(fieldModel);
		}
		case COMMENT -> {
			final CommentModel commentModel = this.getCanvas().findCommentById(this.getCanvas().selectedElement.commentId());
			if (commentModel == null) {
				return "";
			}

			if (commentModel.getKind() == CommentKind.STANDALONE) {
				return "Comment";
			}

			if (commentModel.getBinding() != null && commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
				final ClassModel classModel = this.getCanvas().findClassById(commentModel.getBinding().getTargetId());
				return classModel == null ? "Comment" : this.getCanvas().resolveClassTitle(classModel) + " > comment";
			}

			final LinkModel linkModel = commentModel.getBinding() == null ? null
					: this.getCanvas().findLinkById(commentModel.getBinding().getTargetId());
			return linkModel == null ? "Comment" : this.getCanvas().buildLinkPath(linkModel) + " > comment";
		}
		case LINK -> {
			final LinkModel linkModel = this.getCanvas().findLinkById(this.getCanvas().selectedElement.linkId());
			return linkModel == null ? "" : this.getCanvas().buildLinkPath(linkModel);
		}
		default -> {
			return "";
		}
		}
	}

}
