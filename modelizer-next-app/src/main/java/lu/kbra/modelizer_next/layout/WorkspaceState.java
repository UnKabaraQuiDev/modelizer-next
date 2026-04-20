package lu.kbra.modelizer_next.layout;

import java.util.EnumMap;
import java.util.Map;

public class WorkspaceState {

	public static WorkspaceState createDefault() {
		return new WorkspaceState();
	}
	private DockLayoutState dockLayout;

	private Map<PanelType, PanelState> panels;

	public WorkspaceState() {
		this.dockLayout = DockLayoutState.createDefault();
		this.panels = new EnumMap<>(PanelType.class);
		this.panels.put(PanelType.CONCEPTUAL, new PanelState());
		this.panels.put(PanelType.LOGICAL, new PanelState());
		this.panels.put(PanelType.PHYSICAL, new PanelState());
	}

	public DockLayoutState getDockLayout() {
		return this.dockLayout;
	}

	public Map<PanelType, PanelState> getPanels() {
		return this.panels;
	}

	public void setDockLayout(final DockLayoutState dockLayout) {
		this.dockLayout = dockLayout;
	}

	public void setPanels(final Map<PanelType, PanelState> panels) {
		this.panels = panels;
	}

	@Override
	public String toString() {
		return "WorkspaceState@" + System.identityHashCode(this) + " [dockLayout=" + this.dockLayout + ", panels=" + this.panels + "]";
	}

}
