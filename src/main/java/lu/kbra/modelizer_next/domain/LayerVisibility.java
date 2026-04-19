package lu.kbra.modelizer_next.domain;

public class LayerVisibility {

	private boolean conceptual;
	private boolean logical;
	private boolean physical;

	public LayerVisibility() {
		this.conceptual = true;
		this.logical = true;
		this.physical = true;
	}

	public boolean isConceptual() {
		return this.conceptual;
	}

	public void setConceptual(final boolean conceptual) {
		this.conceptual = conceptual;
	}

	public boolean isLogical() {
		return this.logical;
	}

	public void setLogical(final boolean logical) {
		this.logical = logical;
	}

	public boolean isPhysical() {
		return this.physical;
	}

	public void setPhysical(final boolean physical) {
		this.physical = physical;
	}

	@Override
	public String toString() {
		return "LayerVisibility@" + System.identityHashCode(this) + " [conceptual=" + conceptual + ", logical="
				+ logical + ", physical=" + physical + "]";
	}

}
