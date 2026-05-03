package lu.kbra.modelizer_next.domain;

import lu.kbra.modelizer_next.layout.PanelType;

public class LayerVisibility {

	private boolean conceptual;
	private boolean logical;
	private boolean physical;

	public LayerVisibility() {
		this.conceptual = true;
		this.logical = true;
		this.physical = true;
	}

	public LayerVisibility(final PanelType... pts) {
		this.set(pts);
	}

	public void clear() {
		this.conceptual = false;
		this.logical = false;
		this.physical = false;
	}

	public boolean isConceptual() {
		return this.conceptual;
	}

	public boolean isLogical() {
		return this.logical;
	}

	public boolean isPhysical() {
		return this.physical;
	}

	public boolean isVisible(final PanelType pt) {
		return switch (pt) {
		case CONCEPTUAL -> this.conceptual;
		case LOGICAL -> this.logical;
		case PHYSICAL -> this.physical;
		};
	}

	public void set(final boolean c, final boolean l, final boolean p) {
		this.conceptual = c;
		this.logical = l;
		this.physical = p;
	}

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

	public void setConceptual(final boolean conceptual) {
		this.conceptual = conceptual;
	}

	public void setLogical(final boolean logical) {
		this.logical = logical;
	}

	public void setPhysical(final boolean physical) {
		this.physical = physical;
	}

	@Override
	public String toString() {
		return "LayerVisibility@" + System.identityHashCode(this) + " [conceptual=" + this.conceptual + ", logical=" + this.logical
				+ ", physical=" + this.physical + "]";
	}

}
