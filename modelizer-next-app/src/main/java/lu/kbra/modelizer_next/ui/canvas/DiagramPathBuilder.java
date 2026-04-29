package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.PanelType;

interface DiagramPathBuilder extends DiagramCanvasExt {

	default String buildForeignKeyFieldName(final ClassModel targetClass, final FieldModel targetField) {
		final String className = getCanvas()
				.blankToFallback(targetClass.getNames().getTechnicalName(), targetClass.getNames().getConceptualName(), "target");
		final String fieldName = getCanvas()
				.blankToFallback(targetField.getNames().getTechnicalName(), targetField.getNames().getConceptualName(), "id");
		return className + "_" + fieldName;
	}

	default String buildForeignKeyFieldTechnicalName(final ClassModel targetClass, final FieldModel targetField) {
		final String rawName = getCanvas().buildForeignKeyFieldName(targetClass, targetField);
		return rawName.trim().replaceAll("[^A-Za-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "").toLowerCase();
	}

	default String buildLinkPath(final LinkModel linkModel) {
		final ClassModel fromClass = getCanvas().findClassById(linkModel.getFrom().getClassId());
		final ClassModel toClass = getCanvas().findClassById(linkModel.getTo().getClassId());

		final String fromName = fromClass == null ? "?" : getCanvas().resolveClassTitle(fromClass);
		final String toName = toClass == null ? "?" : getCanvas().resolveClassTitle(toClass);

		if (getPanelType() == PanelType.CONCEPTUAL) {
			String middle = linkModel.getName() == null || linkModel.getName().isBlank() ? "link" : linkModel.getName();

			if (linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank()) {
				final ClassModel associationClass = getCanvas().findClassById(linkModel.getAssociationClassId());
				middle += "["
						+ (associationClass == null ? linkModel.getAssociationClassId() : getCanvas().resolveClassTitle(associationClass))
						+ "]";
			}

			return fromName + " > " + middle + " < " + toName;
		}

		final FieldModel fromField = getCanvas().findFieldById(linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId());
		final FieldModel toField = getCanvas().findFieldById(linkModel.getTo().getClassId(), linkModel.getTo().getFieldId());

		final String fromFieldName = fromField == null ? "?" : getCanvas().resolveFieldName(fromField);
		final String toFieldName = toField == null ? "?" : getCanvas().resolveFieldName(toField);

		return fromName + " > " + fromFieldName + " -> " + toFieldName + " < " + toName;
	}

	default String buildSelectionPath() {
		if (getCanvas().selectedElement == null) {
			return "";
		}

		switch (getCanvas().selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
			return classModel == null ? "" : getCanvas().resolveClassTitle(classModel);
		}
		case FIELD -> {
			final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
			final FieldModel fieldModel = getCanvas().findFieldById(getCanvas().selectedElement.classId(),
					getCanvas().selectedElement.fieldId());
			if (classModel == null || fieldModel == null) {
				return "";
			}
			return getCanvas().resolveClassTitle(classModel) + " > " + getCanvas().resolveFieldName(fieldModel);
		}
		case COMMENT -> {
			final CommentModel commentModel = getCanvas().findCommentById(getCanvas().selectedElement.commentId());
			if (commentModel == null) {
				return "";
			}

			if (commentModel.getKind() == CommentKind.STANDALONE) {
				return "Comment";
			}

			if (commentModel.getBinding() != null && commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
				final ClassModel classModel = getCanvas().findClassById(commentModel.getBinding().getTargetId());
				return classModel == null ? "Comment" : getCanvas().resolveClassTitle(classModel) + " > comment";
			}

			final LinkModel linkModel = commentModel.getBinding() == null ? null
					: getCanvas().findLinkById(commentModel.getBinding().getTargetId());
			return linkModel == null ? "Comment" : getCanvas().buildLinkPath(linkModel) + " > comment";
		}
		case LINK -> {
			final LinkModel linkModel = getCanvas().findLinkById(getCanvas().selectedElement.linkId());
			return linkModel == null ? "" : getCanvas().buildLinkPath(linkModel);
		}
		default -> {
			return "";
		}
		}
	}

}
