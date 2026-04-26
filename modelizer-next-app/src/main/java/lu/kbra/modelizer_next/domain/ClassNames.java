package lu.kbra.modelizer_next.domain;

public class ClassNames {

	private String conceptualName;
	private String technicalName;

	public ClassNames() {
		this.conceptualName = "";
		this.technicalName = null;
	}

	public ClassNames(final String conceptualName, final String technicalName) {
		this.conceptualName = conceptualName;
		this.technicalName = technicalName;
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

	public void setConceptualName(final String conceptualName) {
		this.conceptualName = conceptualName;
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
		return "ClassNames@" + System.identityHashCode(this) + " [conceptualName=" + this.conceptualName + ", technicalName="
				+ this.technicalName + "]";
	}

}
