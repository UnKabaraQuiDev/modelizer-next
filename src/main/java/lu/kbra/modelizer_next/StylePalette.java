package lu.kbra.modelizer_next;

import java.awt.Color;

public class StylePalette {

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

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Color getClassTextColor() {
		return this.classTextColor;
	}

	public void setClassTextColor(final Color classTextColor) {
		this.classTextColor = classTextColor;
	}

	public Color getClassBackgroundColor() {
		return this.classBackgroundColor;
	}

	public void setClassBackgroundColor(final Color classBackgroundColor) {
		this.classBackgroundColor = classBackgroundColor;
	}

	public Color getClassBorderColor() {
		return this.classBorderColor;
	}

	public void setClassBorderColor(final Color classBorderColor) {
		this.classBorderColor = classBorderColor;
	}

	public Color getFieldTextColor() {
		return this.fieldTextColor;
	}

	public void setFieldTextColor(final Color fieldTextColor) {
		this.fieldTextColor = fieldTextColor;
	}

	public Color getFieldBackgroundColor() {
		return this.fieldBackgroundColor;
	}

	public void setFieldBackgroundColor(final Color fieldBackgroundColor) {
		this.fieldBackgroundColor = fieldBackgroundColor;
	}

	public Color getCommentTextColor() {
		return this.commentTextColor;
	}

	public void setCommentTextColor(final Color commentTextColor) {
		this.commentTextColor = commentTextColor;
	}

	public Color getCommentBackgroundColor() {
		return this.commentBackgroundColor;
	}

	public void setCommentBackgroundColor(final Color commentBackgroundColor) {
		this.commentBackgroundColor = commentBackgroundColor;
	}

	public Color getCommentBorderColor() {
		return this.commentBorderColor;
	}

	public void setCommentBorderColor(final Color commentBorderColor) {
		this.commentBorderColor = commentBorderColor;
	}

	public Color getLinkColor() {
		return this.linkColor;
	}

	public void setLinkColor(final Color linkColor) {
		this.linkColor = linkColor;
	}
}