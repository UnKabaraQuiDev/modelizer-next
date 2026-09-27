package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.NamesOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;

@Data
public class FieldModel implements NamesOwner, IdOwner, StyleOwner, TagsOwner {

	public static final String[] SQL_TYPES = { null, "INT", "BIGINT", "TEXT", "BOOLEAN", "TINYINT", "DATE", "TIMESTAMP" };

	private String id;
	private ElementNames names;
	@JsonAlias("notConceptual")
	private boolean technicalOnly;
	private ElementStyle style;
	private FieldTags tags;
	private String type;
	private String defaultValue;

	@JsonIgnore
	private String lastPaletteName;

	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.tags = new FieldTags();
		this.technicalOnly = false;
		this.style = ElementStyle.forField();
		this.type = null;
		this.defaultValue = null;
	}

	@Deprecated
	@Override
	public Color getBorderColor() {
		return StyleOwner.super.getBorderColor();
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

	@JsonProperty("unique")
	@Deprecated
	public void setUniqueLegacy(final boolean unique) {
		this.setUnique(unique);
	}

}
