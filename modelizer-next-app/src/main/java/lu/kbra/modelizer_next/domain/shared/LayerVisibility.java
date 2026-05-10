package lu.kbra.modelizer_next.domain.shared;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Visibility flags for conceptual, logical, and physical diagram layers.
 */
public class LayerVisibility {

	private boolean conceptual;
	private boolean logical;
	private boolean physical;

	/**
	 * Creates a layer visibility instance.
	 */
	public LayerVisibility() {
		this.conceptual = true;
		this.logical = true;
		this.physical = true;
	}

	/**
	 * Creates a layer visibility instance.
	 *
	 * @param pts values for pts
	 */
	public LayerVisibility(final PanelType... pts) {
		this.set(pts);
	}

	/**
	 * Clears all visibility flags.
	 */
	public void clear() {
		this.conceptual = false;
		this.logical = false;
		this.physical = false;
	}

	/**
	 * Checks whether conceptual is enabled or applies.
	 *
	 * @return {@code true} if conceptual is enabled or applies; otherwise {@code false}
	 */
	public boolean isConceptual() {
		return this.conceptual;
	}

	/**
	 * Checks whether logical is enabled or applies.
	 *
	 * @return {@code true} if logical is enabled or applies; otherwise {@code false}
	 */
	public boolean isLogical() {
		return this.logical;
	}

	/**
	 * Checks whether physical is enabled or applies.
	 *
	 * @return {@code true} if physical is enabled or applies; otherwise {@code false}
	 */
	public boolean isPhysical() {
		return this.physical;
	}

	/**
	 * Checks whether visible is enabled or applies.
	 *
	 * @param pt pt value used by the operation
	 * @return {@code true} if visible is enabled or applies; otherwise {@code false}
	 */
	public boolean isVisible(final PanelType pt) {
		return switch (pt) {
		case CONCEPTUAL -> this.conceptual;
		case LOGICAL -> this.logical;
		case PHYSICAL -> this.physical;
		};
	}

	/**
	 * Sets the value for the requested panel or key.
	 *
	 * @param c whether c is enabled
	 * @param l whether l is enabled
	 * @param p whether p is enabled
	 */
	public void set(final boolean c, final boolean l, final boolean p) {
		this.conceptual = c;
		this.logical = l;
		this.physical = p;
	}

	/**
	 * Sets the value for the requested panel or key.
	 *
	 * @param pts values for pts
	 */
	public void set(final PanelType... pts) {
		this.clear();
		for (final PanelType pt : pts) {
			switch (pt) {
			case CONCEPTUAL -> this.conceptual = true;
			case LOGICAL -> this.logical = true;
			case PHYSICAL -> this.physical = true;
			}
		}
	}

	/**
	 * Sets the conceptual.
	 *
	 * @param conceptual whether conceptual is enabled
	 */
	public void setConceptual(final boolean conceptual) {
		this.conceptual = conceptual;
	}

	/**
	 * Sets the logical.
	 *
	 * @param logical whether logical is enabled
	 */
	public void setLogical(final boolean logical) {
		this.logical = logical;
	}

	/**
	 * Sets the physical.
	 *
	 * @param physical whether physical is enabled
	 */
	public void setPhysical(final boolean physical) {
		this.physical = physical;
	}

	/**
	 * Builds a debug string for this layer visibility.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "LayerVisibility@" + System.identityHashCode(this) + " [conceptual=" + this.conceptual + ", logical=" + this.logical
				+ ", physical=" + this.physical + "]";
	}

}
