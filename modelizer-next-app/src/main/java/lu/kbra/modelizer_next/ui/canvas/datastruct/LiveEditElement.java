package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import javax.swing.JComponent;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

public record LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId, boolean forceAlternative,
		Object snapshotValue) {

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

		public boolean isClass() {
			return switch (this) {
			case CLASS, CLASS_FIELD, CLASS_FIELD_STYLE, CLASS_STYLE -> true;
			default -> false;
			};
		}

		public boolean isComment() {
			return this == COMMENT;
		}

		public boolean isLink() {
			return switch (this) {
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_STYLE, LINK_FROM_FIELD,
					LINK_TO_FIELD ->
				true;
			default -> false;
			};
		}

		public boolean isStyle() {
			return switch (this) {
			case CLASS_STYLE, CLASS_FIELD_STYLE, COMMENT_STYLE, LINK_STYLE -> true;
			default -> false;
			};
		}

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

	public LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId) {
		this(type, classId, fieldId, commentId, linkId, false, null);
	}

	public LiveEditElement(LiveEditType type, String classId, String fieldId, String commentId, String linkId, boolean forceAlternative) {
		this(type, classId, fieldId, commentId, linkId, forceAlternative, null);
	}

	public static LiveEditElement forClass(final String classId) {
		return new LiveEditElement(LiveEditType.CLASS, classId, null, null, null);
	}

	public static LiveEditElement forField(final String classId, final String fieldId) {
		return new LiveEditElement(LiveEditType.CLASS_FIELD, classId, fieldId, null, null);
	}

	public static LiveEditElement forClass(final String classId, final boolean alternative) {
		return new LiveEditElement(LiveEditType.CLASS, classId, null, null, null, alternative);
	}

	public static LiveEditElement forField(final String classId, final String fieldId, final boolean alternative) {
		return new LiveEditElement(LiveEditType.CLASS_FIELD, classId, fieldId, null, null, alternative);
	}

	public static LiveEditElement forComment(final String commentId) {
		return new LiveEditElement(LiveEditType.COMMENT, null, null, commentId, null);
	}

	public static LiveEditElement forLink(final String linkId) {
		return new LiveEditElement(LiveEditType.LINK_LABEL, null, null, null, linkId);
	}

	public static LiveEditElement forLink(final String linkId, final LiveEditType type) {
		if (!type.isLink()) {
			throw new IllegalArgumentException("Type isn't applicable to a link: " + type);
		}
		return new LiveEditElement(type, null, null, null, linkId);
	}

	public String getActualId() {
		return switch (this.type) {
		case CLASS, CLASS_STYLE -> this.classId;
		case CLASS_FIELD, CLASS_FIELD_STYLE -> this.fieldId;
		case COMMENT, COMMENT_STYLE -> this.commentId;
		case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL, LINK_STYLE -> this.linkId;
		default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
		};
	}

	@Override
	public final int hashCode() {
		return Objects.hash(this.type, this.getActualId());
	}

	public SelectedElement asSelectedElement() {
		return new SelectedElement(this.type.asSelectedType(), this.classId, this.fieldId, this.commentId, this.linkId);
	}

	@Override
	public final boolean equals(final Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}
		return ((LiveEditElement) other).type == this.type && Objects.equals(((LiveEditElement) other).getActualId(), this.getActualId())
				&& this.forceAlternative == ((LiveEditElement) other).forceAlternative;
	}

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
