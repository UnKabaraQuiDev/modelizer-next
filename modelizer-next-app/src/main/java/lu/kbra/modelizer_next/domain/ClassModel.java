package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.impl.VisibilityOwner;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.domain.shared.LayerVisibility;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Persistent model of a diagram class/table. It owns display names, style, visibility, and the
 * ordered list of fields.
 */
public class ClassModel implements VisibilityOwner, IdOwner, StyleOwner, NamesOwner {

	private String id;
	private ElementNames names;
	private LayerVisibility visibility;
	private ElementStyle style;
	private List<FieldModel> fields;

	/**
	 * Creates a class model instance.
	 */
	public ClassModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.visibility = new LayerVisibility();
		this.style = ElementStyle.forClass();
		this.fields = new ArrayList<>();
	}

	/**
	 * Returns the field.
	 *
	 * @param i         zero-based index to read or update
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the field
	 */
	public FieldModel getField(final int i, final PanelType panelType) {
		int j = 0;

		for (final FieldModel field : this.fields) {
			final boolean visible = !field.isTechnicalOnly() || panelType.isTechnical();

			if (visible) {
				if (j == i) {
					return field;
				}
				j++;
			}
		}

		return null;
	}

	/**
	 * Returns the field count.
	 *
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the field count
	 */
	public int getFieldCount(final PanelType panelType) {
		int count = 0;

		for (final FieldModel field : this.fields) {
			final boolean visible = !field.isTechnicalOnly() || panelType.isTechnical();

			if (visible) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Returns the field index.
	 *
	 * @param fieldId id of the field to look up or modify
	 * @return the field index
	 */
	public int getFieldIndex(final String fieldId) {
		for (int i = 0; i < this.fields.size(); i++) {
			if (fieldId.equals(this.fields.get(i).getId())) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Returns the field index.
	 *
	 * @param fieldId   id of the field to look up or modify
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the field index
	 */
	public int getFieldIndex(final String fieldId, final PanelType panelType) {
		int j = 0;

		for (final FieldModel field : this.fields) {
			final boolean visible = !field.isTechnicalOnly() || panelType.isTechnical();

			if (visible) {
				if (fieldId.equals(field.getId())) {
					return j;
				}
				j++;
			}
		}

		return -1;
	}

	/**
	 * Returns the fields.
	 *
	 * @return the fields
	 */
	public List<FieldModel> getFields() {
		return this.fields;
	}

	/**
	 * Returns the fields.
	 *
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the fields
	 */
	public List<FieldModel> getFields(final PanelType panelType) {
		final List<FieldModel> result = new ArrayList<>();

		for (final FieldModel field : this.fields) {
			final boolean visible = !field.isTechnicalOnly() || panelType.isTechnical();

			if (visible) {
				result.add(field);
			}
		}

		return result;
	}

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the names.
	 *
	 * @return the names
	 */
	@Override
	public ElementNames getNames() {
		return this.names;
	}

	/**
	 * Returns the style.
	 *
	 * @return the style
	 */
	@Override
	public ElementStyle getStyle() {
		return this.style;
	}

	/**
	 * Returns the visibility.
	 *
	 * @return the visibility
	 */
	@Override
	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields values for fields
	 */
	public void setFields(final List<FieldModel> fields) {
		this.fields = fields;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	@Override
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the names.
	 *
	 * @param names name values to use
	 */
	@Override
	public void setNames(final ElementNames names) {
		this.names = names;
	}

	/**
	 * Sets the style.
	 *
	 * @param style style value used by the operation
	 */
	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	/**
	 * Sets the visibility.
	 *
	 * @param visibility visibility value used by the operation
	 */
	@Override
	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * Builds a debug string for this class model.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "ClassModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", visibility="
				+ this.visibility + ", style=" + this.style + ", fields=" + this.fields + "]";
	}

}
