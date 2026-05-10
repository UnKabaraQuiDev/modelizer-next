package lu.kbra.modelizer_next.layout;

/**
 * Diagram views supported by the application: conceptual, logical, and physical.
 */
public enum PanelType {

	CONCEPTUAL,
	LOGICAL,
	PHYSICAL;

	/**
	 * Checks whether technical is enabled or applies.
	 * @return {@code true} if technical is enabled or applies; otherwise {@code false}
	 */
	public boolean isTechnical() {
		return this != CONCEPTUAL;
	}

}
