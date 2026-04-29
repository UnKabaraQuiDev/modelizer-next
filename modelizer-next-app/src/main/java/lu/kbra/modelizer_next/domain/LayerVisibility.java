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

	public LayerVisibility(PanelType... pts) {
		set(pts);
	}

	public boolean is(PanelType pt) {
		return switch (pt) {
		case CONCEPTUAL -> conceptual;
		case LOGICAL -> logical;
		case PHYSICAL -> physical;
		};
	}

	public void set(PanelType[] pts) {
		clear();
		for (PanelType pt : pts) {
			switch (pt) {
			case CONCEPTUAL -> conceptual = true;
			case LOGICAL -> logical = true;
			case PHYSICAL -> physical = true;
			}
		}
	}

	public void clear() {
		this.conceptual = false;
		this.logical = false;
		this.physical = false;
	}

	public void set(boolean c, boolean l, boolean p) {
		this.conceptual = c;
		this.logical = l;
		this.physical = p;
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
