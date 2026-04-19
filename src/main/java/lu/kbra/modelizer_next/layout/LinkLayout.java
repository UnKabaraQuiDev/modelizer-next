package lu.kbra.modelizer_next.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LinkLayout {

	private String linkId;
	private List<Point2D.Double> bendPoints;
	private Point2D.Double nameLabelPosition;

	public LinkLayout() {
		this.linkId = "";
		this.bendPoints = new ArrayList<>();
		this.nameLabelPosition = null;
	}

	public String getLinkId() {
		return this.linkId;
	}

	public void setLinkId(final String linkId) {
		this.linkId = linkId;
	}

	public List<Point2D.Double> getBendPoints() {
		return this.bendPoints;
	}

	public void setBendPoints(final List<Point2D.Double> bendPoints) {
		this.bendPoints = bendPoints;
	}

	public Point2D.Double getNameLabelPosition() {
		return this.nameLabelPosition;
	}

	public void setNameLabelPosition(final Point2D.Double nameLabelPosition) {
		this.nameLabelPosition = nameLabelPosition;
	}

	@Override
	public String toString() {
		return "LinkLayout@" + System.identityHashCode(this) + " [linkId=" + this.linkId + ", bendPoints=" + this.bendPoints
				+ ", nameLabelPosition=" + this.nameLabelPosition + "]";
	}

}
