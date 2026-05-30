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

	/**
	 * Applies the default palette to class.
	 *
	 * @param classModel class model affected by the operation
	 */
	default void applyDefaultPaletteToClass(final ClassModel classModel) {
		this.applyPaletteToClass(this.getCanvas().defaultPalette, classModel, false, false);
	}

	/**
	 * Applies the default palette to class.
	 *
	 * @param classModel  class model affected by the operation
	 * @param deep        whether deep is enabled
	 * @param visibleOnly whether visible only is enabled
	 */
	@Deprecated
	default void applyDefaultPaletteToClass(final ClassModel classModel, final boolean deep, final boolean visibleOnly) {
		this.applyPaletteToClass(this.getCanvas().defaultPalette, classModel, deep, visibleOnly);
	}

	/**
	 * Applies the default palette to comment.
	 *
	 * @param commentModel comment model affected by the operation
	 */
	default void applyDefaultPaletteToComment(final CommentModel commentModel) {
		this.applyPaletteToComment(this.getCanvas().defaultPalette, commentModel);
	}

	/**
	 * Applies the default palette to field.
	 *
	 * @param fieldModel field model affected by the operation
	 */
	default void applyDefaultPaletteToField(final FieldModel fieldModel) {
		this.applyPaletteToField(this.getCanvas().defaultPalette, fieldModel);
	}

	/**
	 * Applies the default palette to link.
	 *
	 * @param linkModel link model affected by the operation
	 */
	default void applyDefaultPaletteToLink(final LinkModel linkModel) {
		this.applyPaletteToLink(this.getCanvas().defaultPalette, linkModel);
	}

	/**
	 * Applies the default palette to selection.
	 */
	default void applyDefaultPaletteToSelection() {
		this.applyPaletteToSelection(this.getCanvas().defaultPalette);
	}

	/**
	 * Applies the palette to class.
	 *
	 * @param palette    palette value used by the operation
	 * @param classModel class model affected by the operation
	 */
	default void applyPaletteToClass(final StylePalette palette, final ClassModel classModel) {
		this.applyPaletteToClass(palette, classModel, false, false);
	}

	/**
	 * Applies the palette to class.
	 *
	 * @param palette     palette value used by the operation
	 * @param classModel  class model affected by the operation
	 * @param deep        whether deep is enabled
	 * @param visibleOnly whether visible only is enabled
	 */
	default void
			applyPaletteToClass(final StylePalette palette, final ClassModel classModel, final boolean deep, final boolean visibleOnly) {
		if (palette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(palette.getClassTextColor());
		classModel.setBackgroundColor(palette.getClassBackgroundColor());
		classModel.setBorderColor(palette.getClassBorderColor());
		classModel.setLastPaletteName(palette.getName());
		if (deep) {
			for (final FieldModel fm : visibleOnly ? classModel.getFields(this.getPanelType()) : classModel.getFields()) {
				this.applyPaletteToField(palette, fm);
			}
		}
	}

	/**
	 * Applies the palette to comment.
	 *
	 * @param palette      palette value used by the operation
	 * @param commentModel comment model affected by the operation
	 */
	default void applyPaletteToComment(final StylePalette palette, final CommentModel commentModel) {
		if (palette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(palette.getCommentTextColor());
		commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
		commentModel.setBorderColor(palette.getCommentBorderColor());
		commentModel.setLastPaletteName(palette.getName());
	}

	/**
	 * Applies the palette to field.
	 *
	 * @param palette    palette value used by the operation
	 * @param fieldModel field model affected by the operation
	 */
	default void applyPaletteToField(final StylePalette palette, final FieldModel fieldModel) {
		if (palette == null || fieldModel == null) {
			return;
		}
		fieldModel.setTextColor(palette.getFieldTextColor());
		fieldModel.setBackgroundColor(palette.getFieldBackgroundColor());
		fieldModel.setLastPaletteName(palette.getName());
	}

	/**
	 * Applies the palette to link.
	 *
	 * @param palette   palette value used by the operation
	 * @param linkModel link model affected by the operation
	 */
	default void applyPaletteToLink(final StylePalette palette, final LinkModel linkModel) {
		if (palette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(palette.getLinkColor());
		linkModel.setLastPaletteName(palette.getName());
	}

	/**
	 * Applies the palette to selection.
	 *
	 * @param palette palette value used by the operation
	 */
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
	}

	/**
	 * Sets the default palette on the active canvas.
	 *
	 * @param defaultPalette default palette value used by the operation
	 */
	default void setDefaultPalette(final StylePalette defaultPalette) {
		this.getCanvas().defaultPalette = defaultPalette;
	}

}
