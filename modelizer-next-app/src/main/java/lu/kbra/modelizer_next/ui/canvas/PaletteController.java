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

	default void applyPalette(final StylePalette palette) {
		if (palette == null || this.getCanvas().selectedElements.isEmpty()) {
			return;
		}

		for (final SelectedElement element : this.getCanvas().selectedElements) {
			switch (element.type()) {
			case CLASS -> {
				final ClassModel classModel = this.getCanvas().findClassById(element.classId());
				if (classModel != null) {
					classModel.setTextColor(palette.getClassTextColor());
					classModel.setBackgroundColor(palette.getClassBackgroundColor());
					classModel.setBorderColor(palette.getClassBorderColor());
				}
			}
			case FIELD -> {
				final FieldModel fieldModel = this.getCanvas().findFieldById(element.classId(), element.fieldId());
				if (fieldModel != null) {
					fieldModel.setTextColor(palette.getFieldTextColor());
					fieldModel.setBackgroundColor(palette.getFieldBackgroundColor());
				}
			}
			case COMMENT -> {
				final CommentModel commentModel = this.getCanvas().findCommentById(element.commentId());
				if (commentModel != null) {
					commentModel.setTextColor(palette.getCommentTextColor());
					commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
					commentModel.setBorderColor(palette.getCommentBorderColor());
				}
			}
			case LINK -> {
				final LinkModel linkModel = this.getCanvas().findLinkById(element.linkId());
				if (linkModel != null) {
					linkModel.setLineColor(palette.getLinkColor());
				}
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
