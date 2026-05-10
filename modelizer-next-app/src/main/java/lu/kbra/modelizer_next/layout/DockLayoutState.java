package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent layout state for docked tab groups in the main frame.
 */
public class DockLayoutState {

	/**
	 * Creates a default.
	 *
	 * @return the created default
	 */
	public static DockLayoutState createDefault() {
		final DockLayoutState state = new DockLayoutState();
		state.getTabGroups().add(DockedTabGroupState.createDefault());
		return state;
	}

	private List<DockedTabGroupState> tabGroups;

	/**
	 * Creates a dock layout state instance.
	 */
	public DockLayoutState() {
		this.tabGroups = new ArrayList<>();
	}

	/**
	 * Returns the tab groups.
	 *
	 * @return the tab groups
	 */
	public List<DockedTabGroupState> getTabGroups() {
		return this.tabGroups;
	}

	/**
	 * Sets the tab groups.
	 *
	 * @param tabGroups values for tab groups
	 */
	public void setTabGroups(final List<DockedTabGroupState> tabGroups) {
		this.tabGroups = tabGroups;
	}

	/**
	 * Builds a debug string for this dock layout state.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "DockLayoutState@" + System.identityHashCode(this) + " [tabGroups=" + this.tabGroups + "]";
	}

}
