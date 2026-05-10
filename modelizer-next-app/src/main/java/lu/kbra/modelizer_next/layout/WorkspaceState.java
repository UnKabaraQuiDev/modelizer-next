package lu.kbra.modelizer_next.layout;

import java.util.EnumMap;
import java.util.Map;

/**
 * Persistent state of the visible workspace, including active panels and docking layout.
 */
public class WorkspaceState {

	/**
	 * Creates a default.
	 *
	 * @return the created default
	 */
	public static WorkspaceState createDefault() {
		return new WorkspaceState();
	}

	private DockLayoutState dockLayout;

	private Map<PanelType, PanelState> panels;

	/**
	 * Creates a workspace state instance.
	 */
	public WorkspaceState() {
		this.dockLayout = DockLayoutState.createDefault();
		this.panels = new EnumMap<>(PanelType.class);
		this.panels.put(PanelType.CONCEPTUAL, new PanelState());
		this.panels.put(PanelType.LOGICAL, new PanelState());
		this.panels.put(PanelType.PHYSICAL, new PanelState());
	}

	/**
	 * Returns the dock layout.
	 *
	 * @return the dock layout
	 */
	public DockLayoutState getDockLayout() {
		return this.dockLayout;
	}

	/**
	 * Returns the panels.
	 *
	 * @return the panels
	 */
	public Map<PanelType, PanelState> getPanels() {
		return this.panels;
	}

	/**
	 * Sets the dock layout.
	 *
	 * @param dockLayout layout object to read or modify
	 */
	public void setDockLayout(final DockLayoutState dockLayout) {
		this.dockLayout = dockLayout;
	}

	/**
	 * Sets the panels.
	 *
	 * @param panels panels value used by the operation
	 */
	public void setPanels(final Map<PanelType, PanelState> panels) {
		this.panels = panels;
	}

	/**
	 * Builds a debug string for this workspace state.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "WorkspaceState@" + System.identityHashCode(this) + " [dockLayout=" + this.dockLayout + ", panels=" + this.panels + "]";
	}

}
