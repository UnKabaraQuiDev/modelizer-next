package lu.kbra.modelizer_next.domain.impl;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.shared.ElementStyle;

public interface StyleOwner {

	default Color getBackgroundColor() {
		return this.getStyle().getBackgroundColor();
	}

	default Color getBorderColor() {
		return this.getStyle().getBorderColor();
	}

	ElementStyle getStyle();

	default Color getTextColor() {
		return this.getStyle().getTextColor();
	}

	default void setBackgroundColor(final Color c) {
		this.getStyle().setBackgroundColor(c);
	}

	default void setBorderColor(final Color c) {
		this.getStyle().setBorderColor(c);
	}

	void setStyle(ElementStyle style);

	default void setTextColor(final Color c) {
		this.getStyle().setTextColor(c);
	}

}
