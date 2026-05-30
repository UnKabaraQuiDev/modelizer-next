package lu.kbra.modelizer_next.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent layout information for a link, including bend points and manual routing state.
 */
public class LinkLayout {

	private String linkId;
	// TODO: remove this, because it is unused ?
	private List<Point2D.Double> bendPoints;
	private Point2D.Double nameLabelPosition;

	/**
	 * Creates a link layout instance.
	 */
	public LinkLayout() {
		this.linkId = "";
		this.bendPoints = new ArrayList<>();
		this.nameLabelPosition = null;
	}

	/**
	 * Returns the bend points.
	 *
	 * @return the bend points
	 */
	public List<Point2D.Double> getBendPoints() {
		return this.bendPoints;
	}

	/**
	 * Returns the link ID.
	 *
	 * @return the link ID
	 */
	public String getLinkId() {
		return this.linkId;
	}

	/**
	 * Returns the name label position.
	 *
	 * @return the name label position
	 */
	public Point2D.Double getNameLabelPosition() {
		return this.nameLabelPosition;
	}

	/**
	 * Sets the bend points.
	 *
	 * @param bendPoints points in canvas coordinates
	 */
	public void setBendPoints(final List<Point2D.Double> bendPoints) {
		this.bendPoints = bendPoints;
	}

	/**
	 * Sets the link ID.
	 *
	 * @param linkId id of the link to look up or modify
	 */
	public void setLinkId(final String linkId) {
		this.linkId = linkId;
	}

	/**
	 * Sets the name label position.
	 *
	 * @param nameLabelPosition name label position value used by the operation
	 */
	public void setNameLabelPosition(final Point2D.Double nameLabelPosition) {
		this.nameLabelPosition = nameLabelPosition;
	}

	/**
	 * Builds a debug string for this link layout.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "LinkLayout@" + System.identityHashCode(this) + " [linkId=" + this.linkId + ", bendPoints=" + this.bendPoints
				+ ", nameLabelPosition=" + this.nameLabelPosition + "]";
	}

}
