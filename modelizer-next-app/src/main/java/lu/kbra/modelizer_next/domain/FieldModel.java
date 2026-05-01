package lu.kbra.modelizer_next.domain;

import java.util.UUID;

public class FieldModel {

	public static final String[] SQL_TYPES = { null, "INT", "BIGINT", "TEXT", "BOOLEAN", "TINYINT", "DATE", "TIMESTAMP" };

	private String id;
	private ElementNames names;
	private boolean notConceptual;
	private ElementStyle style;
	private boolean primaryKey;
	private boolean unique;
	private boolean notNull;
	private String type;

	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new ElementNames();
		this.notConceptual = false;
		this.style = ElementStyle.forField();
		this.primaryKey = false;
		this.unique = false;
		this.notNull = false;
		this.type = null;
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

	public String getType() {
		return this.type;
	}

	public boolean isNotConceptual() {
		return this.notConceptual;
	}

	public boolean isNotNull() {
		return this.notNull;
	}

	public boolean isPrimaryKey() {
		return this.primaryKey;
	}

	public boolean isUnique() {
		return this.unique;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setNames(final ElementNames names) {
		this.names = names;
	}

	public void setNotConceptual(final boolean notConceptual) {
		this.notConceptual = notConceptual;
	}

	public void setNotNull(final boolean notNull) {
		this.notNull = notNull;
	}

	public void setPrimaryKey(final boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@Override
	public String toString() {
		return "FieldModel@" + System.identityHashCode(this) + " [id=" + id + ", names=" + names + ", notConceptual=" + notConceptual
				+ ", style=" + style + ", primaryKey=" + primaryKey + ", unique=" + unique + ", notNull=" + notNull + ", type=" + type
				+ "]";
	}

}
