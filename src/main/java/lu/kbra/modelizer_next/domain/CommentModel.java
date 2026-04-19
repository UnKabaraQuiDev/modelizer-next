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

	public void setVisibility(PanelType... pts) {
		visibleInConceptual = false;
		visibleInLogical = false;
		visibleInPhysical = false;
		for (PanelType pt : pts) {
			switch (pt) {
			case CONCEPTUAL -> visibleInConceptual = true;
			case LOGICAL -> visibleInLogical = true;
			case PHYSICAL -> visibleInPhysical = true;
			}
		}
	}

	@Override
	public String toString() {
		return "CommentModel@" + System.identityHashCode(this) + " [textColor=" + textColor + ", backgroundColor="
				+ backgroundColor + ", borderColor=" + borderColor + ", id=" + id + ", kind=" + kind + ", text=" + text
				+ ", binding=" + binding + ", visibleInConceptual=" + visibleInConceptual + ", visibleInLogical="
				+ visibleInLogical + ", visibleInPhysical=" + visibleInPhysical + "]";
	}

}
