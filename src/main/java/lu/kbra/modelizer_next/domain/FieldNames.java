package lu.kbra.modelizer_next.domain;

public class FieldNames {

	private String name;
	private String technicalName;

	public FieldNames() {
		this.name = "";
		this.technicalName = "";
	}

	public FieldNames(final String name) {
		this.name = name;
	}

	public FieldNames(final String name, final String technicalName) {
		this.name = name;
		this.technicalName = technicalName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean hasTechnicalName() {
		return technicalName != null;
	}

	public String getTechnicalName() {
		return technicalName == null ? name : this.technicalName;
	}

	public void setTechnicalName(final String technicalName) {
		this.technicalName = technicalName;
	}

	@Override
	public String toString() {
		return "FieldNames@" + System.identityHashCode(this) + " [name=" + name + ", technicalName=" + technicalName
				+ "]";
	}

}
