package lu.kbra.modelizer_next.domain;

public class ClassNames {

	private String conceptualName;
	private String technicalName;

	public ClassNames() {
		this.conceptualName = "";
		this.technicalName = "";
	}

	public ClassNames(final String conceptualName, final String technicalName) {
		this.conceptualName = conceptualName;
		this.technicalName = technicalName;
	}

	public String getConceptualName() {
		return this.conceptualName;
	}

	public String getTechnicalName() {
		return this.technicalName;
	}

	public void setConceptualName(final String conceptualName) {
		this.conceptualName = conceptualName;
	}

	public void setTechnicalName(final String technicalName) {
		this.technicalName = technicalName;
	}

	@Override
	public String toString() {
		return "ClassNames@" + System.identityHashCode(this) + " [conceptualName=" + this.conceptualName + ", technicalName="
				+ this.technicalName + "]";
	}

}
