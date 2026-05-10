package lu.kbra.modelizer_next.domain.impl;

import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Contract for model elements that expose an ElementNames object.
 */
public interface NamesOwner {

	/**
	 * Returns the conceptual name.
	 * @return the conceptual name
	 */
	default String getConceptualName() {
		return this.getNames().getConceptualName();
	}

	/**
	 * Returns the name.
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the name
	 */
	default String getName(final PanelType panelType) {
		return this.getNames().get(panelType);
	}

	/**
	 * Returns the names.
	 * @return the names
	 */
	ElementNames getNames();

	/**
	 * Returns the technical name.
	 * @return the technical name
	 */
	default String getTechnicalName() {
		return this.getNames().getTechnicalName();
	}

	/**
	 * Checks whether this object has a technical name.
	 * @return {@code true} if technical name exists; otherwise {@code false}
	 */
	default boolean hasTechnicalName() {
		return this.getNames().hasTechnicalName();
	}

	/**
	 * Sets the conceptual name.
	 * @param name name value to read, write, or display
	 */
	default void setConceptualName(final String name) {
		this.getNames().setConceptualName(name);
	}

	/**
	 * Sets the name.
	 * @param forceTechnical whether force technical is enabled
	 * @param name name value to read, write, or display
	 */
	default void setName(final boolean forceTechnical, final String name) {
		this.getNames().set(forceTechnical, name);
	}

	/**
	 * Sets the name.
	 * @param panelType diagram panel type whose model or layout should be used
	 * @param name name value to read, write, or display
	 */
	default void setName(final PanelType panelType, final String name) {
		this.getNames().set(panelType, name);
	}

	/**
	 * Sets the name.
	 * @param panelType diagram panel type whose model or layout should be used
	 * @param maybeTechnical whether maybe technical is enabled
	 * @param name name value to read, write, or display
	 */
	default void setName(final PanelType panelType, final boolean maybeTechnical, final String name) {
		this.getNames().set(panelType, maybeTechnical, name);
	}

	/**
	 * Sets the names.
	 * @param e event object supplied by Swing
	 */
	void setNames(ElementNames e);

	/**
	 * Sets the technical name.
	 * @param name name value to read, write, or display
	 */
	default void setTechnicalName(final String name) {
		this.getNames().setTechnicalName(name);
	}

}
