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

	public String getTechnicalName() {
		return this.technicalName == null ? this.name : this.technicalName;
	}

	public boolean hasTechnicalName() {
		return this.technicalName != null;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setTechnicalName(final String technicalName) {
		this.technicalName = technicalName;
	}

	@Override
	public String toString() {
		return "FieldNames@" + System.identityHashCode(this) + " [name=" + this.name + ", technicalName=" + this.technicalName + "]";
	}

}
