package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;

/**
 * Persistent model of a class field or table column, including names, data type, keys, cardinality,
 * style, and visibility.
 */
public class FieldModel implements NamesOwner, IdOwner, StyleOwner {

	public static final String[] SQL_TYPES = { null, "INT", "BIGINT", "TEXT", "BOOLEAN", "TINYINT", "DATE", "TIMESTAMP" };

	public static final String NOT_NULL_FLAG = "NN";
	public static final String PRIMARY_KEY_FLAG = "PK";
	public static final String UNIQUE_FLAG = "UQ";

	private String id;
	private ElementNames names;
	@JsonAlias("notConceptual")
	private boolean technicalOnly;
	private ElementStyle style;
	private boolean primaryKey;
	private boolean unique;
	private boolean notNull;
	private String type;

	/**
	 * Creates a field model instance.
	 */
	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.technicalOnly = false;
		this.style = ElementStyle.forField();
		this.primaryKey = false;
		this.unique = false;
		this.notNull = false;
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
	 * Returns the flags.
	 *
	 * @return the flags
	 */
	public List<String> getFlags() {
		final List<String> ll = new ArrayList<>();
		if (this.primaryKey) {
			ll.add(FieldModel.PRIMARY_KEY_FLAG);
		}
		if (this.notNull) {
			ll.add(FieldModel.NOT_NULL_FLAG);
		}
		if (this.unique) {
			ll.add(FieldModel.UNIQUE_FLAG);
		}
		return ll;
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
	 * Returns the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Checks whether this object has a flags.
	 *
	 * @return {@code true} if flags exists; otherwise {@code false}
	 */
	public boolean hasFlags() {
		return this.primaryKey || this.notNull || this.unique;
	}

	/**
	 * Checks whether not null is enabled or applies.
	 *
	 * @return {@code true} if not null is enabled or applies; otherwise {@code false}
	 */
	public boolean isNotNull() {
		return this.notNull;
	}

	/**
	 * Checks whether primary key is enabled or applies.
	 *
	 * @return {@code true} if primary key is enabled or applies; otherwise {@code false}
	 */
	public boolean isPrimaryKey() {
		return this.primaryKey;
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
	 * Checks whether unique is enabled or applies.
	 *
	 * @return {@code true} if unique is enabled or applies; otherwise {@code false}
	 */
	public boolean isUnique() {
		return this.unique;
	}

	/**
	 * Sets the border color.
	 *
	 * @param c c value used by the operation
	 */
	@Deprecated
	@Override
	public void setBorderColor(final Color c) {
		StyleOwner.super.setBorderColor(c);
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
	 * Sets the not null.
	 *
	 * @param notNull whether not null is enabled
	 */
	public void setNotNull(final boolean notNull) {
		this.notNull = notNull;
	}

	/**
	 * Sets the primary key.
	 *
	 * @param primaryKey whether primary key is enabled
	 */
	public void setPrimaryKey(final boolean primaryKey) {
		this.primaryKey = primaryKey;
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

	/**
	 * Sets the unique.
	 *
	 * @param unique whether unique is enabled
	 */
	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	/**
	 * Builds a debug string for this field model.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "FieldModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", notConceptual="
				+ this.technicalOnly + ", style=" + this.style + ", primaryKey=" + this.primaryKey + ", unique=" + this.unique
				+ ", notNull=" + this.notNull + ", type=" + this.type + "]";
	}

}
