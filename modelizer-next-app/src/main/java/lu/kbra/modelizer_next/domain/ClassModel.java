package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.impl.VisibilityOwner;

public class ClassModel implements VisibilityOwner, IdOwner, StyleOwner, NamesOwner {

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

	public int getFieldIndex(final String fieldId) {
		for (int i = 0; i < this.fields.size(); i++) {
			if (fieldId.equals(this.fields.get(i).getId())) {
				return i;
			}
		}

		return -1;
	}

	public List<FieldModel> getFields() {
		return this.fields;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ElementNames getNames() {
		return this.names;
	}

	@Override
	public ElementStyle getStyle() {
		return this.style;
	}

	@Override
	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	public void setFields(final List<FieldModel> fields) {
		this.fields = fields;
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public void setNames(final ElementNames names) {
		this.names = names;
	}

	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	@Override
	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "ClassModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", visibility="
				+ this.visibility + ", style=" + this.style + ", fields=" + this.fields + "]";
	}

}
