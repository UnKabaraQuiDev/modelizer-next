package lu.kbra.modelizer_next.ui.canvas;

import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Contains display-name and technical-name resolution helpers.
 */
interface NameResolver extends DiagramCanvasExt {

	default String blankToFallback(final String primary, final String secondary, final String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary;
		}
		if (secondary != null && !secondary.isBlank()) {
			return secondary;
		}
		return fallback;
	}

	default String getEditableClassName(final ClassModel classModel) {
		return this.getPanelType() == PanelType.CONCEPTUAL ? classModel.getConceptualName() : classModel.getTechnicalName();
	}

	default String getEditableCommentText(final String commentId) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return "";
		}

		return commentModel.getText();
	}

	default String getEditableFieldName(final FieldModel fieldModel) {
		return this.getPanelType() == PanelType.CONCEPTUAL ? fieldModel.getConceptualName() : fieldModel.getTechnicalName();
	}

	default String resolveClassTitle(final ClassModel classModel) {
		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			return this.blankToFallback(classModel.getConceptualName(), classModel.getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getTechnicalName(), classModel.getConceptualName(), "Unnamed class");
	}

	default String resolveCommentText(final CommentModel commentModel) {
		return commentModel == null ? ""
				: commentModel.getText() == null ? ""
				: commentModel.getText();
	}

	default String resolveFieldName(final FieldModel fieldModel) {
		final String baseName;
		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			baseName = this.blankToFallback(fieldModel.getConceptualName(), fieldModel.getTechnicalName(), "Unnamed field");
		} else {
			baseName = this.blankToFallback(fieldModel.getTechnicalName(), fieldModel.getConceptualName(), "Unnamed field");
		}

		if (this.getPanelType() != PanelType.PHYSICAL) {
			return baseName;
		}

		final List<String> flags = new ArrayList<>();
		if (fieldModel.isPrimaryKey()) {
			flags.add("PK");
		}
		if (fieldModel.isUnique()) {
			flags.add("UQ");
		}
		if (fieldModel.isNotNull()) {
			flags.add("NN");
		}

		if (flags.isEmpty()) {
			return baseName;
		}

		return baseName + " [" + String.join(", ", flags) + "] - " + (fieldModel.getType() == null ? "No type" : fieldModel.getType());
	}

	default void setEditableClassName(final ClassModel classModel, final String value) {
		if (classModel == null) {
			return;
		}

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			classModel.setConceptualName(value);
		} else {
			classModel.setTechnicalName(value);
		}
	}

	default void setEditableCommentText(final String commentId, final String value) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		commentModel.setText(value);
	}

	default void setEditableFieldName(final FieldModel fieldModel, final String value) {
		if (fieldModel == null) {
			return;
		}

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			fieldModel.setConceptualName(value);
		} else {
			fieldModel.setTechnicalName(value);
		}
	}

}
