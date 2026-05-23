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
		if (this.getCanvas().defaultPalette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(this.getCanvas().defaultPalette.getClassTextColor());
		classModel.setBackgroundColor(this.getCanvas().defaultPalette.getClassBackgroundColor());
		classModel.setBorderColor(this.getCanvas().defaultPalette.getClassBorderColor());
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

	/**
	 * Applies the default palette to comment.
	 *
	 * @param commentModel comment model affected by the operation
	 */
	default void applyDefaultPaletteToComment(final CommentModel commentModel) {
		if (this.getCanvas().defaultPalette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(this.getCanvas().defaultPalette.getCommentTextColor());
		commentModel.setBackgroundColor(this.getCanvas().defaultPalette.getCommentBackgroundColor());
		commentModel.setBorderColor(this.getCanvas().defaultPalette.getCommentBorderColor());
	}

	/**
	 * Applies the default palette to field.
	 *
	 * @param fieldModel field model affected by the operation
	 */
	default void applyDefaultPaletteToField(final FieldModel fieldModel) {
		if (this.getCanvas().defaultPalette == null || fieldModel == null) {
			return;
		}
		fieldModel.setTextColor(this.getCanvas().defaultPalette.getFieldTextColor());
		fieldModel.setBackgroundColor(this.getCanvas().defaultPalette.getFieldBackgroundColor());
	}

	/**
	 * Applies the default palette to link.
	 *
	 * @param linkModel link model affected by the operation
	 */
	default void applyDefaultPaletteToLink(final LinkModel linkModel) {
		if (this.getCanvas().defaultPalette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(this.getCanvas().defaultPalette.getLinkColor());
	}

	/**
	 * Applies the palette to class.
	 *
	 * @param palette    palette value used by the operation
	 * @param classModel class model affected by the operation
	 */
	default void applyPaletteToClass(final StylePalette palette, final ClassModel classModel) {
		if (palette == null || classModel == null) {
			return;
		}
		classModel.setTextColor(palette.getClassTextColor());
		classModel.setBackgroundColor(palette.getClassBackgroundColor());
		classModel.setBorderColor(palette.getClassBorderColor());
	}

	/**
	 * Applies the palette to class.
	 *
	 * @param palette     palette value used by the operation
	 * @param classModel  class model affected by the operation
	 * @param deep        whether deep is enabled
	 * @param visibleOnly whether visible only is enabled
	 */
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
	}

	/**
	 * Applies the default palette to selection.
	 *
	 * @param palette palette value used by the operation
	 */
	default void applyDefaultPaletteToSelection(final StylePalette palette) {
		this.applyPaletteToSelection(this.getCanvas().defaultPalette);
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
