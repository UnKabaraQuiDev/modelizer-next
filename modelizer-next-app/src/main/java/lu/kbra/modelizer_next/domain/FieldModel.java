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

	@Deprecated
	@Override
	public Color getBorderColor() {
		return StyleOwner.super.getBorderColor();
	}

	@Deprecated
	@Override
	public void setBorderColor(Color c) {
		StyleOwner.super.setBorderColor(c);
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

	public String getType() {
		return this.type;
	}

	public boolean isNotNull() {
		return this.notNull;
	}

	public boolean isPrimaryKey() {
		return this.primaryKey;
	}

	public boolean isTechnicalOnly() {
		return this.technicalOnly;
	}

	public boolean isUnique() {
		return this.unique;
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public void setNames(final ElementNames names) {
		this.names = names;
	}

	public void setNotNull(final boolean notNull) {
		this.notNull = notNull;
	}

	public void setPrimaryKey(final boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	public void setTechnicalOnly(final boolean notConceptual) {
		this.technicalOnly = notConceptual;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	public List<String> getFlags() {
		final List<String> ll = new ArrayList<>();
		if (primaryKey) {
			ll.add(PRIMARY_KEY_FLAG);
		}
		if (notNull) {
			ll.add(NOT_NULL_FLAG);
		}
		if (unique) {
			ll.add(UNIQUE_FLAG);
		}
		return ll;
	}

	public boolean hasFlags() {
		return primaryKey || notNull || unique;
	}

	@Override
	public String toString() {
		return "FieldModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", notConceptual="
				+ this.technicalOnly + ", style=" + this.style + ", primaryKey=" + this.primaryKey + ", unique=" + this.unique
				+ ", notNull=" + this.notNull + ", type=" + this.type + "]";
	}

}
