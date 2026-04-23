package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassModel {

	private String id;
	private ClassNames names;
	private String group;
	private LayerVisibility visibility;
	private ClassStyle style;
	private List<FieldModel> fields;

	public ClassModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ClassNames();
		this.group = "";
		this.visibility = new LayerVisibility();
		this.style = new ClassStyle();
		this.fields = new ArrayList<>();
	}

	public List<FieldModel> getFields() {
		return this.fields;
	}

	public String getGroup() {
		return this.group;
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

	public void setGroup(final String group) {
		this.group = group;
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
		return "ClassModel [id=" + this.id + ", names=" + this.names + ", group=" + this.group + ", visibility=" + this.visibility
				+ ", style=" + this.style + ", fields=" + this.fields + "]";
	}

}
