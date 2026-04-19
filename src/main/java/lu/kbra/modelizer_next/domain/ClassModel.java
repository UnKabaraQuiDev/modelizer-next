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
	private String comment;
	private List<FieldModel> fields;

	public ClassModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ClassNames();
		this.group = "";
		this.visibility = new LayerVisibility();
		this.style = new ClassStyle();
		this.comment = "";
		this.fields = new ArrayList<>();
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public ClassNames getNames() {
		return this.names;
	}

	public void setNames(final ClassNames names) {
		this.names = names;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(final String group) {
		this.group = group;
	}

	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	public ClassStyle getStyle() {
		return this.style;
	}

	public void setStyle(final ClassStyle style) {
		this.style = style;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public List<FieldModel> getFields() {
		return this.fields;
	}

	public void setFields(final List<FieldModel> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "ClassModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", group="
				+ this.group + ", visibility=" + this.visibility + ", style=" + this.style + ", comment=" + this.comment
				+ ", fields=" + this.fields + "]";
	}

}
