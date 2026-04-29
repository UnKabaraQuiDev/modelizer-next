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
		if (getCanvas().defaultPalette == null || classModel == null) {
			return;
		}
		classModel.getStyle().setTextColor(getCanvas().defaultPalette.getClassTextColor());
		classModel.getStyle().setBackgroundColor(getCanvas().defaultPalette.getClassBackgroundColor());
		classModel.getStyle().setBorderColor(getCanvas().defaultPalette.getClassBorderColor());
	}

	default void applyDefaultPaletteToComment(final CommentModel commentModel) {
		if (getCanvas().defaultPalette == null || commentModel == null) {
			return;
		}
		commentModel.setTextColor(getCanvas().defaultPalette.getCommentTextColor());
		commentModel.setBackgroundColor(getCanvas().defaultPalette.getCommentBackgroundColor());
		commentModel.setBorderColor(getCanvas().defaultPalette.getCommentBorderColor());
	}

	default void applyDefaultPaletteToField(final FieldModel fieldModel) {
		if (getCanvas().defaultPalette == null || fieldModel == null) {
			return;
		}
		fieldModel.getStyle().setTextColor(getCanvas().defaultPalette.getFieldTextColor());
		fieldModel.getStyle().setBackgroundColor(getCanvas().defaultPalette.getFieldBackgroundColor());
	}

	default void applyDefaultPaletteToLink(final LinkModel linkModel) {
		if (getCanvas().defaultPalette == null || linkModel == null) {
			return;
		}
		linkModel.setLineColor(getCanvas().defaultPalette.getLinkColor());
	}

	default void applyPalette(final StylePalette palette) {
		if (palette == null || getCanvas().selectedElements.isEmpty()) {
			return;
		}

		for (final SelectedElement element : getCanvas().selectedElements) {
			switch (element.type()) {
			case CLASS -> {
				final ClassModel classModel = getCanvas().findClassById(element.classId());
				if (classModel != null) {
					classModel.getStyle().setTextColor(palette.getClassTextColor());
					classModel.getStyle().setBackgroundColor(palette.getClassBackgroundColor());
					classModel.getStyle().setBorderColor(palette.getClassBorderColor());
				}
			}
			case FIELD -> {
				final FieldModel fieldModel = getCanvas().findFieldById(element.classId(), element.fieldId());
				if (fieldModel != null) {
					fieldModel.getStyle().setTextColor(palette.getFieldTextColor());
					fieldModel.getStyle().setBackgroundColor(palette.getFieldBackgroundColor());
				}
			}
			case COMMENT -> {
				final CommentModel commentModel = getCanvas().findCommentById(element.commentId());
				if (commentModel != null) {
					commentModel.setTextColor(palette.getCommentTextColor());
					commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
					commentModel.setBorderColor(palette.getCommentBorderColor());
				}
			}
			case LINK -> {
				final LinkModel linkModel = getCanvas().findLinkById(element.linkId());
				if (linkModel != null) {
					linkModel.setLineColor(palette.getLinkColor());
				}
			}
			default -> {
			}
			}
		}

		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void setDefaultPalette(final StylePalette defaultPalette) {
		getCanvas().defaultPalette = defaultPalette;
	}

}
