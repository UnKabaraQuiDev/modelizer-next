package lu.kbra.modelizer_next.style;

import java.awt.Color;

import lu.kbra.modelizer_next.domain.data.DisplayValueOwner;

/**
 * Named collection of reusable element styles.
 */
public class StylePalette implements DisplayValueOwner {

	private String name;

	private Color classTextColor = Color.BLACK;
	private Color classBackgroundColor = new Color(0xFFF59D);
	private Color classBorderColor = new Color(0x333333);

	private Color fieldTextColor = Color.BLACK;
	private Color fieldBackgroundColor = Color.WHITE;

	private Color commentTextColor = new Color(0x333333);
	private Color commentBackgroundColor = new Color(0xFFF8CC);
	private Color commentBorderColor = new Color(0x444444);

	private Color linkColor = new Color(0x555555);

	/**
	 * Returns the display value.
	 * @return the display value
	 */
	@Override
	public String getDisplayValue() {
		return name;
	}

	/**
	 * Returns the class background color.
	 * @return the class background color
	 */
	public Color getClassBackgroundColor() {
		return this.classBackgroundColor;
	}

	/**
	 * Returns the class border color.
	 * @return the class border color
	 */
	public Color getClassBorderColor() {
		return this.classBorderColor;
	}

	/**
	 * Returns the class text color.
	 * @return the class text color
	 */
	public Color getClassTextColor() {
		return this.classTextColor;
	}

	/**
	 * Returns the comment background color.
	 * @return the comment background color
	 */
	public Color getCommentBackgroundColor() {
		return this.commentBackgroundColor;
	}

	/**
	 * Returns the comment border color.
	 * @return the comment border color
	 */
	public Color getCommentBorderColor() {
		return this.commentBorderColor;
	}

	/**
	 * Returns the comment text color.
	 * @return the comment text color
	 */
	public Color getCommentTextColor() {
		return this.commentTextColor;
	}

	/**
	 * Returns the field background color.
	 * @return the field background color
	 */
	public Color getFieldBackgroundColor() {
		return this.fieldBackgroundColor;
	}

	/**
	 * Returns the field text color.
	 * @return the field text color
	 */
	public Color getFieldTextColor() {
		return this.fieldTextColor;
	}

	/**
	 * Returns the link color.
	 * @return the link color
	 */
	public Color getLinkColor() {
		return this.linkColor;
	}

	/**
	 * Returns the name.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the class background color.
	 * @param classBackgroundColor color value to use
	 */
	public void setClassBackgroundColor(final Color classBackgroundColor) {
		this.classBackgroundColor = classBackgroundColor;
	}

	/**
	 * Sets the class border color.
	 * @param classBorderColor color value to use
	 */
	public void setClassBorderColor(final Color classBorderColor) {
		this.classBorderColor = classBorderColor;
	}

	/**
	 * Sets the class text color.
	 * @param classTextColor color value to use
	 */
	public void setClassTextColor(final Color classTextColor) {
		this.classTextColor = classTextColor;
	}

	/**
	 * Sets the comment background color.
	 * @param commentBackgroundColor color value to use
	 */
	public void setCommentBackgroundColor(final Color commentBackgroundColor) {
		this.commentBackgroundColor = commentBackgroundColor;
	}

	/**
	 * Sets the comment border color.
	 * @param commentBorderColor color value to use
	 */
	public void setCommentBorderColor(final Color commentBorderColor) {
		this.commentBorderColor = commentBorderColor;
	}

	/**
	 * Sets the comment text color.
	 * @param commentTextColor color value to use
	 */
	public void setCommentTextColor(final Color commentTextColor) {
		this.commentTextColor = commentTextColor;
	}

	/**
	 * Sets the field background color.
	 * @param fieldBackgroundColor color value to use
	 */
	public void setFieldBackgroundColor(final Color fieldBackgroundColor) {
		this.fieldBackgroundColor = fieldBackgroundColor;
	}

	/**
	 * Sets the field text color.
	 * @param fieldTextColor color value to use
	 */
	public void setFieldTextColor(final Color fieldTextColor) {
		this.fieldTextColor = fieldTextColor;
	}

	/**
	 * Sets the link color.
	 * @param linkColor color value to use
	 */
	public void setLinkColor(final Color linkColor) {
		this.linkColor = linkColor;
	}

	/**
	 * Sets the name.
	 * @param name name value to read, write, or display
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Builds a debug string for this style palette.
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "StylePalette [name=" + this.name + "]";
	}

}
