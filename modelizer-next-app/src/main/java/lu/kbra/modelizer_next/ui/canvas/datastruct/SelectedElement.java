package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingElement.RenamingType;

public record SelectedElement(SelectedType type, String classId, String fieldId, String commentId, String linkId) {

	public enum SelectedType {

		NONE,
		CLASS,
		FIELD,
		COMMENT,
		LINK;

		public RenamingType asRenamingType() {
			return switch (this) {
			case CLASS -> RenamingType.CLASS;
			case FIELD -> RenamingType.CLASS_FIELD;
			case COMMENT -> RenamingType.COMMENT;
			case LINK -> RenamingType.LINK_LABEL;
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

	public RenamingElement asRenamingElement() {
		return new RenamingElement(this.type.asRenamingType(), classId, fieldId, commentId, linkId);
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}
		return ((SelectedElement) other).type == this.type && Objects.equals(((SelectedElement) other).getActualId(), this.getActualId());
	}

}
