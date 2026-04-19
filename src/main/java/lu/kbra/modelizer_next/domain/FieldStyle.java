package lu.kbra.modelizer_next.domain;

import java.awt.Color;

import lu.kbra.modelizer_next.common.ColorUtils;

public class FieldStyle {

	private Color textColor;
	private Color backgroundColor;

	public FieldStyle() {
		this.textColor = ColorUtils.ofRgb(0x000000);
		this.backgroundColor = ColorUtils.ofRgb(0xFFFFFF);
	}

	public Color getTextColor() {
		return this.textColor;
	}

	public void setTextColor(final Color textColor) {
		this.textColor = textColor;
	}

	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public String toString() {
		return "FieldStyle@" + System.identityHashCode(this) + " [textColor=" + this.textColor + ", backgroundColor="
				+ this.backgroundColor + "]";
	}

}
