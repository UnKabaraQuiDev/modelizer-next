package lu.kbra.modelizer_next.domain;

import java.awt.Color;

import lu.kbra.modelizer_next.common.ColorUtils;

public class ClassStyle {

	private Color textColor;
	private Color backgroundColor;
	private Color borderColor;

	public ClassStyle() {
		this.textColor = ColorUtils.ofRgb(0x000000);
		this.backgroundColor = ColorUtils.ofRgb(0xFFF59D);
		this.borderColor = ColorUtils.ofRgb(0x333333);
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

	public Color getBorderColor() {
		return this.borderColor;
	}

	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
	}

	@Override
	public String toString() {
		return "ClassStyle@" + System.identityHashCode(this) + " [textColor=" + this.textColor + ", backgroundColor="
				+ this.backgroundColor + ", borderColor=" + this.borderColor + "]";
	}

}
