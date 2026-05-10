package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistent state of one docked tab group and the panels it contains.
 */
public class DockedTabGroupState {

	/**
	 * Creates a default.
	 *
	 * @return the created default
	 */
	public static DockedTabGroupState createDefault() {
		final DockedTabGroupState state = new DockedTabGroupState();
		state.getTabs().add(PanelType.CONCEPTUAL);
		state.getTabs().add(PanelType.LOGICAL);
		state.getTabs().add(PanelType.PHYSICAL);
		state.setSelectedTab(PanelType.CONCEPTUAL);
		return state;
	}

	private String id;
	private double x;
	private double y;
	private double width;
	private double height;
	private List<PanelType> tabs;

	private PanelType selectedTab;

	/**
	 * Creates a docked tab group state instance.
	 */
	public DockedTabGroupState() {
		this.id = UUID.randomUUID().toString();
		this.x = 0.0;
		this.y = 0.0;
		this.width = 1.0;
		this.height = 1.0;
		this.tabs = new ArrayList<>();
		this.selectedTab = PanelType.CONCEPTUAL;
	}

	/**
	 * Returns the height.
	 *
	 * @return the height
	 */
	public double getHeight() {
		return this.height;
	}

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the selected tab.
	 *
	 * @return the selected tab
	 */
	public PanelType getSelectedTab() {
		return this.selectedTab;
	}

	/**
	 * Returns the tabs.
	 *
	 * @return the tabs
	 */
	public List<PanelType> getTabs() {
		return this.tabs;
	}

	/**
	 * Returns the width.
	 *
	 * @return the width
	 */
	public double getWidth() {
		return this.width;
	}

	/**
	 * Returns the x.
	 *
	 * @return the x
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y.
	 *
	 * @return the y
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Sets the height.
	 *
	 * @param height height value
	 */
	public void setHeight(final double height) {
		this.height = height;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the selected tab.
	 *
	 * @param selectedTab selected tab value used by the operation
	 */
	public void setSelectedTab(final PanelType selectedTab) {
		this.selectedTab = selectedTab;
	}

	/**
	 * Sets the tabs.
	 *
	 * @param tabs values for tabs
	 */
	public void setTabs(final List<PanelType> tabs) {
		this.tabs = tabs;
	}

	/**
	 * Sets the width.
	 *
	 * @param width width value
	 */
	public void setWidth(final double width) {
		this.width = width;
	}

	/**
	 * Sets the x.
	 *
	 * @param x x coordinate
	 */
	public void setX(final double x) {
		this.x = x;
	}

	/**
	 * Sets the y.
	 *
	 * @param y y coordinate
	 */
	public void setY(final double y) {
		this.y = y;
	}

	/**
	 * Builds a debug string for this docked tab group state.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "DockedTabGroupState@" + System.identityHashCode(this) + " [id=" + this.id + ", x=" + this.x + ", y=" + this.y + ", width="
				+ this.width + ", height=" + this.height + ", tabs=" + this.tabs + ", selectedTab=" + this.selectedTab + "]";
	}

}
