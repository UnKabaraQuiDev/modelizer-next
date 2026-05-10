package lu.kbra.modelizer_next.domain.impl;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.shared.ElementStyle;

/**
 * Contract for model elements that expose an ElementStyle object.
 */
public interface StyleOwner {

	/**
	 * Returns the background color.
	 *
	 * @return the background color
	 */
	default Color getBackgroundColor() {
		return this.getStyle().getBackgroundColor();
	}

	/**
	 * Returns the border color.
	 *
	 * @return the border color
	 */
	default Color getBorderColor() {
		return this.getStyle().getBorderColor();
	}

	/**
	 * Returns the style.
	 *
	 * @return the style
	 */
	ElementStyle getStyle();

	/**
	 * Returns the text color.
	 *
	 * @return the text color
	 */
	default Color getTextColor() {
		return this.getStyle().getTextColor();
	}

	/**
	 * Sets the background color.
	 *
	 * @param c c value used by the operation
	 */
	default void setBackgroundColor(final Color c) {
		this.getStyle().setBackgroundColor(c);
	}

	/**
	 * Sets the border color.
	 *
	 * @param c c value used by the operation
	 */
	default void setBorderColor(final Color c) {
		this.getStyle().setBorderColor(c);
	}

	/**
	 * Sets the style.
	 *
	 * @param style style value used by the operation
	 */
	void setStyle(ElementStyle style);

	/**
	 * Sets the text color.
	 *
	 * @param c c value used by the operation
	 */
	default void setTextColor(final Color c) {
		this.getStyle().setTextColor(c);
	}

}
