package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Contains display-name and technical-name resolution helpers.
 */
interface NameResolver extends DiagramCanvasExt {

	/**
	 * Returns the fallback text when the preferred value is blank.
	 *
	 * @param primary   text value for primary
	 * @param secondary text value for secondary
	 * @param fallback  text value for fallback
	 * @return the blank to fallback result
	 */
	default String blankToFallback(final String primary, final String secondary, final String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary;
		}
		if (secondary != null && !secondary.isBlank()) {
			return secondary;
		}
		return fallback;
	}

	/**
	 * Returns the editable class name.
	 *
	 * @param classModel class model affected by the operation
	 * @return the editable class name
	 */
	default String getEditableClassName(final ClassModel classModel) {
		return this.getPanelType() == PanelType.CONCEPTUAL ? classModel.getConceptualName() : classModel.getTechnicalName();
	}

	/**
	 * Returns the editable comment text.
	 *
	 * @param commentId id of the comment to look up or modify
	 * @return the editable comment text
	 */
	default String getEditableCommentText(final String commentId) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return "";
		}

		return commentModel.getText();
	}

	/**
	 * Returns the editable field name.
	 *
	 * @param fieldModel field model affected by the operation
	 * @return the editable field name
	 */
	default String getEditableFieldName(final FieldModel fieldModel) {
		return this.getPanelType() == PanelType.CONCEPTUAL ? fieldModel.getConceptualName() : fieldModel.getTechnicalName();
	}

	/**
	 * Resolves the class title from the current model and layout state.
	 *
	 * @param classModel class model affected by the operation
	 * @return the resolved class title
	 */
	default String resolveClassTitle(final ClassModel classModel) {
		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			return this.blankToFallback(classModel.getConceptualName(), classModel.getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getTechnicalName(), classModel.getConceptualName(), "Unnamed class");
	}

	/**
	 * Resolves the field name from the current model and layout state.
	 *
	 * @param fieldModel field model affected by the operation
	 * @return the resolved field name
	 */
	default String resolveFieldName(final FieldModel fieldModel) {
		final String baseName;
		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			baseName = this.blankToFallback(fieldModel.getConceptualName(), fieldModel.getTechnicalName(), "Unnamed field");
		} else {
			baseName = this.blankToFallback(fieldModel.getTechnicalName(), fieldModel.getConceptualName(), "Unnamed field");
		}

		return baseName;
//
//		if (this.getPanelType() != PanelType.PHYSICAL) {
//			return baseName;
//		}
//
//		final List<String> flags = new ArrayList<>();
//		if (fieldModel.isPrimaryKey()) {
//			flags.add("PK");
//		}
//		if (fieldModel.isUnique()) {
//			flags.add("UQ");
//		}
//		if (fieldModel.isNonNull()) {
//			flags.add("NN");
//		}
//
//		return baseName + (flags.isEmpty() ? "" : " [" + String.join(", ", flags) + "]") + " - "
//				+ (fieldModel.getType() == null ? "No type" : fieldModel.getType());
	}

	/**
	 * Sets the editable class name.
	 *
	 * @param classModel class model affected by the operation
	 * @param value      value to process
	 */
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

	/**
	 * Sets the editable comment text.
	 *
	 * @param commentId id of the comment to look up or modify
	 * @param value     value to process
	 */
	default void setEditableCommentText(final String commentId, final String value) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		commentModel.setText(value);
	}

	/**
	 * Sets the editable field name.
	 *
	 * @param fieldModel field model affected by the operation
	 * @param value      value to process
	 */
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
