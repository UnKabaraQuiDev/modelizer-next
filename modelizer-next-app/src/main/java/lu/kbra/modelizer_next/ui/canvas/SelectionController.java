package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.MouseEvent;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.data.StyleScope;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;

/**
 * Contains selection state helpers and selection change actions.
 */
interface SelectionController extends DiagramCanvasExt {

	/**
	 * Adds the to selection.
	 * @param element element value used by the operation
	 */
	default void addToSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.getDocument().getModel().getClasses().sort(this.getCanvas().comparator);
		this.getCanvas().selectedElements.add(element);
		this.getCanvas().selectedElement = element;
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	/**
	 * Clears the selection.
	 */
	default void clearSelection() {
		this.getCanvas().selectedElements.clear();
		this.getCanvas().selectedElement = null;
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	/**
	 * Returns the selection info.
	 * @return the selection info
	 */
	default SelectionInfo getSelectionInfo() {
		return new SelectionInfo(this.getPanelType(), this.getCanvas().buildSelectionPath());
	}

	/**
	 * Returns the style scope on the active canvas.
	 * @return the style scope
	 */
	default StyleScope getStyleScope() {
		if (this.getCanvas().selectedElement == null) {
			return StyleScope.NONE;
		}

		return switch (this.getCanvas().selectedElement.type()) {
		case CLASS -> StyleScope.CLASS;
		case FIELD -> StyleScope.FIELD;
		case COMMENT -> StyleScope.COMMENT;
		case LINK -> StyleScope.LINK;
		default -> StyleScope.NONE;
		};
	}

	/**
	 * Checks whether this object has a selection.
	 * @return {@code true} if selection exists; otherwise {@code false}
	 */
	default boolean hasSelection() {
		return !this.getCanvas().selectedElements.isEmpty();
	}

	/**
	 * Checks whether class selected is enabled or applies.
	 * @param classId id of the class to look up or modify
	 * @return {@code true} if class selected is enabled or applies; otherwise {@code false}
	 */
	default boolean isClassSelected(final String classId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forClass(classId));
	}

	/**
	 * Checks whether comment selected is enabled or applies.
	 * @param commentId id of the comment to look up or modify
	 * @return {@code true} if comment selected is enabled or applies; otherwise {@code false}
	 */
	default boolean isCommentSelected(final String commentId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forComment(commentId));
	}

	/**
	 * Checks whether element selected is enabled or applies on the active canvas.
	 * @param element element value used by the operation
	 * @return {@code true} if element selected is enabled or applies; otherwise {@code false}
	 */
	default boolean isElementSelected(final SelectedElement element) {
		return element != null && this.getCanvas().selectedElements.contains(element);
	}

	/**
	 * Checks whether field selected is enabled or applies.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return {@code true} if field selected is enabled or applies; otherwise {@code false}
	 */
	default boolean isFieldSelected(final String classId, final String fieldId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forField(classId, fieldId));
	}

	/**
	 * Checks whether link selected is enabled or applies.
	 * @param linkId id of the link to look up or modify
	 * @return {@code true} if link selected is enabled or applies; otherwise {@code false}
	 */
	default boolean isLinkSelected(final String linkId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forLink(linkId));
	}

	/**
	 * Removes the from selection.
	 * @param element element value used by the operation
	 */
	default void removeFromSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.getCanvas().selectedElements.remove(element);

		if (Objects.equals(this.getCanvas().selectedElement, element)) {
			this.getCanvas().selectedElement = this.getCanvas().selectedElements.isEmpty() ? null
					: this.getCanvas().selectedElements.getLast();
		}

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	/**
	 * Selects an element and updates dependent UI state.
	 * @param element element value used by the operation
	 */
	default void select(final SelectedElement element) {
		this.getCanvas().selectedElements.clear();
		if (element != null) {
			this.getCanvas().selectedElements.add(element);
		}
		this.getDocument().getModel().getClasses().sort(this.getCanvas().comparator);
		this.getCanvas().selectedElement = element;
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	/**
	 * Selects the all on the active canvas.
	 */
	default void selectAll() {
		this.getCanvas().selectedElements.clear();

		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			if (classModel.isVisible(this.getPanelType())) {
				this.getCanvas().selectedElements.add(SelectedElement.forClass(classModel.getId()));
			}
		}

		for (final CommentModel commentModel : this.getDocument().getModel().getComments()) {
			if (this.getCanvas().isCommentVisible(commentModel)) {
				this.getCanvas().selectedElements.add(SelectedElement.forComment(commentModel.getId()));
			}
		}

		for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
			this.getCanvas().selectedElements.add(SelectedElement.forLink(linkModel.getId()));
		}

		this.getCanvas().selectedElement = this.getCanvas().selectedElements.isEmpty() ? null : this.getCanvas().selectedElements.getLast();
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	/**
	 * Updates the selection from mouse.
	 * @param element element value used by the operation
	 * @param event event object supplied by Swing
	 */
	default void updateSelectionFromMouse(final SelectedElement element, final MouseEvent event) {
		if (element == null) {
			if (!event.isShiftDown() && !event.isControlDown()) {
				this.clearSelection();
			}
			return;
		}

		if (event.isShiftDown()) {
			this.addToSelection(element);
			return;
		}

		if (event.isControlDown()) {
			this.removeFromSelection(element);
			return;
		}

		this.select(element);
	}

}
