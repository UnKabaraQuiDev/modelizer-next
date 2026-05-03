package lu.kbra.modelizer_next.domain.shared;

import com.fasterxml.jackson.annotation.JsonAlias;

import lu.kbra.modelizer_next.layout.PanelType;

public class ElementNames {

	@JsonAlias("name")
	private String conceptualName;
	private String technicalName;

	public ElementNames() {
		this.conceptualName = "";
		this.technicalName = null;
	}

	public ElementNames(final String conceptualName) {
		this.conceptualName = conceptualName;
	}

	public ElementNames(final String conceptualName, final String technicalName) {
		this.conceptualName = conceptualName;
		this.technicalName = technicalName;
	}

	public String get(final PanelType panelType) {
		return panelType.isTechnical() && this.hasTechnicalName() ? this.getTechnicalName() : this.getConceptualName();
	}

	public String getConceptualName() {
		return this.conceptualName;
	}

	public String getTechnicalName() {
		return this.technicalName == null || this.technicalName.isBlank() ? this.conceptualName : this.technicalName;
	}

	public boolean hasTechnicalName() {
		return this.technicalName != null && !this.technicalName.isBlank();
	}

	public void set(final PanelType panelType, final String name) {
		if (panelType.isTechnical() && this.hasTechnicalName()) {
			this.setTechnicalName(name);
		} else {
			this.setConceptualName(name);
		}
	}

	public void setConceptualName(final String name) {
		this.conceptualName = name;
	}

	public void setTechnicalName(final String technicalName) {
		if (technicalName == null || technicalName.isBlank()) {
			this.technicalName = null;
			return;
		}
		this.technicalName = technicalName;
	}

	@Override
	public String toString() {
		return "ElementNames@" + System.identityHashCode(this) + " [conceptualName=" + this.conceptualName + ", technicalName="
				+ this.technicalName + "]";
	}

}
