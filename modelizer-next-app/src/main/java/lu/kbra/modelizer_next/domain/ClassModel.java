package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.impl.VisibilityOwner;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.domain.shared.LayerVisibility;
import lu.kbra.modelizer_next.layout.PanelType;

import lombok.Data;
import lombok.ToString;

/**
 * Persistent model of a diagram class/table. It owns display names, style, visibility, and the
 * ordered list of fields.
 */
@Data
@ToString
public class ClassModel implements VisibilityOwner, IdOwner, StyleOwner, NamesOwner {

	private String id;
	private ElementNames names;
	private LayerVisibility visibility;
	private ElementStyle style;
	private List<FieldModel> fields;

	@JsonIgnore
	private Map<String, FieldModel> fieldById = new HashMap<>();
	@JsonIgnore
	private Set<String> primaryKeyFieldIds = new HashSet<>();
	@JsonIgnore
	private String lastPaletteName;
	@JsonIgnore
	private Map<PanelType, Set<String>> duplicatedFields = new HashMap<>();

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

	public void addField(final FieldModel fieldModel) {
		this.fields.add(fieldModel);
		this.fieldById.put(fieldModel.getId(), fieldModel);
	}

	public void addField(final int index, final FieldModel fieldModel) {
		this.fields.add(index, fieldModel);
		this.fieldById.put(fieldModel.getId(), fieldModel);
	}

	public Map<String, FieldModel> buildFieldByIdIndex() {
		this.fieldById.clear();
		this.fields.stream().forEach(f -> this.fieldById.put(f.getId(), f));
		return this.fieldById;
	}

	public Set<String> buildPrimaryKeyFieldIdsIndex() {
		this.primaryKeyFieldIds.clear();
		this.fields.stream().filter(FieldModel::isPrimaryKey).map(FieldModel::getId).forEach(this.primaryKeyFieldIds::add);
		return this.primaryKeyFieldIds;
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
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the fields
	 */
	public List<FieldModel> getFields(final PanelType panelType) {
		final List<FieldModel> result = new ArrayList<>();

		for (final FieldModel field : this.fields) {
			if (!field.isTechnicalOnly() || panelType.isTechnical()) {
				result.add(field);
			}
		}

		return result;
	}

	public void removeField(final FieldModel fieldModel) {
		this.fields.remove(fieldModel);
		this.fieldById.remove(fieldModel.getId());
	}

	public void setFieldById(final Map<String, FieldModel> fieldById) {
		this.fieldById = fieldById;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields values for fields
	 */
	@JsonProperty("fields")
	public void setFields(final List<FieldModel> fields) {
		this.fields = fields;
		this.buildFieldByIdIndex();
		this.buildPrimaryKeyFieldIdsIndex();
	}

	public Map<String, FieldModel> validateFieldByIdIndex() {
		if (this.fieldById == null) {
			this.fieldById = new HashMap<>();
		}
		if (this.fieldById.size() != this.fields.size()) {
			this.buildFieldByIdIndex();
		}
		return this.fieldById;
	}

	public Set<String> validatePrimaryKeyFieldIdsIndex() {
		if (this.primaryKeyFieldIds == null) {
			this.primaryKeyFieldIds = new HashSet<>();
		}
		this.buildFieldByIdIndex();
		return this.primaryKeyFieldIds;
	}

	public Map<PanelType, Set<String>> validateDuplicatedFields() {
		for (final PanelType pt : PanelType.values()) {
			this.validateDuplicatedFields(pt);
		}
		return this.duplicatedFields;
	}

	public Set<String> validateDuplicatedFields(final PanelType pt) {
		if (this.duplicatedFields == null) {
			this.duplicatedFields = new HashMap<>();
		}
		final Set<String> set = this.duplicatedFields.computeIfAbsent(pt, k -> new HashSet<>());
		set.clear();
		final Set<String> currentNames = new HashSet<>();
		for (final FieldModel f : this.fields) {
			if (!currentNames.add(f.getName(pt))) {
				set.add(f.getId());
			}
		}
		return set;
	}

	public Set<String> getDuplicatedFields(final PanelType pt) {
		if (this.duplicatedFields == null || !this.duplicatedFields.containsKey(pt)) {
			this.validateDuplicatedFields(pt);
		}
		return this.duplicatedFields.get(pt);
	}

}
