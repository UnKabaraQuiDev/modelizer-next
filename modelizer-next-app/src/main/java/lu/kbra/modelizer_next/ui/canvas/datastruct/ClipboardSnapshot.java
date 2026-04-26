package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.List;

import lu.kbra.modelizer_next.layout.PanelType;

public record ClipboardSnapshot(PanelType panelType, List<CopiedClass> classes, List<CopiedField> fields, List<CopiedComment> comments,
		List<CopiedLink> links) {

	public boolean isEmpty() {
		return this.classes.isEmpty() && this.fields.isEmpty() && this.comments.isEmpty() && this.links.isEmpty();
	}
}