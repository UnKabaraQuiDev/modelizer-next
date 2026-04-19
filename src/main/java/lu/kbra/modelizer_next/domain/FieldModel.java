package lu.kbra.modelizer_next.domain;

import java.util.UUID;

public class FieldModel {

	private String id;
	private FieldNames names;
	private boolean notConceptual;
	private String comment;
	private FieldStyle style;

	public FieldModel() {
		this.id = UUID.randomUUID().toString();
		this.names = new FieldNames();
		this.notConceptual = false;
		this.comment = "";
		this.style = new FieldStyle();
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public FieldNames getNames() {
		return this.names;
	}

	public void setNames(final FieldNames names) {
		this.names = names;
	}

	public boolean isNotConceptual() {
		return this.notConceptual;
	}

	public void setNotConceptual(final boolean notConceptual) {
		this.notConceptual = notConceptual;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public FieldStyle getStyle() {
		return this.style;
	}

	public void setStyle(final FieldStyle style) {
		this.style = style;
	}

	@Override
	public String toString() {
		return "FieldModel@" + System.identityHashCode(this) + " [id=" + id + ", names=" + names + ", notConceptual="
				+ notConceptual + ", comment=" + comment + ", style=" + style + "]";
	}

}
