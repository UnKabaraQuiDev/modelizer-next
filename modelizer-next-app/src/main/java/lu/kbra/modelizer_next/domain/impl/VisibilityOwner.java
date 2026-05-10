package lu.kbra.modelizer_next.domain.impl;

import lu.kbra.modelizer_next.domain.shared.LayerVisibility;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Contract for model elements that can be shown or hidden per diagram panel.
 */
public interface VisibilityOwner {

	/**
	 * Clears the visibility.
	 */
	default void clearVisibility() {
		this.getVisibility().clear();
	}

	/**
	 * Returns the visibility.
	 *
	 * @return the visibility
	 */
	LayerVisibility getVisibility();

	/**
	 * Checks whether visible is enabled or applies.
	 *
	 * @param pt pt value used by the operation
	 * @return {@code true} if visible is enabled or applies; otherwise {@code false}
	 */
	default boolean isVisible(final PanelType pt) {
		return this.getVisibility().isVisible(pt);
	}

	/**
	 * Checks whether visible in conceptual is enabled or applies.
	 *
	 * @return {@code true} if visible in conceptual is enabled or applies; otherwise {@code false}
	 */
	default boolean isVisibleInConceptual() {
		return this.getVisibility().isConceptual();
	}

	/**
	 * Checks whether visible in logical is enabled or applies.
	 *
	 * @return {@code true} if visible in logical is enabled or applies; otherwise {@code false}
	 */
	default boolean isVisibleInLogical() {
		return this.getVisibility().isLogical();
	}

	/**
	 * Checks whether visible in physical is enabled or applies.
	 *
	 * @return {@code true} if visible in physical is enabled or applies; otherwise {@code false}
	 */
	default boolean isVisibleInPhysical() {
		return this.getVisibility().isPhysical();
	}

	/**
	 * Sets the visibility.
	 *
	 * @param c whether c is enabled
	 * @param l whether l is enabled
	 * @param p whether p is enabled
	 */
	default void setVisibility(final boolean c, final boolean l, final boolean p) {
		this.getVisibility().set(c, l, p);
	}

	/**
	 * Sets the visibility.
	 *
	 * @param v v value used by the operation
	 */
	void setVisibility(LayerVisibility v);

	/**
	 * Sets the visibility.
	 *
	 * @param pts values for pts
	 */
	default void setVisibility(final PanelType... pts) {
		this.getVisibility().set(pts);
	}

	/**
	 * Sets the visible in conceptual.
	 *
	 * @param c whether c is enabled
	 */
	default void setVisibleInConceptual(final boolean c) {
		this.getVisibility().setConceptual(c);
	}

	/**
	 * Sets the visible in logical.
	 *
	 * @param c whether c is enabled
	 */
	default void setVisibleInLogical(final boolean c) {
		this.getVisibility().setLogical(c);
	}

	/**
	 * Sets the visible in physical.
	 *
	 * @param c whether c is enabled
	 */
	default void setVisibleInPhysical(final boolean c) {
		this.getVisibility().setPhysical(c);
	}

}
