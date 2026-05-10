package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent state for one workspace panel.
 */
public class PanelState {

	private double zoom;
	private double panX;
	private double panY;
	private List<NodeLayout> nodeLayouts;
	private List<LinkLayout> linkLayouts;

	/**
	 * Creates a panel state instance.
	 */
	public PanelState() {
		this.zoom = 1.0;
		this.panX = 0.0;
		this.panY = 0.0;
		this.nodeLayouts = new ArrayList<>();
		this.linkLayouts = new ArrayList<>();
	}

	/**
	 * Returns the link layouts.
	 *
	 * @return the link layouts
	 */
	public List<LinkLayout> getLinkLayouts() {
		return this.linkLayouts;
	}

	/**
	 * Returns the node layouts.
	 *
	 * @return the node layouts
	 */
	public List<NodeLayout> getNodeLayouts() {
		return this.nodeLayouts;
	}

	/**
	 * Returns the pan x.
	 *
	 * @return the pan x
	 */
	public double getPanX() {
		return this.panX;
	}

	/**
	 * Returns the pan y.
	 *
	 * @return the pan y
	 */
	public double getPanY() {
		return this.panY;
	}

	/**
	 * Returns the zoom.
	 *
	 * @return the zoom
	 */
	public double getZoom() {
		return this.zoom;
	}

	/**
	 * Sets the link layouts.
	 *
	 * @param linkLayouts layout objects to read or modify
	 */
	public void setLinkLayouts(final List<LinkLayout> linkLayouts) {
		this.linkLayouts = linkLayouts;
	}

	/**
	 * Sets the node layouts.
	 *
	 * @param nodeLayouts layout objects to read or modify
	 */
	public void setNodeLayouts(final List<NodeLayout> nodeLayouts) {
		this.nodeLayouts = nodeLayouts;
	}

	/**
	 * Sets the pan x.
	 *
	 * @param panX numeric pan x value
	 */
	public void setPanX(final double panX) {
		this.panX = panX;
	}

	/**
	 * Sets the pan y.
	 *
	 * @param panY numeric pan y value
	 */
	public void setPanY(final double panY) {
		this.panY = panY;
	}

	/**
	 * Sets the zoom.
	 *
	 * @param zoom numeric zoom value
	 */
	public void setZoom(final double zoom) {
		this.zoom = zoom;
	}

	/**
	 * Builds a debug string for this panel state.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "PanelState@" + System.identityHashCode(this) + " [zoom=" + this.zoom + ", panX=" + this.panX + ", panY=" + this.panY
				+ ", nodeLayouts=" + this.nodeLayouts + ", linkLayouts=" + this.linkLayouts + "]";
	}

}
