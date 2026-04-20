package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DockedTabGroupState {

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

	public DockedTabGroupState() {
		this.id = UUID.randomUUID().toString();
		this.x = 0.0;
		this.y = 0.0;
		this.width = 1.0;
		this.height = 1.0;
		this.tabs = new ArrayList<>();
		this.selectedTab = PanelType.CONCEPTUAL;
	}

	public double getHeight() {
		return this.height;
	}

	public String getId() {
		return this.id;
	}

	public PanelType getSelectedTab() {
		return this.selectedTab;
	}

	public List<PanelType> getTabs() {
		return this.tabs;
	}

	public double getWidth() {
		return this.width;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public void setHeight(final double height) {
		this.height = height;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setSelectedTab(final PanelType selectedTab) {
		this.selectedTab = selectedTab;
	}

	public void setTabs(final List<PanelType> tabs) {
		this.tabs = tabs;
	}

	public void setWidth(final double width) {
		this.width = width;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public void setY(final double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "DockedTabGroupState@" + System.identityHashCode(this) + " [id=" + this.id + ", x=" + this.x + ", y=" + this.y + ", width="
				+ this.width + ", height=" + this.height + ", tabs=" + this.tabs + ", selectedTab=" + this.selectedTab + "]";
	}

}
