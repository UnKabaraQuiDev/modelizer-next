package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.Objects;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

public record RenamingElement(RenamingType type, String classId, String fieldId, String commentId, String linkId) {

	public enum RenamingType {

		NONE,
		CLASS,
		CLASS_FIELD,
		COMMENT,
		LINK_LABEL,
		LINK_FROM_CARDINALITY,
		LINK_FROM_LABEL,
		LINK_TO_CARDINALITY,
		LINK_TO_LABEL;

		public boolean isClass() {
			return switch (this) {
			case CLASS, CLASS_FIELD -> true;
			default -> false;
			};
		}

		public boolean isComment() {
			return this == COMMENT;
		}

		public boolean isLink() {
			return switch (this) {
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL -> true;
			default -> false;
			};
		}

		public RenamingType next() {
			return switch (this) {
			case LINK_LABEL -> LINK_FROM_CARDINALITY;
			case LINK_FROM_CARDINALITY -> LINK_FROM_LABEL;
			case LINK_FROM_LABEL -> LINK_TO_CARDINALITY;
			case LINK_TO_CARDINALITY -> LINK_TO_LABEL;
			case LINK_TO_LABEL -> LINK_LABEL;
			default -> this;
			};
		}

		public RenamingType previous() {
			return switch (this) {
			case LINK_FROM_CARDINALITY -> LINK_FROM_LABEL;
			case LINK_FROM_LABEL -> LINK_TO_CARDINALITY;
			case LINK_TO_CARDINALITY -> LINK_TO_LABEL;
			case LINK_TO_LABEL -> LINK_LABEL;
			case LINK_LABEL -> LINK_FROM_CARDINALITY;
			default -> this;
			};
		}

		public SelectedType asSelectedType() {
			return switch (this) {
			case CLASS -> SelectedType.CLASS;
			case CLASS_FIELD -> SelectedType.FIELD;
			case COMMENT -> SelectedType.COMMENT;
			case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL -> SelectedType.LINK;
			default -> throw new IllegalArgumentException("Unsupported option: " + this);
			};
		}

	}

	public static RenamingElement forClass(final String classId) {
		return new RenamingElement(RenamingType.CLASS, classId, null, null, null);
	}

	public static RenamingElement forField(final String classId, final String fieldId) {
		return new RenamingElement(RenamingType.CLASS_FIELD, classId, fieldId, null, null);
	}

	public static RenamingElement forComment(final String commentId) {
		return new RenamingElement(RenamingType.COMMENT, null, null, commentId, null);
	}

	public static RenamingElement forLink(final String linkId) {
		return new RenamingElement(RenamingType.LINK_LABEL, null, null, null, linkId);
	}

	public String getActualId() {
		return switch (this.type) {
		case CLASS -> this.classId;
		case CLASS_FIELD -> this.fieldId;
		case COMMENT -> this.commentId;
		case LINK_LABEL, LINK_FROM_CARDINALITY, LINK_FROM_LABEL, LINK_TO_CARDINALITY, LINK_TO_LABEL -> this.linkId;
		default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
		};
	}

	@Override
	public final int hashCode() {
		return Objects.hash(this.type, this.getActualId());
	}

	public SelectedElement asSelectedElement() {
		return new SelectedElement(this.type.asSelectedType(), classId, fieldId, commentId, linkId);
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}
		return ((RenamingElement) other).type == this.type && Objects.equals(((RenamingElement) other).getActualId(), this.getActualId());
	}

}
