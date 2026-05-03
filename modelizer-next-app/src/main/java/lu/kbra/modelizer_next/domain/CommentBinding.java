package lu.kbra.modelizer_next.domain;

import lu.kbra.modelizer_next.domain.data.BoundTargetType;

public class CommentBinding {

	private BoundTargetType targetType;
	private String targetId;

	public CommentBinding() {
		this.targetType = BoundTargetType.CLASS;
		this.targetId = "";
	}

	public CommentBinding(final BoundTargetType targetType, final String targetId) {
		this.targetType = targetType;
		this.targetId = targetId;
	}

	public String getTargetId() {
		return this.targetId;
	}

	public BoundTargetType getTargetType() {
		return this.targetType;
	}

	public void setTargetId(final String targetId) {
		this.targetId = targetId;
	}

	public void setTargetType(final BoundTargetType targetType) {
		this.targetType = targetType;
	}

	@Override
	public String toString() {
		return "CommentBinding@" + System.identityHashCode(this) + " [targetType=" + this.targetType + ", targetId=" + this.targetId + "]";
	}

}
