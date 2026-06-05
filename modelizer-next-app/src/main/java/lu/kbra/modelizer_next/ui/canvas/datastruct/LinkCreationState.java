package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

/**
 * State kept while the user creates a new link with the mouse.
 *
 * @param sourceType type value to use
 * @param classId    id of the class to look up or modify
 * @param fieldId    id of the field to look up or modify
 * @param commentId  id of the comment to look up or modify
 * @param linkId     id of the link to look up or modify
 */
public record LinkCreationState(SelectedType sourceType, String classId, String fieldId, String commentId, String linkId, Point2D origin) {

	/**
	 * Creates a value from the supplied selection.
	 *
	 * @param selection selection state to read or update
	 * @return the from selection result
	 */
	public static LinkCreationState fromSelection(final SelectedElement selection, final Point2D origin) {
		if (selection == null) {
			return null;
		}

		return new LinkCreationState(selection
				.type(), selection.classId(), selection.fieldId(), selection.commentId(), selection.linkId(), origin);
	}

	/**
	 * Converts the input to a selected element on the active canvas.
	 *
	 * @return the to selected element result
	 */
	public SelectedElement toSelectedElement() {
		return switch (this.sourceType) {
		case CLASS -> SelectedElement.forClass(this.classId);
		case FIELD -> SelectedElement.forField(this.classId, this.fieldId);
		case COMMENT -> SelectedElement.forComment(this.commentId);
		case LINK -> SelectedElement.forLink(this.linkId);
		default -> null;
		};
	}

}
