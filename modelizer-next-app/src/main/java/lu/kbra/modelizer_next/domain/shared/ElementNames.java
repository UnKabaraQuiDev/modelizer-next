package lu.kbra.modelizer_next.domain.shared;

import com.fasterxml.jackson.annotation.JsonAlias;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Display names for the same model element in conceptual, logical, and physical views.
 */
public class ElementNames {

	@JsonAlias("name")
	private String conceptualName;
	private String technicalName;

	/**
	 * Creates an element names instance.
	 */
	public ElementNames() {
		this.conceptualName = "";
		this.technicalName = null;
	}

	/**
	 * Creates an element names instance.
	 *
	 * @param conceptualName name value to use
	 */
	public ElementNames(final String conceptualName) {
		this.conceptualName = conceptualName;
	}

	/**
	 * Creates an element names instance.
	 *
	 * @param conceptualName name value to use
	 * @param technicalName  name value to use
	 */
	public ElementNames(final String conceptualName, final String technicalName) {
		this.conceptualName = conceptualName;
		this.technicalName = technicalName;
	}

	/**
	 * Returns the value for the requested panel or key.
	 *
	 * @param panelType diagram panel type whose model or layout should be used
	 * @return the value
	 */
	public String get(final PanelType panelType) {
		return panelType.isTechnical() && this.hasTechnicalName() ? this.getTechnicalName() : this.getConceptualName();
	}

	/**
	 * Returns the conceptual name.
	 *
	 * @return the conceptual name
	 */
	public String getConceptualName() {
		return this.conceptualName;
	}

	/**
	 * Returns the technical name.
	 *
	 * @return the technical name
	 */
	public String getTechnicalName() {
		return this.technicalName == null || this.technicalName.isBlank() ? this.conceptualName : this.technicalName;
	}

	/**
	 * Checks whether this object has a technical name.
	 *
	 * @return {@code true} if technical name exists; otherwise {@code false}
	 */
	public boolean hasTechnicalName() {
		return this.technicalName != null && !this.technicalName.isBlank();
	}

	/**
	 * Sets the value for the requested panel or key.
	 *
	 * @param forceTechnicalName name value to use
	 * @param name               name value to read, write, or display
	 */
	public void set(final boolean forceTechnicalName, final String name) {
		if (forceTechnicalName) {
			this.setTechnicalName(name);
		} else {
			this.setConceptualName(name);
		}
	}

	/**
	 * Sets the value for the requested panel or key.
	 *
	 * @param panelType      diagram panel type whose model or layout should be used
	 * @param maybeTechnical whether maybe technical is enabled
	 * @param name           name value to read, write, or display
	 */
	public void set(final PanelType panelType, final boolean maybeTechnical, final String name) {
		if (maybeTechnical || panelType.isTechnical() && this.hasTechnicalName()) {
			this.setTechnicalName(name);
		} else {
			this.setConceptualName(name);
		}
	}

	/**
	 * Sets the value for the requested panel or key.
	 *
	 * @param panelType diagram panel type whose model or layout should be used
	 * @param name      name value to read, write, or display
	 */
	public void set(final PanelType panelType, final String name) {
		if (panelType.isTechnical() && this.hasTechnicalName()) {
			this.setTechnicalName(name);
		} else {
			this.setConceptualName(name);
		}
	}

	/**
	 * Sets the conceptual name.
	 *
	 * @param name name value to read, write, or display
	 */
	public void setConceptualName(final String name) {
		this.conceptualName = name;
	}

	/**
	 * Sets the technical name.
	 *
	 * @param technicalName name value to use
	 */
	public void setTechnicalName(final String technicalName) {
		if (technicalName == null || technicalName.isBlank()) {
			this.technicalName = null;
			return;
		}
		this.technicalName = technicalName;
	}

	/**
	 * Builds a debug string for this element names.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "ElementNames@" + System.identityHashCode(this) + " [conceptualName=" + this.conceptualName + ", technicalName="
				+ this.technicalName + "]";
	}

}
