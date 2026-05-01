package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassModel {

	private String id;
	private ElementNames names;
	private LayerVisibility visibility;
	private ElementStyle style;
	private List<FieldModel> fields;

	public ClassModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.visibility = new LayerVisibility();
		this.style = ElementStyle.forClass();
		this.fields = new ArrayList<>();
	}

	public List<FieldModel> getFields() {
		return this.fields;
	}

	public String getId() {
		return this.id;
	}

	public ElementNames getNames() {
		return this.names;
	}

	public ElementStyle getStyle() {
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

	public void setNames(final ElementNames names) {
		this.names = names;
	}

	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	public int getFieldIndex(String fieldId) {
		for (int i = 0; i < fields.size(); i++) {
			if (fieldId.equals(fields.get(i).getId())) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public String toString() {
		return "ClassModel@" + System.identityHashCode(this) + " [id=" + id + ", names=" + names + ", visibility=" + visibility + ", style="
				+ style + ", fields=" + fields + "]";
	}

}
