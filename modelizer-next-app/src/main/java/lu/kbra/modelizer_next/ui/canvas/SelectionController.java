package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.MouseEvent;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;
import lu.kbra.modelizer_next.ui.canvas.datastruct.StylePreviewType;

final class SelectionController {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	SelectionController(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setSelectionController(this);
	}

	void addToSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.registry.document().getModel().getClasses().sort(this.canvas.comparator);
		this.canvas.selectedElements.add(element);
		this.canvas.selectedElement = element;
		this.canvas.notifySelectionChanged();
		this.canvas.repaint();
	}

	void clearSelection() {
		this.canvas.selectedElements.clear();
		this.canvas.selectedElement = null;
		this.canvas.notifySelectionChanged();
		this.canvas.repaint();
	}

	SelectionInfo getSelectionInfo() {
		return new SelectionInfo(this.registry.panelType(), this.canvas.buildSelectionPath());
	}

	StylePreviewType getStylePreviewType() {
		if (this.canvas.selectedElement == null) {
			return StylePreviewType.NONE;
		}

		return switch (this.canvas.selectedElement.type()) {
		case CLASS -> StylePreviewType.CLASS;
		case FIELD -> StylePreviewType.FIELD;
		case COMMENT -> StylePreviewType.COMMENT;
		case LINK -> StylePreviewType.LINK;
		default -> StylePreviewType.NONE;
		};
	}

	boolean hasSelection() {
		return !this.canvas.selectedElements.isEmpty();
	}

	boolean isClassSelected(final String classId) {
		return !this.canvas.suppressSelectionDecorations && this.canvas.selectedElements.contains(SelectedElement.forClass(classId));
	}

	boolean isCommentSelected(final String commentId) {
		return !this.canvas.suppressSelectionDecorations && this.canvas.selectedElements.contains(SelectedElement.forComment(commentId));
	}

	boolean isElementSelected(final SelectedElement element) {
		return element != null && this.canvas.selectedElements.contains(element);
	}

	boolean isFieldSelected(final String classId, final String fieldId) {
		return !this.canvas.suppressSelectionDecorations
				&& this.canvas.selectedElements.contains(SelectedElement.forField(classId, fieldId));
	}

	boolean isLinkSelected(final String linkId) {
		return !this.canvas.suppressSelectionDecorations && this.canvas.selectedElements.contains(SelectedElement.forLink(linkId));
	}

	void removeFromSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.canvas.selectedElements.remove(element);

		if (Objects.equals(this.canvas.selectedElement, element)) {
			this.canvas.selectedElement = this.canvas.selectedElements.isEmpty() ? null : this.canvas.selectedElements.getLast();
		}

		this.canvas.notifySelectionChanged();
		this.canvas.repaint();
	}

	void select(final SelectedElement element) {
		this.canvas.selectedElements.clear();
		if (element != null) {
			this.canvas.selectedElements.add(element);
		}
		this.registry.document().getModel().getClasses().sort(this.canvas.comparator);
		this.canvas.selectedElement = element;
		this.canvas.notifySelectionChanged();
		this.canvas.repaint();
	}

	void selectAll() {
		this.canvas.selectedElements.clear();

		for (final ClassModel classModel : this.registry.document().getModel().getClasses()) {
			if (this.canvas.isVisible(classModel)) {
				this.canvas.selectedElements.add(SelectedElement.forClass(classModel.getId()));
			}
		}

		for (final CommentModel commentModel : this.registry.document().getModel().getComments()) {
			final String text = this.canvas.resolveCommentText(commentModel);
			if (this.canvas.isCommentVisible(commentModel) && text != null && !text.isBlank()) {
				this.canvas.selectedElements.add(SelectedElement.forComment(commentModel.getId()));
			}
		}

		for (final LinkModel linkModel : this.canvas.getActiveLinks()) {
			this.canvas.selectedElements.add(SelectedElement.forLink(linkModel.getId()));
		}

		this.canvas.selectedElement = this.canvas.selectedElements.isEmpty() ? null : this.canvas.selectedElements.getLast();
		this.canvas.notifySelectionChanged();
		this.canvas.repaint();
	}

	void updateSelectionFromMouse(final SelectedElement element, final MouseEvent event) {
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
