package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.MouseEvent;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;
import lu.kbra.modelizer_next.ui.canvas.datastruct.StylePreviewType;

/**
 * Contains selection state helpers and selection change actions.
 */
interface SelectionController extends DiagramCanvasExt {

	default void addToSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		getDocument().getModel().getClasses().sort(getCanvas().comparator);
		getCanvas().selectedElements.add(element);
		getCanvas().selectedElement = element;
		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

	default void clearSelection() {
		getCanvas().selectedElements.clear();
		getCanvas().selectedElement = null;
		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

	default SelectionInfo getSelectionInfo() {
		return new SelectionInfo(getPanelType(), getCanvas().buildSelectionPath());
	}

	default StylePreviewType getStylePreviewType() {
		if (getCanvas().selectedElement == null) {
			return StylePreviewType.NONE;
		}

		return switch (getCanvas().selectedElement.type()) {
		case CLASS -> StylePreviewType.CLASS;
		case FIELD -> StylePreviewType.FIELD;
		case COMMENT -> StylePreviewType.COMMENT;
		case LINK -> StylePreviewType.LINK;
		default -> StylePreviewType.NONE;
		};
	}

	default boolean hasSelection() {
		return !getCanvas().selectedElements.isEmpty();
	}

	default boolean isClassSelected(final String classId) {
		return !getCanvas().suppressSelectionDecorations && getCanvas().selectedElements.contains(SelectedElement.forClass(classId));
	}

	default boolean isCommentSelected(final String commentId) {
		return !getCanvas().suppressSelectionDecorations && getCanvas().selectedElements.contains(SelectedElement.forComment(commentId));
	}

	default boolean isElementSelected(final SelectedElement element) {
		return element != null && getCanvas().selectedElements.contains(element);
	}

	default boolean isFieldSelected(final String classId, final String fieldId) {
		return !getCanvas().suppressSelectionDecorations
				&& getCanvas().selectedElements.contains(SelectedElement.forField(classId, fieldId));
	}

	default boolean isLinkSelected(final String linkId) {
		return !getCanvas().suppressSelectionDecorations && getCanvas().selectedElements.contains(SelectedElement.forLink(linkId));
	}

	default void removeFromSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		getCanvas().selectedElements.remove(element);

		if (Objects.equals(getCanvas().selectedElement, element)) {
			getCanvas().selectedElement = getCanvas().selectedElements.isEmpty() ? null : getCanvas().selectedElements.getLast();
		}

		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

	default void select(final SelectedElement element) {
		getCanvas().selectedElements.clear();
		if (element != null) {
			getCanvas().selectedElements.add(element);
		}
		getDocument().getModel().getClasses().sort(getCanvas().comparator);
		getCanvas().selectedElement = element;
		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

	default void selectAll() {
		getCanvas().selectedElements.clear();

		for (final ClassModel classModel : getDocument().getModel().getClasses()) {
			if (getCanvas().isVisible(classModel)) {
				getCanvas().selectedElements.add(SelectedElement.forClass(classModel.getId()));
			}
		}

		for (final CommentModel commentModel : getDocument().getModel().getComments()) {
			final String text = getCanvas().resolveCommentText(commentModel);
			if (getCanvas().isCommentVisible(commentModel) && text != null && !text.isBlank()) {
				getCanvas().selectedElements.add(SelectedElement.forComment(commentModel.getId()));
			}
		}

		for (final LinkModel linkModel : getCanvas().getActiveLinks()) {
			getCanvas().selectedElements.add(SelectedElement.forLink(linkModel.getId()));
		}

		getCanvas().selectedElement = getCanvas().selectedElements.isEmpty() ? null : getCanvas().selectedElements.getLast();
		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

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
