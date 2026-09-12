package lu.kbra.modelizer_next.domain;

/**
 * One endpoint of a link, including the target class or field and its cardinality information.
 */
public class LinkEnd {

	private String classId;
	private String fieldId;

	/**
	 * Creates a link end instance.
	 */
	public LinkEnd() {
		this.classId = "";
		this.fieldId = null;
	}

	/**
	 * Creates a link end instance.
	 *
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 */
	public LinkEnd(final String classId, final String fieldId) {
		this.classId = classId;
		this.fieldId = fieldId;
	}

	/**
	 * Returns the class ID.
	 *
	 * @return the class ID
	 */
	public String getClassId() {
		return this.classId;
	}

	/**
	 * Returns the field ID.
	 *
	 * @return the field ID
	 */
	public String getFieldId() {
		return this.fieldId;
	}

	public boolean hasField() {
		return this.fieldId == null || this.fieldId.isBlank();
	}

	/**
	 * Sets the class ID.
	 *
	 * @param classId id of the class to look up or modify
	 */
	public void setClassId(final String classId) {
		this.classId = classId;
	}

	/**
	 * Sets the field ID.
	 *
	 * @param fieldId id of the field to look up or modify
	 */
	public void setFieldId(final String fieldId) {
		this.fieldId = fieldId;
	}

	/**
	 * Builds a debug string for this link end.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "LinkEnd@" + System.identityHashCode(this) + " [classId=" + this.classId + ", fieldId=" + this.fieldId + "]";
	}

}
