package lu.kbra.modelizer_next.domain.shared;

import java.awt.Color;

import lu.kbra.modelizer_next.common.ColorUtils;

/**
 * Reusable style settings for model elements, including colors and optional visual flags.
 */
public class ElementStyle implements ModelElement {

	/**
	 * Creates the default style for class elements.
	 *
	 * @return the for class result
	 */
	public static ElementStyle forClass() {
		final ElementStyle style = new ElementStyle();
		style.textColor = ColorUtils.ofRgb(0x000000);
		style.backgroundColor = ColorUtils.ofRgb(0xFFF59D);
		style.borderColor = ColorUtils.ofRgb(0x333333);
		return style;
	}

	/**
	 * Creates the default style for field elements.
	 *
	 * @return the for field result
	 */
	public static ElementStyle forField() {
		final ElementStyle style = new ElementStyle();
		style.textColor = ColorUtils.ofRgb(0x000000);
		style.backgroundColor = ColorUtils.ofRgb(0xFFFFFF);
		return style;
	}

	private Color textColor;
	private Color backgroundColor;
	private Color borderColor;

	/**
	 * Creates an element style instance.
	 */
	public ElementStyle() {
	}

	/**
	 * Creates an element style instance.
	 *
	 * @param textColor       color value to use
	 * @param backgroundColor color value to use
	 * @param borderColor     color value to use
	 */
	public ElementStyle(final Color textColor, final Color backgroundColor, final Color borderColor) {
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
	}

	/**
	 * Returns the background color.
	 *
	 * @return the background color
	 */
	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

	/**
	 * Returns the border color.
	 *
	 * @return the border color
	 */
	public Color getBorderColor() {
		return this.borderColor;
	}

	/**
	 * Returns the text color.
	 *
	 * @return the text color
	 */
	public Color getTextColor() {
		return this.textColor;
	}

	/**
	 * Sets the background color.
	 *
	 * @param backgroundColor color value to use
	 */
	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * Sets the border color.
	 *
	 * @param borderColor color value to use
	 */
	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * Sets the text color.
	 *
	 * @param textColor color value to use
	 */
	public void setTextColor(final Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * Creates a copy of this object so callers can modify it without changing the original.
	 *
	 * @return the clone result
	 */
	@Override
	public ElementStyle clone() {
		try {
			return (ElementStyle) super.clone();
		} catch (CloneNotSupportedException e) {
			return new ElementStyle(textColor, backgroundColor, borderColor);
		}
	}

	/**
	 * Builds a debug string for this element style.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "ClassStyle@" + System.identityHashCode(this) + " [textColor=" + this.textColor + ", backgroundColor=" + this.backgroundColor
				+ ", borderColor=" + this.borderColor + "]";
	}

}
