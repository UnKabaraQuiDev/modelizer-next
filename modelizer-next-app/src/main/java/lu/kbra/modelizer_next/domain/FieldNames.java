package lu.kbra.modelizer_next.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public class FieldNames {

	@JsonAlias("name")
	private String conceptualName;
	private String technicalName;

	public FieldNames() {
		this.conceptualName = "";
		this.technicalName = null;
	}

	public FieldNames(final String conceptualName) {
		this.conceptualName = conceptualName;
	}

	public FieldNames(final String conceptualName, final String technicalName) {
		this.conceptualName = conceptualName;
		this.technicalName = technicalName;
	}

	public String getConceptualName() {
		return this.conceptualName;
	}

	public String getTechnicalName() {
		return this.technicalName == null || technicalName.isBlank() ? this.conceptualName : this.technicalName;
	}

	public boolean hasTechnicalName() {
		return technicalName != null && !technicalName.isBlank();
	}

	public void setConceptualName(final String name) {
		this.conceptualName = name;
	}

	public void setTechnicalName(final String technicalName) {
		this.technicalName = technicalName;
	}

	@Override
	public String toString() {
		return "FieldNames@" + System.identityHashCode(this) + " [name=" + this.conceptualName + ", technicalName=" + this.technicalName
				+ "]";
	}

}
