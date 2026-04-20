package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;

public class PanelState {

	private double zoom;
	private double panX;
	private double panY;
	private List<NodeLayout> nodeLayouts;
	private List<LinkLayout> linkLayouts;

	public PanelState() {
		this.zoom = 1.0;
		this.panX = 0.0;
		this.panY = 0.0;
		this.nodeLayouts = new ArrayList<>();
		this.linkLayouts = new ArrayList<>();
	}

	public List<LinkLayout> getLinkLayouts() {
		return this.linkLayouts;
	}

	public List<NodeLayout> getNodeLayouts() {
		return this.nodeLayouts;
	}

	public double getPanX() {
		return this.panX;
	}

	public double getPanY() {
		return this.panY;
	}

	public double getZoom() {
		return this.zoom;
	}

	public void setLinkLayouts(final List<LinkLayout> linkLayouts) {
		this.linkLayouts = linkLayouts;
	}

	public void setNodeLayouts(final List<NodeLayout> nodeLayouts) {
		this.nodeLayouts = nodeLayouts;
	}

	public void setPanX(final double panX) {
		this.panX = panX;
	}

	public void setPanY(final double panY) {
		this.panY = panY;
	}

	public void setZoom(final double zoom) {
		this.zoom = zoom;
	}

	@Override
	public String toString() {
		return "PanelState@" + System.identityHashCode(this) + " [zoom=" + this.zoom + ", panX=" + this.panX + ", panY=" + this.panY
				+ ", nodeLayouts=" + this.nodeLayouts + ", linkLayouts=" + this.linkLayouts + "]";
	}

}
