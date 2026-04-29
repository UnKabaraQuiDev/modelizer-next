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
	private String labelFrom;
	private String labelTo;

	public LinkModel() {
		this.id = UUID.randomUUID().toString();
		this.name = null;
		this.from = new LinkEnd();
		this.to = new LinkEnd();
		this.cardinalityFrom = Cardinality.ONE;
		this.cardinalityTo = Cardinality.ZERO_OR_MANY;
		this.associationClassId = null;
		this.lineColor = Color.BLACK;
		this.labelFrom = null;
		this.labelTo = null;
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

	public String getLabelFrom() {
		return this.labelFrom;
	}

	public String getLabelTo() {
		return this.labelTo;
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

	public boolean hasLabelFrom() {
		return this.labelFrom != null && !this.labelFrom.isBlank();
	}

	public boolean hasLabelTo() {
		return this.labelTo != null && !this.labelTo.isBlank();
	}

	public boolean hasName() {
		return this.name != null && !this.name.isBlank();
	}

	public boolean isSelfLinking() {
		return this.to != null && this.from != null && Objects.equals(this.to.getClassId(), this.from.getClassId());
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

	public void setLabelFrom(final String labelFrom) {
		if (labelFrom == null || labelFrom.isBlank()) {
			this.labelFrom = null;
		}
		this.labelFrom = labelFrom;
	}

	public void setLabelTo(final String labelTo) {
		if (labelTo == null || labelTo.isBlank()) {
			this.labelTo = null;
		}
		this.labelTo = labelTo;
	}

	public void setLineColor(final Color lineColor) {
		this.lineColor = lineColor;
	}

	public void setName(final String name) {
		if (name == null || name.isBlank()) {
			this.name = null;
			return;
		}
		this.name = name;
	}

	public void setTo(final LinkEnd to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "LinkModel [id=" + this.id + ", name=" + this.name + ", from=" + this.from + ", to=" + this.to + ", cardinalityFrom="
				+ this.cardinalityFrom + ", cardinalityTo=" + this.cardinalityTo + ", associationClassId=" + this.associationClassId
				+ ", lineColor=" + this.lineColor + ", labelFrom=" + this.labelFrom + ", labelTo=" + this.labelTo + "]";
	}

}
