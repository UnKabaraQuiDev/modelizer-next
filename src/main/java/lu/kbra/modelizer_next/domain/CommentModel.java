package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import lu.kbra.modelizer_next.common.ColorUtils;

public class CommentModel {

	private Color textColor = ColorUtils.ofHex("#333333");
	private Color backgroundColor = ColorUtils.ofHex("#FFF8CC");
	private Color borderColor = ColorUtils.ofHex("#444444");

	private String id;
	private CommentKind kind;
	private String text;
	private CommentBinding binding;

	public CommentModel() {
		this.id = UUID.randomUUID().toString();
		this.kind = CommentKind.STANDALONE;
		this.text = "";
		this.binding = null;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public CommentKind getKind() {
		return this.kind;
	}

	public void setKind(final CommentKind kind) {
		this.kind = kind;
	}

	public String getText() {
		return this.text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public CommentBinding getBinding() {
		return this.binding;
	}

	public void setBinding(final CommentBinding binding) {
		this.binding = binding;
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

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [id=" + id + ", kind=" + kind + ", text=" + text
				+ ", binding=" + binding + "]";
	}

}
