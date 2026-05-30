package lu.kbra.modelizer_next.layout;

/**
 * Diagram views supported by the application: conceptual, logical, and physical.
 */
public enum PanelType {

	CONCEPTUAL,
	LOGICAL,
	PHYSICAL;

	public boolean isTechnical() {
		return this != CONCEPTUAL;
	}

	public PanelType previous() {
		return switch (this) {
		case CONCEPTUAL -> null;
		case LOGICAL -> CONCEPTUAL;
		case PHYSICAL -> LOGICAL;
		};
	}

	public PanelType next() {
		return switch (this) {
		case CONCEPTUAL -> LOGICAL;
		case LOGICAL -> PHYSICAL;
		case PHYSICAL -> null;
		};
	}

}
