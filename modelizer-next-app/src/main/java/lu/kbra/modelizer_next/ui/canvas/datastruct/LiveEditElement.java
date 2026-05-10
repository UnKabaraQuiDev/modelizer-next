package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import javax.swing.JComponent;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

/**
 * Model element currently being edited inline on the canvas.
 *
 * @param type             type value that selects the operation mode
 * @param classId          id of the class to look up or modify
 * @param fieldId          id of the field to look up or modify
 * @param commentId        id of the comment to look up or modify
 * @param linkId           id of the link to look up or modify
 * @param forceAlternative whether force alternative is enabled
 * @param snapshotValue    snapshot value value used by the operation
 */
public record LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId, boolean forceAlternative,
		Object snapshotValue) {

	/**
	 * Enumerates supported live edit type values.
	 */
	public enum LiveEditType {

		NONE,
		CLASS,
		CLASS_FIELD,
		COMMENT,
		LINK_LABEL,
		LINK_FROM_CARDINALITY,
		LINK_FROM_LABEL,
		LINK_TO_CARDINALITY,
		LINK_TO_LABEL,
		@Deprecated
		LINK_FROM_FIELD,
		@Deprecated
		LINK_TO_FIELD,

		CLASS_STYLE,
		CLASS_FIELD_STYLE,
		COMMENT_STYLE,
		LINK_STYLE;

		/**
		 * Converts this live edit type to the matching selection type.
		 *
		 * @return the as selected type result
		 */
		public SelectedType asSelectedType() {
			return switch (this) {
			case CLASS, CLASS_STYLE -> SelectedType.CLASS;
			case CLASS_FIELD, CLASS_FIELD_STYLE -> SelectedType.FIELD;
			case COMMENT, COMMENT_STYLE -> SelectedType.COMMENT;
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_FROM_FIELD, LINK_TO_FIELD,
					LINK_STYLE ->
				SelectedType.LINK;
			default -> throw new IllegalArgumentException("Unsupported option: " + this);
			};
		}

		/**
		 * Converts this live edit type to the matching style scope.
		 *
		 * @return the as style result
		 */
		public LiveEditType asStyle() {
			return switch (this) {
			case CLASS -> CLASS_STYLE;
			case CLASS_FIELD -> CLASS_FIELD_STYLE;
			case COMMENT -> COMMENT_STYLE;
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_FROM_FIELD, LINK_TO_FIELD ->
				LINK_STYLE;
			default -> throw new IllegalArgumentException("Unexpected value: " + this);
			};
		}

		/**
		 * Checks whether class is enabled or applies.
		 *
		 * @return {@code true} if class is enabled or applies; otherwise {@code false}
		 */
		public boolean isClass() {
			return switch (this) {
			case CLASS, CLASS_FIELD, CLASS_FIELD_STYLE, CLASS_STYLE -> true;
			default -> false;
			};
		}

		/**
		 * Checks whether comment is enabled or applies.
		 *
		 * @return {@code true} if comment is enabled or applies; otherwise {@code false}
		 */
		public boolean isComment() {
			return this == COMMENT;
		}

		/**
		 * Checks whether link is enabled or applies.
		 *
		 * @return {@code true} if link is enabled or applies; otherwise {@code false}
		 */
		public boolean isLink() {
			return switch (this) {
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_STYLE, LINK_FROM_FIELD,
					LINK_TO_FIELD ->
				true;
			default -> false;
			};
		}

		/**
		 * Checks whether style is enabled or applies on the active canvas.
		 *
		 * @return {@code true} if style is enabled or applies; otherwise {@code false}
		 */
		public boolean isStyle() {
			return switch (this) {
			case CLASS_STYLE, CLASS_FIELD_STYLE, COMMENT_STYLE, LINK_STYLE -> true;
			default -> false;
			};
		}

		/**
		 * Returns the next value from this producer or iterator on the active canvas.
		 *
		 * @return the next result
		 */
		public LiveEditType next() {
			return switch (this) {
			case LINK_LABEL -> LINK_FROM_CARDINALITY;
			case LINK_FROM_CARDINALITY -> LINK_FROM_LABEL;
			case LINK_FROM_LABEL -> LINK_TO_CARDINALITY;
			case LINK_TO_CARDINALITY -> LINK_TO_LABEL;
			case LINK_TO_LABEL -> LINK_LABEL;
			default -> this;
			};
		}

		/**
		 * Returns the previous live edit type in the cycling order.
		 *
		 * @return the previous result
		 */
		public LiveEditType previous() {
			return switch (this) {
			case LINK_LABEL -> LINK_TO_LABEL;
			case LINK_TO_LABEL -> LINK_TO_CARDINALITY;
			case LINK_TO_CARDINALITY -> LINK_FROM_LABEL;
			case LINK_FROM_LABEL -> LINK_FROM_CARDINALITY;
			case LINK_FROM_CARDINALITY -> LINK_LABEL;
			default -> this;
			};
		}

	}

	/**
	 * Creates a live edit element instance on the active canvas.
	 *
	 * @param type      type value that selects the operation mode
	 * @param classId   id of the class to look up or modify
	 * @param fieldId   id of the field to look up or modify
	 * @param commentId id of the comment to look up or modify
	 * @param linkId    id of the link to look up or modify
	 */
	public LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId) {
		this(type, classId, fieldId, commentId, linkId, false, null);
	}

	/**
	 * Creates a live edit element instance on the active canvas.
	 *
	 * @param type             type value that selects the operation mode
	 * @param classId          id of the class to look up or modify
	 * @param fieldId          id of the field to look up or modify
	 * @param commentId        id of the comment to look up or modify
	 * @param linkId           id of the link to look up or modify
	 * @param forceAlternative whether force alternative is enabled
	 */
	public LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId, boolean forceAlternative) {
		this(type, classId, fieldId, commentId, linkId, forceAlternative, null);
	}

	/**
	 * Creates the default style for class elements.
	 *
	 * @param classId id of the class to look up or modify
	 * @return the for class result
	 */
	public static LiveEditElement forClass(final String classId) {
		return new LiveEditElement(LiveEditType.CLASS, classId, null, null, null);
	}

	/**
	 * Creates the default style for field elements.
	 *
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return the for field result
	 */
	public static LiveEditElement forField(final String classId, final String fieldId) {
		return new LiveEditElement(LiveEditType.CLASS_FIELD, classId, fieldId, null, null);
	}

	/**
	 * Creates the default style for class elements.
	 *
	 * @param classId     id of the class to look up or modify
	 * @param alternative whether alternative is enabled
	 * @return the for class result
	 */
	public static LiveEditElement forClass(final String classId, final boolean alternative) {
		return new LiveEditElement(LiveEditType.CLASS, classId, null, null, null, alternative);
	}

	/**
	 * Creates the default style for field elements.
	 *
	 * @param classId     id of the class to look up or modify
	 * @param fieldId     id of the field to look up or modify
	 * @param alternative whether alternative is enabled
	 * @return the for field result
	 */
	public static LiveEditElement forField(final String classId, final String fieldId, final boolean alternative) {
		return new LiveEditElement(LiveEditType.CLASS_FIELD, classId, fieldId, null, null, alternative);
	}

	/**
	 * Creates the default style for comment elements.
	 *
	 * @param commentId id of the comment to look up or modify
	 * @return the for comment result
	 */
	public static LiveEditElement forComment(final String commentId) {
		return new LiveEditElement(LiveEditType.COMMENT, null, null, commentId, null);
	}

	/**
	 * Creates the default style for link elements.
	 *
	 * @param linkId id of the link to look up or modify
	 * @return the for link result
	 */
	public static LiveEditElement forLink(final String linkId) {
		return new LiveEditElement(LiveEditType.LINK_LABEL, null, null, null, linkId);
	}

	/**
	 * Creates the default style for link elements.
	 *
	 * @param linkId id of the link to look up or modify
	 * @param type   type value that selects the operation mode
	 * @return the for link result
	 */
	public static LiveEditElement forLink(final String linkId, final LiveEditType type) {
		if (!type.isLink()) {
			throw new IllegalArgumentException("Type isn't applicable to a link: " + type);
		}
		return new LiveEditElement(type, null, null, null, linkId);
	}

	/**
	 * Returns the actual ID on the active canvas.
	 *
	 * @return the actual ID
	 */
	public String getActualId() {
		return switch (this.type) {
		case CLASS, CLASS_STYLE -> this.classId;
		case CLASS_FIELD, CLASS_FIELD_STYLE -> this.fieldId;
		case COMMENT, COMMENT_STYLE -> this.commentId;
		case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_STYLE -> this.linkId;
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
	 * Converts this live edit element into a selected element.
	 *
	 * @return the as selected element result
	 */
	public SelectedElement asSelectedElement() {
		return new SelectedElement(this.type.asSelectedType(), this.classId, this.fieldId, this.commentId, this.linkId);
	}

	/**
	 * Compares this live edit element with another object for value equality on the active canvas.
	 *
	 * @param other other value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public final boolean equals(final Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}
		return ((LiveEditElement) other).type == this.type && Objects.equals(((LiveEditElement) other).getActualId(), this.getActualId())
				&& this.forceAlternative == ((LiveEditElement) other).forceAlternative;
	}

	/**
	 * Returns the renaming component on the active canvas.
	 *
	 * @param component Swing component to configure
	 * @return the renaming component
	 */
	public JComponent getRenamingComponent(final LiveEditComponents component) {
		return switch (this.type) {
		case CLASS, CLASS_FIELD, LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> component.textField();
		case COMMENT -> component.textArea();
		case LINK_FROM_CARDINALITY, LINK_TO_CARDINALITY -> component.enumList();
		case CLASS_STYLE, CLASS_FIELD_STYLE, LINK_STYLE, COMMENT_STYLE -> component.paletteList();
		default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
		};
	}

}
