package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import lu.kbra.modelizer_next.ui.canvas.data.StyleScope;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement.LiveEditType;

/**
 * Identifier of the currently selected model element and its kind.
 *
 * @param type      type value that selects the operation mode
 * @param classId   id of the class to look up or modify
 * @param fieldId   id of the field to look up or modify
 * @param commentId id of the comment to look up or modify
 * @param linkId    id of the link to look up or modify
 */
public record SelectedElement(SelectedType type, String classId, String fieldId, String commentId, String linkId) {

	/**
	 * Enumerates supported selected type values.
	 */
	public enum SelectedType {

		NONE,
		CLASS,
		FIELD,
		COMMENT,
		LINK;

		/**
		 * Converts this selected type to the matching live edit type.
		 *
		 * @return the as live edit type result
		 */
		public LiveEditType asLiveEditType() {
			return switch (this) {
			case CLASS -> LiveEditType.CLASS;
			case FIELD -> LiveEditType.CLASS_FIELD;
			case COMMENT -> LiveEditType.COMMENT;
			case LINK -> LiveEditType.LINK_LABEL;
			default -> throw new IllegalArgumentException("Unsupported option: " + this);
			};
		}

		/**
		 * Converts this selected type to the matching style scope.
		 *
		 * @return the as style scope result
		 */
		public StyleScope asStyleScope() {
			return switch (this) {
			case CLASS -> StyleScope.CLASS;
			case FIELD -> StyleScope.FIELD;
			case COMMENT -> StyleScope.COMMENT;
			case LINK -> StyleScope.LINK;
			default -> throw new IllegalArgumentException("Unexpected value: " + this);
			};
		}

	}

	/**
	 * Creates the default style for class elements.
	 *
	 * @param classId id of the class to look up or modify
	 * @return the for class result
	 */
	public static SelectedElement forClass(final String classId) {
		return new SelectedElement(SelectedType.CLASS, classId, null, null, null);
	}

	/**
	 * Creates the default style for field elements.
	 *
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return the for field result
	 */
	public static SelectedElement forField(final String classId, final String fieldId) {
		return new SelectedElement(SelectedType.FIELD, classId, fieldId, null, null);
	}

	/**
	 * Creates the default style for comment elements.
	 *
	 * @param commentId id of the comment to look up or modify
	 * @return the for comment result
	 */
	public static SelectedElement forComment(final String commentId) {
		return new SelectedElement(SelectedType.COMMENT, null, null, commentId, null);
	}

	/**
	 * Creates the default style for link elements.
	 *
	 * @param linkId id of the link to look up or modify
	 * @return the for link result
	 */
	public static SelectedElement forLink(final String linkId) {
		return new SelectedElement(SelectedType.LINK, null, null, null, linkId);
	}

	/**
	 * Returns the actual ID on the active canvas.
	 *
	 * @return the actual ID
	 */
	public String getActualId() {
		return switch (this.type) {
		case CLASS -> this.classId;
		case FIELD -> this.fieldId;
		case COMMENT -> this.commentId;
		case LINK -> this.linkId;
		default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
		};
	}

	/**
	 * Computes the hash code that matches this object's equality rules on the active canvas.
	 *
	 * @return the hash code for this object
	 */
	@Override
	public final int hashCode() {
		return Objects.hash(this.type, this.getActualId());
	}

	/**
	 * Converts this selected element into a live edit element.
	 *
	 * @param alternative whether alternative is enabled
	 * @return the as live edit element result
	 */
	public LiveEditElement asLiveEditElement(final boolean alternative) {
		return new LiveEditElement(this.type.asLiveEditType(), this.classId, this.fieldId, this.commentId, this.linkId, alternative);
	}

	/**
	 * Converts this selected element into a live edit element.
	 *
	 * @param alternative whether alternative is enabled
	 * @param style       whether style is enabled
	 * @return the as live edit element result
	 */
	public LiveEditElement asLiveEditElement(final boolean alternative, final boolean style) {
		return new LiveEditElement(style ? this.type.asLiveEditType().asStyle()
				: this.type.asLiveEditType(), this.classId, this.fieldId, this.commentId, this.linkId, alternative);
	}

	/**
	 * Converts this selected element into a style edit target.
	 *
	 * @param alternative  whether alternative is enabled
	 * @param currentStyle current style value used by the operation
	 * @return the as style edit element result
	 */
	public LiveEditElement asStyleEditElement(final boolean alternative, final Object currentStyle) {
		return new LiveEditElement(this.type.asLiveEditType()
				.asStyle(), this.classId, this.fieldId, this.commentId, this.linkId, alternative, currentStyle);
	}

	/**
	 * Compares this selected element with another object for value equality on the active canvas.
	 *
	 * @param other other value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public final boolean equals(final Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}

		return ((SelectedElement) other).type == this.type && Objects.equals(((SelectedElement) other).getActualId(), this.getActualId());
	}

}
