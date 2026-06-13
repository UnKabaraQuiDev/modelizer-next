package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.ui.canvas.datastruct.FieldTags;

/**
 * Persistent model of a class field or table column, including names, data type, keys, cardinality,
 * style, and visibility.
 */
public class FieldModel implements NamesOwner, IdOwner, StyleOwner, TagsOwner {

	public static final String[] SQL_TYPES = { null, "INT", "BIGINT", "TEXT", "BOOLEAN", "TINYINT", "DATE", "TIMESTAMP" };

	private String id;
	private ElementNames names;
	@JsonAlias("notConceptual")
	private boolean technicalOnly;
	private ElementStyle style;
	private FieldTags tags;
	private String type;

	@JsonIgnore
	private String lastPaletteName;

	/**
	 * Creates a field model instance.
	 */
	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.tags = new FieldTags();
		this.technicalOnly = false;
		this.style = ElementStyle.forField();
		this.type = null;
	}

	/**
	 * Returns the border color.
	 *
	 * @return the border color
	 */
	@Deprecated
	@Override
	public Color getBorderColor() {
		return StyleOwner.super.getBorderColor();
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

	@Override
	public String getLastPaletteName() {
		return this.lastPaletteName;
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

	@Override
	public FieldTags getTags() {
		return this.tags;
	}

	/**
	 * Returns the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Checks whether technical only is enabled or applies.
	 *
	 * @return {@code true} if technical only is enabled or applies; otherwise {@code false}
	 */
	public boolean isTechnicalOnly() {
		return this.technicalOnly;
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

	@Override
	public void setLastPaletteName(final String lastPaletteName) {
		this.lastPaletteName = lastPaletteName;
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

	@JsonProperty("notNull")
	@Deprecated
	public void setNotNullLegacy(final boolean notNull) {
		this.setNonNull(notNull);
	}

	@JsonProperty("primaryKey")
	@Deprecated
	public void setPrimaryKeyLegacy(final boolean primaryKey) {
		this.setPrimaryKey(primaryKey);
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

	@Override
	public void setTags(final FieldTags fieldTagsData) {
		this.tags = fieldTagsData;
	}

	/**
	 * Sets the technical only.
	 *
	 * @param notConceptual whether not conceptual is enabled
	 */
	public void setTechnicalOnly(final boolean notConceptual) {
		this.technicalOnly = notConceptual;
	}

	/**
	 * Sets the type.
	 *
	 * @param type type value that selects the operation mode
	 */
	public void setType(final String type) {
		this.type = type;
	}

	@JsonProperty("unique")
	@Deprecated
	public void setUniqueLegacy(final boolean unique) {
		this.setUnique(unique);
	}

	@Override
	public String toString() {
		return "FieldModel [id=" + this.id + ", names=" + this.names + ", technicalOnly=" + this.technicalOnly + ", style=" + this.style
				+ ", tags=" + this.tags + ", type=" + this.type + ", lastPaletteName=" + this.lastPaletteName + "]";
	}

}
