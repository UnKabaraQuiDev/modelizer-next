package lu.kbra.modelizer_next.domain;

public class LinkEnd {

	private String classId;
	private String fieldId;

	public LinkEnd() {
		this.classId = "";
		this.fieldId = null;
	}

	public LinkEnd(final String classId, final String fieldId) {
		this.classId = classId;
		this.fieldId = fieldId;
	}

	public String getClassId() {
		return this.classId;
	}

	public void setClassId(final String classId) {
		this.classId = classId;
	}

	public String getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(final String fieldId) {
		this.fieldId = fieldId;
	}

	@Override
	public String toString() {
		return "LinkEnd@" + System.identityHashCode(this) + " [classId=" + this.classId + ", fieldId=" + this.fieldId + "]";
	}

}
