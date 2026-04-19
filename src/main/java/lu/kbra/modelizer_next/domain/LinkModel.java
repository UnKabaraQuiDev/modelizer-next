package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

public class LinkModel {

	private String id;
	private String name;
	private LinkEnd from;
	private LinkEnd to;
	private Cardinality cardinalityFrom;
	private Cardinality cardinalityTo;
	private String associationClassId;
	private String comment;
	private Color lineColor;

	public LinkModel() {
		this.id = UUID.randomUUID().toString();
		this.name = "";
		this.from = new LinkEnd();
		this.to = new LinkEnd();
		this.cardinalityFrom = Cardinality.ONE;
		this.cardinalityTo = Cardinality.ZERO_OR_MANY;
		this.associationClassId = null;
		this.comment = "";
		this.lineColor = Color.BLACK;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public LinkEnd getFrom() {
		return this.from;
	}

	public void setFrom(final LinkEnd from) {
		this.from = from;
	}

	public LinkEnd getTo() {
		return this.to;
	}

	public void setTo(final LinkEnd to) {
		this.to = to;
	}

	public Cardinality getCardinalityFrom() {
		return this.cardinalityFrom;
	}

	public void setCardinalityFrom(final Cardinality cardinalityFrom) {
		this.cardinalityFrom = cardinalityFrom;
	}

	public Cardinality getCardinalityTo() {
		return this.cardinalityTo;
	}

	public void setCardinalityTo(final Cardinality cardinalityTo) {
		this.cardinalityTo = cardinalityTo;
	}

	public String getAssociationClassId() {
		return this.associationClassId;
	}

	public void setAssociationClassId(final String associationClassId) {
		this.associationClassId = associationClassId;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public Color getLineColor() {
		return this.lineColor;
	}

	public void setLineColor(final Color lineColor) {
		this.lineColor = lineColor;
	}

	@Override
	public String toString() {
		return "LinkModel@" + System.identityHashCode(this) + " [id=" + this.id + ", name=" + this.name + ", from=" + this.from + ", to="
				+ this.to + ", cardinalityFrom=" + this.cardinalityFrom + ", cardinalityTo=" + this.cardinalityTo + ", associationClassId="
				+ this.associationClassId + ", comment=" + this.comment + ", lineColor=" + this.lineColor + "]";
	}

}
