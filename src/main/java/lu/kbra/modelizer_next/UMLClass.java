package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class UMLClass {

	protected String internalId;

	protected String conceptualName;
	protected String logicalPhysicalName;
	protected boolean conceptual = true;
	protected boolean logical = true;
	protected boolean physical = true;
	protected Color textColor = Consts.FG_COLOR;
	protected Color backgroundColor = Consts.BG_COLOR;
	protected String comment;
	protected List<UMLField> fields;
	protected Point2D position;

	public UMLClass() {
		fields = new ArrayList<>();
		position = new Point2D.Float();
	}

	public UMLClass(String conceptualName, String logicalPhysicalName, boolean conceptual, boolean logical, boolean physical,
			Color textColor, Color backgroundColor, String comment, Point2D position) {
		this.conceptualName = conceptualName;
		this.logicalPhysicalName = logicalPhysicalName;
		this.conceptual = conceptual;
		this.logical = logical;
		this.physical = physical;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		this.comment = comment;
		this.fields = new ArrayList<>();
		this.position = position;
	}

	public UMLField createField() {
		final UMLField f = new UMLField();
		f.setName("Field_" + fields.size());
		fields.add(f);
		return f;
	}

	public ConceptualClassPanel asConceptualPanel() {
		return new ConceptualClassPanel(this);
	}

	public String getConceptualName() {
		return conceptualName;
	}

	public void setConceptualName(String conceptualName) {
		this.conceptualName = conceptualName;
	}

	public String getLogicalPhysicalName() {
		return logicalPhysicalName;
	}

	public void setLogicalPhysicalName(String logicalPhysicalName) {
		this.logicalPhysicalName = logicalPhysicalName;
	}

	public boolean isConceptual() {
		return conceptual;
	}

	public void setConceptual(boolean conceptual) {
		this.conceptual = conceptual;
	}

	public boolean isLogical() {
		return logical;
	}

	public void setLogical(boolean logical) {
		this.logical = logical;
	}

	public boolean isPhysical() {
		return physical;
	}

	public void setPhysical(boolean physical) {
		this.physical = physical;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setFields(List<UMLField> fields) {
		this.fields = fields;
	}

	public void addField(UMLField field) {
		fields.add(field);
	}

	public List<UMLField> getFields() {
		return fields;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "UMLClass@" + System.identityHashCode(this) + " [internalId=" + internalId + ", conceptualName=" + conceptualName
				+ ", logicalPhysicalName=" + logicalPhysicalName + ", conceptual=" + conceptual + ", logical=" + logical + ", physical="
				+ physical + ", textColor=" + textColor + ", backgroundColor=" + backgroundColor + ", comment=" + comment + ", fields="
				+ fields + ", position=" + position + "]";
	}

}