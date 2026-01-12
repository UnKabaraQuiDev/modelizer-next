package lu.kbra.modelizer_next;

import java.awt.Color;
import java.util.Optional;

public class UMLField {

	protected String internalId;

	protected String name;
	protected Optional<String> logicalPhysicalName;
	protected boolean synthetic;
	protected String type;
	protected boolean showType;
	protected String comment;
	protected Color textColor;
	protected Color backgroundColor;

	public UMLField() {
	}

	public UMLField(String name, Optional<String> logicalPhysicalName, boolean synthetic, String comment,
			Color textColor, Color backgroundColor) {
		this.name = name;
		this.logicalPhysicalName = logicalPhysicalName;
		this.synthetic = synthetic;
		this.comment = comment;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
	}

	public ConceptualFieldLabel asConceptualLabel() {
		return new ConceptualFieldLabel(this);
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public String getName() {
		return name;
	}

	public boolean isSynthetic() {
		return synthetic;
	}

	public String getComment() {
		return comment;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isShowType() {
		return showType;
	}

	public void setShowType(boolean showType) {
		this.showType = showType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Optional<String> getLogicalPhysicalName() {
		return logicalPhysicalName;
	}

	public void setLogicalPhysicalName(Optional<String> logicalPhysicalName) {
		this.logicalPhysicalName = logicalPhysicalName;
	}

	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public String toString() {
		return "UMLField@" + System.identityHashCode(this) + " [internalId=" + internalId + ", name=" + name
				+ ", logicalPhysicalName=" + logicalPhysicalName + ", synthetic=" + synthetic + ", type=" + type
				+ ", showType=" + showType + ", comment=" + comment + ", textColor=" + textColor + ", backgroundColor="
				+ backgroundColor + "]";
	}

}