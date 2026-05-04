package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.MouseEvent;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.data.StylePreviewType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;

/**
 * Contains selection state helpers and selection change actions.
 */
interface SelectionController extends DiagramCanvasExt {

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

	default void clearSelection() {
		this.getCanvas().selectedElements.clear();
		this.getCanvas().selectedElement = null;
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

	default SelectionInfo getSelectionInfo() {
		return new SelectionInfo(this.getPanelType(), this.getCanvas().buildSelectionPath());
	}

	default StylePreviewType getStylePreviewType() {
		if (this.getCanvas().selectedElement == null) {
			return StylePreviewType.NONE;
		}

		return switch (this.getCanvas().selectedElement.type()) {
		case CLASS -> StylePreviewType.CLASS;
		case FIELD -> StylePreviewType.FIELD;
		case COMMENT -> StylePreviewType.COMMENT;
		case LINK -> StylePreviewType.LINK;
		default -> StylePreviewType.NONE;
		};
	}

	default boolean hasSelection() {
		return !this.getCanvas().selectedElements.isEmpty();
	}

	default boolean isClassSelected(final String classId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forClass(classId));
	}

	default boolean isCommentSelected(final String commentId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forComment(commentId));
	}

	default boolean isElementSelected(final SelectedElement element) {
		return element != null && this.getCanvas().selectedElements.contains(element);
	}

	default boolean isFieldSelected(final String classId, final String fieldId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forField(classId, fieldId));
	}

	default boolean isLinkSelected(final String linkId) {
		return !this.getCanvas().suppressSelectionDecorations
				&& this.getCanvas().selectedElements.contains(SelectedElement.forLink(linkId));
	}

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

	default void selectAll() {
		this.getCanvas().selectedElements.clear();

		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			if (classModel.isVisible(this.getPanelType())) {
				this.getCanvas().selectedElements.add(SelectedElement.forClass(classModel.getId()));
			}
		}

		for (final CommentModel commentModel : this.getDocument().getModel().getComments()) {
			final String text = this.getCanvas().resolveCommentText(commentModel);
			if (this.getCanvas().isCommentVisible(commentModel) && text != null && !text.isBlank()) {
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
