package lu.kbra.modelizer_next.domain;

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

	public BoundTargetType getTargetType() {
		return this.targetType;
	}

	public void setTargetType(final BoundTargetType targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return this.targetId;
	}

	public void setTargetId(final String targetId) {
		this.targetId = targetId;
	}

	@Override
	public String toString() {
		return "CommentBinding@" + System.identityHashCode(this) + " [targetType=" + targetType + ", targetId="
				+ targetId + "]";
	}

}
