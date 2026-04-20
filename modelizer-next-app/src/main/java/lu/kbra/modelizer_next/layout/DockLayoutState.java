package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;

public class DockLayoutState {

	public static DockLayoutState createDefault() {
		final DockLayoutState state = new DockLayoutState();
		state.getTabGroups().add(DockedTabGroupState.createDefault());
		return state;
	}

	private List<DockedTabGroupState> tabGroups;

	public DockLayoutState() {
		this.tabGroups = new ArrayList<>();
	}

	public List<DockedTabGroupState> getTabGroups() {
		return this.tabGroups;
	}

	public void setTabGroups(final List<DockedTabGroupState> tabGroups) {
		this.tabGroups = tabGroups;
	}

	@Override
	public String toString() {
		return "DockLayoutState@" + System.identityHashCode(this) + " [tabGroups=" + this.tabGroups + "]";
	}

}
