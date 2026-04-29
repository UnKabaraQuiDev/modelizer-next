package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassModel {

	private String id;
	private ClassNames names;
	private LayerVisibility visibility;
	private ClassStyle style;
	private List<FieldModel> fields;

	public ClassModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ClassNames();
		this.visibility = new LayerVisibility();
		this.style = new ClassStyle();
		this.fields = new ArrayList<>();
	}

	public List<FieldModel> getFields() {
		return this.fields;
	}

	public String getId() {
		return this.id;
	}

	public ClassNames getNames() {
		return this.names;
	}

	public ClassStyle getStyle() {
		return this.style;
	}

	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	public void setFields(final List<FieldModel> fields) {
		this.fields = fields;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setNames(final ClassNames names) {
		this.names = names;
	}

	public void setStyle(final ClassStyle style) {
		this.style = style;
	}

	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "ClassModel@" + System.identityHashCode(this) + " [id=" + id + ", names=" + names + ", visibility=" + visibility + ", style="
				+ style + ", fields=" + fields + "]";
	}

}
