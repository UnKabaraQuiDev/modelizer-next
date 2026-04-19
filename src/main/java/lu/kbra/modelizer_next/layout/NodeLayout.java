package lu.kbra.modelizer_next.layout;

import lu.kbra.modelizer_next.common.Point2;
import lu.kbra.modelizer_next.common.Size2;

public class NodeLayout {

	private LayoutObjectType objectType;
	private String objectId;
	private Point2 position;
	private Size2 size;

	public NodeLayout() {
		this.objectType = LayoutObjectType.CLASS;
		this.objectId = "";
		this.position = new Point2();
		this.size = new Size2();
	}

	public LayoutObjectType getObjectType() {
		return this.objectType;
	}

	public void setObjectType(final LayoutObjectType objectType) {
		this.objectType = objectType;
	}

	public String getObjectId() {
		return this.objectId;
	}

	public void setObjectId(final String objectId) {
		this.objectId = objectId;
	}

	public Point2 getPosition() {
		return this.position;
	}

	public void setPosition(final Point2 position) {
		this.position = position;
	}

	public Size2 getSize() {
		return this.size;
	}

	public void setSize(final Size2 size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "NodeLayout@" + System.identityHashCode(this) + " [objectType=" + objectType + ", objectId=" + objectId
				+ ", position=" + position + ", size=" + size + "]";
	}

}
