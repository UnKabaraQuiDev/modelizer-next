package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.impl.VisibilityOwner;

public class CommentModel implements StyleOwner, VisibilityOwner, IdOwner {

	private String id;
	private CommentKind kind;
	private String text;
	private CommentBinding binding;
	private LayerVisibility visibility;
	private ElementStyle style;

	public CommentModel() {
		this.id = UUID.randomUUID().toString();
		this.kind = CommentKind.STANDALONE;
		this.text = null;
		this.binding = null;
		this.visibility = new LayerVisibility();
		this.style = new ElementStyle();
	}

	public CommentBinding getBinding() {
		return this.binding;
	}

	@Override
	public String getId() {
		return this.id;
	}

	public CommentKind getKind() {
		return this.kind;
	}

	@Override
	public ElementStyle getStyle() {
		return this.style;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	@JsonProperty("backgroundColor")
	@Deprecated
	public void setBackgroundColorLegacy(final Color color) {
		this.style.setBackgroundColor(color);
	}

	public void setBinding(final CommentBinding binding) {
		this.binding = binding;
	}

	@JsonProperty("borderColor")
	@Deprecated
	public void setBorderColorLegacy(final Color color) {
		this.style.setBorderColor(color);
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	public void setKind(final CommentKind kind) {
		this.kind = kind;
	}

	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	public void setText(final String text) {
		this.text = text;
	}

	@JsonProperty("textColor")
	@Deprecated
	public void setTextColorLegacy(final Color color) {
		this.style.setTextColor(color);
	}

	@Override
	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	@JsonProperty("visibleInConceptual")
	@Deprecated
	public void setVisibleInConceptualLegacy(final boolean visibleInConceptual) {
		this.visibility.setConceptual(visibleInConceptual);
	}

	@JsonProperty("visibleInLogical")
	@Deprecated
	public void setVisibleInLogicalLegacy(final boolean visibleInLogical) {
		this.visibility.setLogical(visibleInLogical);
	}

	@JsonProperty("visibleInPhysical")
	@Deprecated
	public void setVisibleInPhysicalLegacy(final boolean visibleInPhysical) {
		this.visibility.setPhysical(visibleInPhysical);
	}

	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [id=" + this.id + ", kind=" + this.kind + ", text=" + this.text
				+ ", binding=" + this.binding + ", visibility=" + this.visibility + ", elementStyle=" + this.style + "]";
	}

}
