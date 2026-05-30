package lu.kbra.modelizer_next.layout;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.common.Size2D;

/**
 * Persistent layout information for a class or comment node on the canvas.
 */
public class NodeLayout {

	private LayoutObjectType objectType;
	private String objectId;
	private Point2D.Double position;
	private Size2D size;

	/**
	 * Creates a node layout instance.
	 */
	public NodeLayout() {
		this.objectType = LayoutObjectType.CLASS;
		this.objectId = "";
		this.position = new Point2D.Double();
		this.size = new Size2D();
	}

	/**
	 * Returns the object ID.
	 *
	 * @return the object ID
	 */
	public String getObjectId() {
		return this.objectId;
	}

	/**
	 * Returns the object type.
	 *
	 * @return the object type
	 */
	public LayoutObjectType getObjectType() {
		return this.objectType;
	}

	/**
	 * Returns the position.
	 *
	 * @return the position
	 */
	public Point2D.Double getPosition() {
		return this.position;
	}

	/**
	 * Returns the size.
	 *
	 * @return the size
	 */
	public Size2D getSize() {
		return this.size;
	}

	@Deprecated
	public void set(final NodeLayout otherLayout) {
		this.objectType = otherLayout.objectType;
		this.objectId = otherLayout.objectId;
		this.position.setLocation(otherLayout.position);
		this.size.setLocation(otherLayout.size);
	}

	public void setLayout(final NodeLayout otherLayout) {
		this.position.setLocation(otherLayout.position);
		this.size.setLocation(otherLayout.size);
	}

	/**
	 * Sets the object ID.
	 *
	 * @param objectId id of the element to read or modify
	 */
	public void setObjectId(final String objectId) {
		this.objectId = objectId;
	}

	/**
	 * Sets the object type.
	 *
	 * @param objectType type value to use
	 */
	public void setObjectType(final LayoutObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * Sets the position.
	 *
	 * @param position position value used by the operation
	 */
	public void setPosition(final Point2D.Double position) {
		this.position = position;
	}

	/**
	 * Sets the size.
	 *
	 * @param size size value used by the operation
	 */
	public void setSize(final Size2D size) {
		this.size = size;
	}

	/**
	 * Builds a debug string for this node layout.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "NodeLayout@" + System.identityHashCode(this) + " [objectType=" + this.objectType + ", objectId=" + this.objectId
				+ ", position=" + this.position + ", size=" + this.size + "]";
	}

}
