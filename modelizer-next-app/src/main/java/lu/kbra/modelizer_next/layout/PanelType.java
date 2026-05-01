package lu.kbra.modelizer_next.layout;

public enum PanelType {

	CONCEPTUAL,
	LOGICAL,
	PHYSICAL;

	public boolean isTechnical() {
		return this != CONCEPTUAL;
	}

}
