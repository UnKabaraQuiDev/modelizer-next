package lu.kbra.modelizer_next.ui.canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.layout.PanelType;

final class NameResolver {

	private final DiagramCanvasModuleRegistry registry;

	NameResolver(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(canvas, "canvas");
		this.registry.setNameResolver(this);
	}

	String blankToFallback(final String primary, final String secondary, final String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary;
		}
		if (secondary != null && !secondary.isBlank()) {
			return secondary;
		}
		return fallback;
	}

	String getEditableClassName(final ClassModel classModel) {
		return this.registry.panelType() == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
				: classModel.getNames().getTechnicalName();
	}

	String getEditableCommentText(final String commentId) {
		final CommentModel commentModel = this.registry.modelLookup().findCommentById(commentId);
		if (commentModel == null) {
			return "";
		}

		return commentModel.getText();
	}

	String getEditableFieldName(final FieldModel fieldModel) {
		return this.registry.panelType() == PanelType.CONCEPTUAL ? fieldModel.getNames().getConceptualName()
				: fieldModel.getNames().getTechnicalName();
	}

	String resolveClassTitle(final ClassModel classModel) {
		if (this.registry.panelType() == PanelType.CONCEPTUAL) {
			return this
					.blankToFallback(classModel.getNames().getConceptualName(), classModel.getNames().getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getNames().getTechnicalName(), classModel.getNames().getConceptualName(), "Unnamed class");
	}

	String resolveCommentText(final CommentModel commentModel) {
		return commentModel == null ? ""
				: commentModel.getText() == null ? ""
				: commentModel.getText();
	}

	String resolveFieldName(final FieldModel fieldModel) {
		final String baseName;
		if (this.registry.panelType() == PanelType.CONCEPTUAL) {
			baseName = this
					.blankToFallback(fieldModel.getNames().getConceptualName(), fieldModel.getNames().getTechnicalName(), "Unnamed field");
		} else {
			baseName = this
					.blankToFallback(fieldModel.getNames().getTechnicalName(), fieldModel.getNames().getConceptualName(), "Unnamed field");
		}

		if (this.registry.panelType() != PanelType.PHYSICAL) {
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

	void setEditableClassName(final ClassModel classModel, final String value) {
		if (classModel == null) {
			return;
		}

		if (this.registry.panelType() == PanelType.CONCEPTUAL) {
			classModel.getNames().setConceptualName(value);
		} else {
			classModel.getNames().setTechnicalName(value);
		}
	}

	void setEditableCommentText(final String commentId, final String value) {
		final CommentModel commentModel = this.registry.modelLookup().findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		commentModel.setText(value);
	}

	void setEditableFieldName(final FieldModel fieldModel, final String value) {
		if (fieldModel == null) {
			return;
		}

		if (this.registry.panelType() == PanelType.CONCEPTUAL) {
			fieldModel.getNames().setConceptualName(value);
		} else {
			fieldModel.getNames().setTechnicalName(value);
		}
	}
}
