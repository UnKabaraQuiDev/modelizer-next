package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement.LiveEditType;

public record SelectedElement(SelectedType type, String classId, String fieldId, String commentId, String linkId) {

	public enum SelectedType {

		NONE,
		CLASS,
		FIELD,
		COMMENT,
		LINK;

		public LiveEditType asLiveEditType() {
			return switch (this) {
			case CLASS -> LiveEditType.CLASS;
			case FIELD -> LiveEditType.CLASS_FIELD;
			case COMMENT -> LiveEditType.COMMENT;
			case LINK -> LiveEditType.LINK_LABEL;
			default -> throw new IllegalArgumentException("Unsupported option: " + this);
			};
		}

	}

	public static SelectedElement forClass(final String classId) {
		return new SelectedElement(SelectedType.CLASS, classId, null, null, null);
	}

	public static SelectedElement forField(final String classId, final String fieldId) {
		return new SelectedElement(SelectedType.FIELD, classId, fieldId, null, null);
	}

	public static SelectedElement forComment(final String commentId) {
		return new SelectedElement(SelectedType.COMMENT, null, null, commentId, null);
	}

	public static SelectedElement forLink(final String linkId) {
		return new SelectedElement(SelectedType.LINK, null, null, null, linkId);
	}

	public String getActualId() {
		return switch (this.type) {
		case CLASS -> this.classId;
		case FIELD -> this.fieldId;
		case COMMENT -> this.commentId;
		case LINK -> this.linkId;
		default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
		};
	}

	@Override
	public final int hashCode() {
		return Objects.hash(this.type, this.getActualId());
	}

	public LiveEditElement asLiveEditElement(boolean alternative) {
		return new LiveEditElement(this.type.asLiveEditType(), this.classId, this.fieldId, this.commentId, this.linkId, alternative);
	}

	public LiveEditElement asLiveEditElement(boolean alternative, boolean style) {
		return new LiveEditElement(this.type.asLiveEditType()
				.asStyle(style), this.classId, this.fieldId, this.commentId, this.linkId, alternative);
	}

	public LiveEditElement asStyleEditElement(boolean alternative) {
		return new LiveEditElement(this.type.asLiveEditType()
				.asStyle(alternative), this.classId, this.fieldId, this.commentId, this.linkId, false);
	}

	@Override
	public final boolean equals(final Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}

		return ((SelectedElement) other).type == this.type && Objects.equals(((SelectedElement) other).getActualId(), this.getActualId());
	}

}
