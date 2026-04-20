package lu.kbra.modelizer_next.domain;

import java.util.UUID;

public class FieldModel {

	private String id;
	private FieldNames names;
	private boolean notConceptual;
	private String comment;
	private FieldStyle style;
	private boolean primaryKey;
	private boolean unique;
	private boolean notNull;

	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new FieldNames();
		this.notConceptual = false;
		this.comment = "";
		this.style = new FieldStyle();
		this.primaryKey = false;
		this.unique = false;
		this.notNull = false;
	}

	public String getComment() {
		return this.comment;
	}

	public String getId() {
		return this.id;
	}

	public FieldNames getNames() {
		return this.names;
	}

	public FieldStyle getStyle() {
		return this.style;
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

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setNames(final FieldNames names) {
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

	public void setStyle(final FieldStyle style) {
		this.style = style;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@Override
	public String toString() {
		return "FieldModel@" + System.identityHashCode(this) + " [id=" + this.id + ", names=" + this.names + ", notConceptual="
				+ this.notConceptual + ", comment=" + this.comment + ", style=" + this.style + "]";
	}

}
