package lu.kbra.modelizer_next.domain.impl;

import lu.kbra.modelizer_next.domain.shared.LayerVisibility;
import lu.kbra.modelizer_next.layout.PanelType;

public interface VisibilityOwner {

	default void clearVisibility() {
		this.getVisibility().clear();
	}

	LayerVisibility getVisibility();

	default boolean isVisible(final PanelType pt) {
		return this.getVisibility().isVisible(pt);
	}

	default boolean isVisibleInConceptual() {
		return this.getVisibility().isConceptual();
	}

	default boolean isVisibleInLogical() {
		return this.getVisibility().isLogical();
	}

	default boolean isVisibleInPhysical() {
		return this.getVisibility().isPhysical();
	}

	default void setVisibility(final boolean c, final boolean l, final boolean p) {
		this.getVisibility().set(c, l, p);
	}

	void setVisibility(LayerVisibility v);

	default void setVisibility(final PanelType... pts) {
		this.getVisibility().set(pts);
	}

	default void setVisibleInConceptual(final boolean c) {
		this.getVisibility().setConceptual(c);
	}

	default void setVisibleInLogical(final boolean c) {
		this.getVisibility().setLogical(c);
	}

	default void setVisibleInPhysical(final boolean c) {
		this.getVisibility().setPhysical(c);
	}

}
