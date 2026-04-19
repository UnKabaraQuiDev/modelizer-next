package lu.kbra.modelizer_next.domain;

import java.awt.Color;
import java.util.UUID;

import lu.kbra.modelizer_next.layout.PanelType;

public class CommentModel {

	private Color textColor;
	private Color backgroundColor;
	private Color borderColor;

	private String id;
	private CommentKind kind;
	private String text;
	private CommentBinding binding;
	private boolean visibleInConceptual = true;
	private boolean visibleInLogical = true;
	private boolean visibleInPhysical = true;

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

	public boolean isVisibleInConceptual() {
		return this.visibleInConceptual;
	}

	public void setVisibleInConceptual(final boolean visibleInConceptual) {
		this.visibleInConceptual = visibleInConceptual;
	}

	public boolean isVisibleInLogical() {
		return this.visibleInLogical;
	}

	public void setVisibleInLogical(final boolean visibleInLogical) {
		this.visibleInLogical = visibleInLogical;
	}

	public boolean isVisibleInPhysical() {
		return this.visibleInPhysical;
	}

	public void setVisibleInPhysical(final boolean visibleInPhysical) {
		this.visibleInPhysical = visibleInPhysical;
	}

	public void setVisibility(final PanelType... pts) {
		this.visibleInConceptual = false;
		this.visibleInLogical = false;
		this.visibleInPhysical = false;
		for (final PanelType pt : pts) {
			switch (pt) {
			case CONCEPTUAL -> this.visibleInConceptual = true;
			case LOGICAL -> this.visibleInLogical = true;
			case PHYSICAL -> this.visibleInPhysical = true;
			}
		}
	}

	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [textColor=" + this.textColor + ", backgroundColor="
				+ this.backgroundColor + ", borderColor=" + this.borderColor + ", id=" + this.id + ", kind=" + this.kind + ", text="
				+ this.text + ", binding=" + this.binding + ", visibleInConceptual=" + this.visibleInConceptual + ", visibleInLogical="
				+ this.visibleInLogical + ", visibleInPhysical=" + this.visibleInPhysical + "]";
	}

}
