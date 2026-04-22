package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.Objects;
import java.util.UUID;

public class LinkModel {

	private String id;
	private String name;
	private LinkEnd from;
	private LinkEnd to;
	private Cardinality cardinalityFrom;
	private Cardinality cardinalityTo;
	private String associationClassId;
	private Color lineColor;

	public LinkModel() {
		this.id = UUID.randomUUID().toString();
		this.name = "";
		this.from = new LinkEnd();
		this.to = new LinkEnd();
		this.cardinalityFrom = Cardinality.ONE;
		this.cardinalityTo = Cardinality.ZERO_OR_MANY;
		this.associationClassId = null;
		this.lineColor = Color.BLACK;
	}

	public String getAssociationClassId() {
		return this.associationClassId;
	}

	public Cardinality getCardinalityFrom() {
		return this.cardinalityFrom;
	}

	public Cardinality getCardinalityTo() {
		return this.cardinalityTo;
	}

	public LinkEnd getFrom() {
		return this.from;
	}

	public String getId() {
		return this.id;
	}

	public Color getLineColor() {
		return this.lineColor;
	}

	public String getName() {
		return this.name;
	}

	public LinkEnd getTo() {
		return this.to;
	}

	public void setAssociationClassId(final String associationClassId) {
		this.associationClassId = associationClassId;
	}

	public void setCardinalityFrom(final Cardinality cardinalityFrom) {
		this.cardinalityFrom = cardinalityFrom;
	}

	public void setCardinalityTo(final Cardinality cardinalityTo) {
		this.cardinalityTo = cardinalityTo;
	}

	public void setFrom(final LinkEnd from) {
		this.from = from;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setLineColor(final Color lineColor) {
		this.lineColor = lineColor;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setTo(final LinkEnd to) {
		this.to = to;
	}

	public boolean isSelfLinking() {
		return Objects.equals(to.getClassId(), from.getClassId());
	}

	@Override
	public String toString() {
		return "LinkModel [id=" + id + ", name=" + name + ", from=" + from + ", to=" + to + ", cardinalityFrom=" + cardinalityFrom
				+ ", cardinalityTo=" + cardinalityTo + ", associationClassId=" + associationClassId + ", lineColor=" + lineColor + "]";
	}

}
