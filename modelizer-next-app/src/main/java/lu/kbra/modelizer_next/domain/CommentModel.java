package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.domain.impl.IdOwner;
import lu.kbra.modelizer_next.domain.impl.StyleOwner;
import lu.kbra.modelizer_next.domain.impl.VisibilityOwner;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.domain.shared.LayerVisibility;

/**
 * Persistent model of a text comment or annotation placed in a diagram.
 */
public class CommentModel implements StyleOwner, VisibilityOwner, IdOwner {

	private String id;
	private CommentKind kind;
	private String text;
	private CommentBinding binding;
	private LayerVisibility visibility;
	private ElementStyle style;

	/**
	 * Creates a comment model instance.
	 */
	public CommentModel() {
		this.id = UUID.randomUUID().toString();
		this.kind = CommentKind.STANDALONE;
		this.text = null;
		this.binding = null;
		this.visibility = new LayerVisibility();
		this.style = new ElementStyle();
	}

	/**
	 * Creates a comment model instance.
	 *
	 * @param txt text value for txt
	 */
	public CommentModel(final String txt) {
		this();
		this.text = txt;
	}

	/**
	 * Returns the binding.
	 *
	 * @return the binding
	 */
	public CommentBinding getBinding() {
		return this.binding;
	}

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the kind.
	 *
	 * @return the kind
	 */
	public CommentKind getKind() {
		return this.kind;
	}

	/**
	 * Returns the style.
	 *
	 * @return the style
	 */
	@Override
	public ElementStyle getStyle() {
		return this.style;
	}

	/**
	 * Returns the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Returns the visibility.
	 *
	 * @return the visibility
	 */
	@Override
	public LayerVisibility getVisibility() {
		return this.visibility;
	}

	/**
	 * Sets the background color legacy.
	 *
	 * @param color color value to use
	 */
	@JsonProperty("backgroundColor")
	@Deprecated
	public void setBackgroundColorLegacy(final Color color) {
		this.style.setBackgroundColor(color);
	}

	/**
	 * Sets the binding.
	 *
	 * @param binding binding value used by the operation
	 */
	public void setBinding(final CommentBinding binding) {
		this.binding = binding;
	}

	/**
	 * Sets the border color legacy.
	 *
	 * @param color color value to use
	 */
	@JsonProperty("borderColor")
	@Deprecated
	public void setBorderColorLegacy(final Color color) {
		this.style.setBorderColor(color);
	}

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	@Override
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the kind.
	 *
	 * @param kind kind value used by the operation
	 */
	public void setKind(final CommentKind kind) {
		this.kind = kind;
	}

	/**
	 * Sets the style.
	 *
	 * @param style style value used by the operation
	 */
	@Override
	public void setStyle(final ElementStyle style) {
		this.style = style;
	}

	/**
	 * Sets the text.
	 *
	 * @param text text to display or edit
	 */
	public void setText(final String text) {
		this.text = text;
	}

	/**
	 * Sets the text color legacy.
	 *
	 * @param color color value to use
	 */
	@JsonProperty("textColor")
	@Deprecated
	public void setTextColorLegacy(final Color color) {
		this.style.setTextColor(color);
	}

	/**
	 * Sets the visibility.
	 *
	 * @param visibility visibility value used by the operation
	 */
	@Override
	public void setVisibility(final LayerVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * Sets the visible in conceptual legacy.
	 *
	 * @param visibleInConceptual whether visible in conceptual is enabled
	 */
	@JsonProperty("visibleInConceptual")
	@Deprecated
	public void setVisibleInConceptualLegacy(final boolean visibleInConceptual) {
		this.visibility.setConceptual(visibleInConceptual);
	}

	/**
	 * Sets the visible in logical legacy.
	 *
	 * @param visibleInLogical whether visible in logical is enabled
	 */
	@JsonProperty("visibleInLogical")
	@Deprecated
	public void setVisibleInLogicalLegacy(final boolean visibleInLogical) {
		this.visibility.setLogical(visibleInLogical);
	}

	/**
	 * Sets the visible in physical legacy.
	 *
	 * @param visibleInPhysical whether visible in physical is enabled
	 */
	@JsonProperty("visibleInPhysical")
	@Deprecated
	public void setVisibleInPhysicalLegacy(final boolean visibleInPhysical) {
		this.visibility.setPhysical(visibleInPhysical);
	}

	/**
	 * Builds a debug string for this comment model.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [id=" + this.id + ", kind=" + this.kind + ", text=" + this.text
				+ ", binding=" + this.binding + ", visibility=" + this.visibility + ", elementStyle=" + this.style + "]";
	}

}
