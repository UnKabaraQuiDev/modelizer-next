package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;

/**
 * Persistent model of a relationship between two link ends, optionally connected to an association
 * class.
 */
public class LinkModel implements StyleOwner {

	private String id;
	@JsonAlias("name")
	private String label;
	private LinkEnd from;
	private LinkEnd to;
	private Cardinality cardinalityFrom;
	private Cardinality cardinalityTo;
	private String associationClassId;
	private String labelFrom;
	private String labelTo;
	private ElementStyle style;

	@JsonIgnore
	private String lastPaletteName;

	/**
	 * Creates a link model instance.
	 */
	public LinkModel() {
		this.id = UUID.randomUUID().toString();
		this.label = null;
		this.from = new LinkEnd();
		this.to = new LinkEnd();
		this.cardinalityFrom = Cardinality.ONE;
		this.cardinalityTo = Cardinality.ZERO_OR_MANY;
		this.associationClassId = null;
		this.labelFrom = null;
		this.labelTo = null;
		this.style = ElementStyle.forLink();
	}

	/**
	 * Returns the association class ID.
	 *
	 * @return the association class ID
	 */
	public String getAssociationClassId() {
		return this.associationClassId;
	}

	/**
	 * Returns the cardinality from.
	 *
	 * @return the cardinality from
	 */
	public Cardinality getCardinalityFrom() {
		return this.cardinalityFrom;
	}

	/**
	 * Returns the cardinality to.
	 *
	 * @return the cardinality to
	 */
	public Cardinality getCardinalityTo() {
		return this.cardinalityTo;
	}

	/**
	 * Returns the from.
	 *
	 * @return the from
	 */
	public LinkEnd getFrom() {
		return this.from;
	}

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Returns the label from.
	 *
	 * @return the label from
	 */
	public String getLabelFrom() {
		return this.labelFrom;
	}

	/**
	 * Returns the label to.
	 *
	 * @return the label to
	 */
	public String getLabelTo() {
		return this.labelTo;
	}

	@Override
	public String getLastPaletteName() {
		return this.lastPaletteName;
	}

	/**
	 * Returns the line color.
	 *
	 * @return the line color
	 */
	public Color getLineColor() {
		return this.getBorderColor();
	}

	@Override
	public ElementStyle getStyle() {
		return this.style;
	}

	/**
	 * Returns the to.
	 *
	 * @return the to
	 */
	public LinkEnd getTo() {
		return this.to;
	}

	public boolean hasAssociationClass() {
		return this.associationClassId == null || this.associationClassId.isBlank();
	}

	/**
	 * Checks whether this object has a label.
	 *
	 * @return {@code true} if label exists; otherwise {@code false}
	 */
	public boolean hasLabel() {
		return this.label != null && !this.label.isBlank();
	}

	/**
	 * Checks whether this object has a label from.
	 *
	 * @return {@code true} if label from exists; otherwise {@code false}
	 */
	public boolean hasLabelFrom() {
		return this.labelFrom != null && !this.labelFrom.isBlank();
	}

	/**
	 * Checks whether this object has a label to.
	 *
	 * @return {@code true} if label to exists; otherwise {@code false}
	 */
	public boolean hasLabelTo() {
		return this.labelTo != null && !this.labelTo.isBlank();
	}

	/**
	 * Checks whether this object has a target label.
	 *
	 * @return {@code true} if target label exists; otherwise {@code false}
	 */
	public boolean hasTargetLabel() {
		return this.labelTo != null && !this.labelTo.isBlank() || this.labelFrom != null && !this.labelFrom.isBlank();
	}

	/**
	 * Checks whether self linking is enabled or applies.
	 *
	 * @return {@code true} if self linking is enabled or applies; otherwise {@code false}
	 */
	public boolean isSelfLinking() {
		return this.to != null && this.from != null && Objects.equals(this.to.getClassId(), this.from.getClassId());
	}

	/**
	 * Sets the association class ID.
	 *
	 * @param associationClassId id of the element to read or modify
	 */
	public void setAssociationClassId(final String associationClassId) {
		this.associationClassId = associationClassId;
	}

	/**
	 * Sets the cardinality from.
	 *
	 * @param cardinalityFrom cardinality from value used by the operation
	 */
	public void setCardinalityFrom(final Cardinality cardinalityFrom) {
		this.cardinalityFrom = cardinalityFrom;
	}

	/**
	 * Sets the cardinality to.
	 *
	 * @param cardinalityTo cardinality to value used by the operation
	 */
	public void setCardinalityTo(final Cardinality cardinalityTo) {
		this.cardinalityTo = cardinalityTo;
	}

	/**
	 * Sets the from.
	 *
	 * @param from start point or source value
	 */
	public void setFrom(final LinkEnd from) {
		this.from = from;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the label.
	 *
	 * @param name name value to read, write, or display
	 */
	public void setLabel(final String name) {
		if (name == null || name.isBlank()) {
			this.label = null;
			return;
		}
		this.label = name;
	}

	/**
	 * Sets the label from.
	 *
	 * @param labelFrom text value for label from
	 */
	public void setLabelFrom(final String labelFrom) {
		if (labelFrom == null || labelFrom.isBlank()) {
			this.labelFrom = null;
		}
		this.labelFrom = labelFrom;
	}

	/**
	 * Sets the label to.
	 *
	 * @param labelTo text value for label to
	 */
	public void setLabelTo(final String labelTo) {
		if (labelTo == null || labelTo.isBlank()) {
			this.labelTo = null;
		}
		this.labelTo = labelTo;
	}

	@Override
	public void setLastPaletteName(final String lastPaletteName) {
		this.lastPaletteName = lastPaletteName;
	}

	/**
	 * Sets the line color.
	 *
	 * @param lineColor color value to use
	 */
	@JsonProperty("lineColor")
	public void setLineColor(final Color lineColor) {
		this.setBorderColor(lineColor);
	}

	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	/**
	 * Sets the to.
	 *
	 * @param to target point or destination value
	 */
	public void setTo(final LinkEnd to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "LinkModel@" + System.identityHashCode(this) + " [id=" + this.id + ", label=" + this.label + ", from=" + this.from + ", to="
				+ this.to + ", cardinalityFrom=" + this.cardinalityFrom + ", cardinalityTo=" + this.cardinalityTo + ", associationClassId="
				+ this.associationClassId + ", labelFrom=" + this.labelFrom + ", labelTo=" + this.labelTo + ", style=" + this.style
				+ ", lastPaletteName=" + this.lastPaletteName + "]";
	}

}
