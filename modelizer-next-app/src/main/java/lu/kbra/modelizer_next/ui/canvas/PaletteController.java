package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

final class PaletteController {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	PaletteController(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setPaletteController(this);
	}

	void applyDefaultPaletteToClass(final ClassModel classModel) {
		if (this.canvas.defaultPalette == null || classModel == null) {
			return;
		}
		classModel.getStyle().setTextColor(this.canvas.defaultPalette.getClassTextColor());
		classModel.getStyle().setBackgroundColor(this.canvas.defaultPalette.getClassBackgroundColor());
		classModel.getStyle().setBorderColor(this.canvas.defaultPalette.getClassBorderColor());
	}

	void applyDefaultPaletteToComment(final CommentModel commentModel) {
		if (this.canvas.defaultPalette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(this.canvas.defaultPalette.getCommentTextColor());
		commentModel.setBackgroundColor(this.canvas.defaultPalette.getCommentBackgroundColor());
		commentModel.setBorderColor(this.canvas.defaultPalette.getCommentBorderColor());
	}

	void applyDefaultPaletteToField(final FieldModel fieldModel) {
		if (this.canvas.defaultPalette == null || fieldModel == null) {
			return;
		}
		fieldModel.getStyle().setTextColor(this.canvas.defaultPalette.getFieldTextColor());
		fieldModel.getStyle().setBackgroundColor(this.canvas.defaultPalette.getFieldBackgroundColor());
	}

	void applyDefaultPaletteToLink(final LinkModel linkModel) {
		if (this.canvas.defaultPalette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(this.canvas.defaultPalette.getLinkColor());
	}

	void applyPalette(final StylePalette palette) {
		if (palette == null || this.canvas.selectedElements.isEmpty()) {
			return;
		}

		for (final SelectedElement element : this.canvas.selectedElements) {
			switch (element.type()) {
			case CLASS -> {
				final ClassModel classModel = this.registry.modelLookup().findClassById(element.classId());
				if (classModel != null) {
					classModel.getStyle().setTextColor(palette.getClassTextColor());
					classModel.getStyle().setBackgroundColor(palette.getClassBackgroundColor());
					classModel.getStyle().setBorderColor(palette.getClassBorderColor());
				}
			}
			case FIELD -> {
				final FieldModel fieldModel = this.registry.modelLookup().findFieldById(element.classId(), element.fieldId());
				if (fieldModel != null) {
					fieldModel.getStyle().setTextColor(palette.getFieldTextColor());
					fieldModel.getStyle().setBackgroundColor(palette.getFieldBackgroundColor());
				}
			}
			case COMMENT -> {
				final CommentModel commentModel = this.registry.modelLookup().findCommentById(element.commentId());
				if (commentModel != null) {
					commentModel.setTextColor(palette.getCommentTextColor());
					commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
					commentModel.setBorderColor(palette.getCommentBorderColor());
				}
			}
			case LINK -> {
				final LinkModel linkModel = this.registry.modelLookup().findLinkById(element.linkId());
				if (linkModel != null) {
					linkModel.setLineColor(palette.getLinkColor());
				}
			}
			default -> {
			}
			}
		}

		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void setDefaultPalette(final StylePalette defaultPalette) {
		this.canvas.defaultPalette = defaultPalette;
	}
}
