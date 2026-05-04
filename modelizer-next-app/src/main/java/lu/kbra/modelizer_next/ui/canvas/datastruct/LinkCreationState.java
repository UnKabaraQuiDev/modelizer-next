package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

public record LinkCreationState(SelectedType sourceType, String classId, String fieldId, String commentId, String linkId) {

	public static LinkCreationState fromSelection(final SelectedElement selection) {
		if (selection == null) {
			return null;
		}

		return new LinkCreationState(selection.type(), selection.classId(), selection.fieldId(), selection.commentId(), selection.linkId());
	}

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
