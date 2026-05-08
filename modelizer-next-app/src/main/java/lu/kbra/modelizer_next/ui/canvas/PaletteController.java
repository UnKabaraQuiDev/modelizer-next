package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

/**
 * Contains style palette lookup and default style application helpers.
 */
interface PaletteController extends DiagramCanvasExt {

	default void applyDefaultPaletteToClass(final ClassModel classModel) {
		if (this.getCanvas().defaultPalette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(this.getCanvas().defaultPalette.getClassTextColor());
		classModel.setBackgroundColor(this.getCanvas().defaultPalette.getClassBackgroundColor());
		classModel.setBorderColor(this.getCanvas().defaultPalette.getClassBorderColor());
	}

	@Deprecated
	default void applyDefaultPaletteToClass(final ClassModel classModel, final boolean deep, final boolean visibleOnly) {
		if (this.getCanvas().defaultPalette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(this.getCanvas().defaultPalette.getClassTextColor());
		classModel.setBackgroundColor(this.getCanvas().defaultPalette.getClassBackgroundColor());
		classModel.setBorderColor(this.getCanvas().defaultPalette.getClassBorderColor());
		if (deep) {
			for (final FieldModel fm : visibleOnly ? classModel.getFields(this.getPanelType()) : classModel.getFields()) {
				this.applyDefaultPaletteToField(fm);
			}
		}
	}

	default void applyDefaultPaletteToComment(final CommentModel commentModel) {
		if (this.getCanvas().defaultPalette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(this.getCanvas().defaultPalette.getCommentTextColor());
		commentModel.setBackgroundColor(this.getCanvas().defaultPalette.getCommentBackgroundColor());
		commentModel.setBorderColor(this.getCanvas().defaultPalette.getCommentBorderColor());
	}

	default void applyDefaultPaletteToField(final FieldModel fieldModel) {
		if (this.getCanvas().defaultPalette == null || fieldModel == null) {
			return;
		}
		fieldModel.setTextColor(this.getCanvas().defaultPalette.getFieldTextColor());
		fieldModel.setBackgroundColor(this.getCanvas().defaultPalette.getFieldBackgroundColor());
	}

	default void applyDefaultPaletteToLink(final LinkModel linkModel) {
		if (this.getCanvas().defaultPalette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(this.getCanvas().defaultPalette.getLinkColor());
	}

	default void applyPaletteToClass(final StylePalette palette, final ClassModel classModel) {
		if (palette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(palette.getClassTextColor());
		classModel.setBackgroundColor(palette.getClassBackgroundColor());
		classModel.setBorderColor(palette.getClassBorderColor());
	}

	default void applyPaletteToClass(
			final StylePalette palette,
			final ClassModel classModel,
			final boolean deep,
			final boolean visibleOnly) {
		if (palette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(palette.getClassTextColor());
		classModel.setBackgroundColor(palette.getClassBackgroundColor());
		classModel.setBorderColor(palette.getClassBorderColor());
		if (deep) {
			for (final FieldModel fm : visibleOnly ? classModel.getFields(this.getPanelType()) : classModel.getFields()) {
				this.applyPaletteToField(palette, fm);
			}
		}
	}

	default void applyPaletteToComment(final StylePalette palette, final CommentModel commentModel) {
		if (palette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(palette.getCommentTextColor());
		commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
		commentModel.setBorderColor(palette.getCommentBorderColor());
	}

	default void applyPaletteToField(final StylePalette palette, final FieldModel fieldModel) {
		if (palette == null || fieldModel == null) {
			return;
		}
		fieldModel.setTextColor(palette.getFieldTextColor());
		fieldModel.setBackgroundColor(palette.getFieldBackgroundColor());
	}

	default void applyPaletteToLink(final StylePalette palette, final LinkModel linkModel) {
		if (palette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(palette.getLinkColor());
	}

	default void applyDefaultPaletteToSelection(final StylePalette palette) {
		this.applyPaletteToSelection(this.getCanvas().defaultPalette);
	}

	default void applyPaletteToSelection(final StylePalette palette) {
		if (palette == null || this.getCanvas().selectedElements.isEmpty()) {
			return;
		}

		for (final SelectedElement element : this.getCanvas().selectedElements) {
			switch (element.type()) {
			case CLASS -> {
				final ClassModel classModel = this.getCanvas().findClassById(element.classId());
				this.applyPaletteToClass(palette, classModel);
			}
			case FIELD -> {
				final FieldModel fieldModel = this.getCanvas().findFieldById(element.classId(), element.fieldId());
				this.applyPaletteToField(palette, fieldModel);
			}
			case COMMENT -> {
				final CommentModel commentModel = this.getCanvas().findCommentById(element.commentId());
				this.applyPaletteToComment(palette, commentModel);
			}
			case LINK -> {
				final LinkModel linkModel = this.getCanvas().findLinkById(element.linkId());
				this.applyPaletteToLink(palette, linkModel);
			}
			default -> {
			}
			}
		}

		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void setDefaultPalette(final StylePalette defaultPalette) {
		this.getCanvas().defaultPalette = defaultPalette;
	}

}
