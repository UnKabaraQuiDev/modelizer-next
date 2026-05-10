package lu.kbra.modelizer_next.domain;

import lu.kbra.modelizer_next.domain.data.BoundTargetType;

/**
 * Describes how a comment is bound to another model element or field.
 */
public class CommentBinding {

	private BoundTargetType targetType;
	private String targetId;

	/**
	 * Creates a comment binding instance.
	 */
	public CommentBinding() {
		this.targetType = BoundTargetType.CLASS;
		this.targetId = "";
	}

	/**
	 * Creates a comment binding instance.
	 * @param targetType type value to use
	 * @param targetId id of the element to read or modify
	 */
	public CommentBinding(final BoundTargetType targetType, final String targetId) {
		this.targetType = targetType;
		this.targetId = targetId;
	}

	/**
	 * Returns the target ID.
	 * @return the target ID
	 */
	public String getTargetId() {
		return this.targetId;
	}

	/**
	 * Returns the target type.
	 * @return the target type
	 */
	public BoundTargetType getTargetType() {
		return this.targetType;
	}

	/**
	 * Sets the target ID.
	 * @param targetId id of the element to read or modify
	 */
	public void setTargetId(final String targetId) {
		this.targetId = targetId;
	}

	/**
	 * Sets the target type.
	 * @param targetType type value to use
	 */
	public void setTargetType(final BoundTargetType targetType) {
		this.targetType = targetType;
	}

	/**
	 * Builds a debug string for this comment binding.
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "CommentBinding@" + System.identityHashCode(this) + " [targetType=" + this.targetType + ", targetId=" + this.targetId + "]";
	}

}
