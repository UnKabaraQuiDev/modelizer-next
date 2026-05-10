package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Serializable snapshot of copied classes, comments, links, fields, and layout data.
 *
 * @param panelType diagram panel type whose model or layout should be used
 * @param classes   values for classes
 * @param fields    values for fields
 * @param comments  values for comments
 * @param links     values for links
 */
public record ClipboardSnapshot(PanelType panelType, List<CopiedClass> classes, List<CopiedField> fields, List<CopiedComment> comments,
		List<CopiedLink> links) {

	/**
	 * Checks whether empty is enabled or applies on the active canvas.
	 *
	 * @return {@code true} if empty is enabled or applies; otherwise {@code false}
	 */
	public boolean isEmpty() {
		return this.classes.isEmpty() && this.fields.isEmpty() && this.comments.isEmpty() && this.links.isEmpty();
	}
}
