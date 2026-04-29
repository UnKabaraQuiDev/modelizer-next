package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentModel {

	private Color textColor;
	private Color backgroundColor;
	private Color borderColor;

	private String id;
	private CommentKind kind;
	private String text;
	private CommentBinding binding;
	private LayerVisibility visibility;

	public CommentModel() {
		this.id = UUID.randomUUID().toString();
		this.kind = CommentKind.STANDALONE;
		this.text = null;
		this.binding = null;
		this.visibility = new LayerVisibility();
	}

	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

	public CommentBinding getBinding() {
		return this.binding;
	}

	public Color getBorderColor() {
		return this.borderColor;
	}

	public String getId() {
		return this.id;
	}

	public CommentKind getKind() {
		return this.kind;
	}

	public String getText() {
		return this.text;
	}

	public Color getTextColor() {
		return this.textColor;
	}

	@Deprecated
	public boolean isVisibleInConceptual() {
		return visibility.isConceptual();
	}

	@Deprecated
	public boolean isVisibleInLogical() {
		return visibility.isLogical();
	}

	@Deprecated
	public boolean isVisibleInPhysical() {
		return visibility.isPhysical();
	}

	public LayerVisibility getVisibility() {
		return visibility;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setBinding(final CommentBinding binding) {
		this.binding = binding;
	}

	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setKind(final CommentKind kind) {
		this.kind = kind;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void setTextColor(final Color textColor) {
		this.textColor = textColor;
	}

	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [textColor=" + textColor + ", backgroundColor=" + backgroundColor
				+ ", borderColor=" + borderColor + ", id=" + id + ", kind=" + kind + ", text=" + text + ", binding=" + binding
				+ ", visibility=" + visibility + "]";
	}

	@JsonProperty("visibleInConceptual")
	public void setVisibleInConceptualLegacy(boolean visibleInConceptual) {
		this.visibility.setConceptual(visibleInConceptual);
	}

	@JsonProperty("visibleInLogical")
	public void setVisibleInLogicalLegacy(boolean visibleInLogical) {
		this.visibility.setLogical(visibleInLogical);
	}

	@JsonProperty("visibleInPhysical")
	public void setVisibleInPhysicalLegacy(boolean visibleInPhysical) {
		this.visibility.setPhysical(visibleInPhysical);
	}

}
