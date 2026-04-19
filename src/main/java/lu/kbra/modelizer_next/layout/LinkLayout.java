package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.common.Point2;

public class LinkLayout {

	private String linkId;
	private List<Point2> bendPoints;
	private Point2 nameLabelPosition;

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

	public List<Point2> getBendPoints() {
		return this.bendPoints;
	}

	public void setBendPoints(final List<Point2> bendPoints) {
		this.bendPoints = bendPoints;
	}

	public Point2 getNameLabelPosition() {
		return this.nameLabelPosition;
	}

	public void setNameLabelPosition(final Point2 nameLabelPosition) {
		this.nameLabelPosition = nameLabelPosition;
	}

	@Override
	public String toString() {
		return "LinkLayout@" + System.identityHashCode(this) + " [linkId=" + linkId + ", bendPoints=" + bendPoints
				+ ", nameLabelPosition=" + nameLabelPosition + "]";
	}

}
