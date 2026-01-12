package lu.kbra.modelizer_next;

import java.lang.ref.WeakReference;

public class UMLConceptualLink extends UMLLink {

	protected UMLCardinality fromCardinality;
	protected UMLCardinality toCardinality;
	protected WeakReference<UMLField> fromField;
	protected WeakReference<UMLField> toField;

	public UMLConceptualLink() {
	}

	public UMLConceptualLink(String internalId, WeakReference<UMLClass> fromClass, WeakReference<UMLClass> toClass,
			UMLCardinality fromCardinality, UMLCardinality toCardinality, WeakReference<UMLField> fromField,
			WeakReference<UMLField> toField) {
		super(internalId, fromClass, toClass);
		this.fromCardinality = fromCardinality;
		this.toCardinality = toCardinality;
		this.fromField = fromField;
		this.toField = toField;
	}

	public UMLCardinality getFromCardinality() {
		return fromCardinality;
	}

	public void setFromCardinality(UMLCardinality fromCardinality) {
		this.fromCardinality = fromCardinality;
	}

	public UMLCardinality getToCardinality() {
		return toCardinality;
	}

	public void setToCardinality(UMLCardinality toCardinality) {
		this.toCardinality = toCardinality;
	}

	public WeakReference<UMLField> getFromField() {
		return fromField;
	}

	public void setFromField(WeakReference<UMLField> fromField) {
		this.fromField = fromField;
	}

	public WeakReference<UMLField> getToField() {
		return toField;
	}

	public void setToField(WeakReference<UMLField> toField) {
		this.toField = toField;
	}

	@Override
	public String toString() {
		return "UMLConceptualLink@" + System.identityHashCode(this) + " [fromCardinality=" + fromCardinality
				+ ", toCardinality=" + toCardinality + ", fromField=" + fromField + ", toField=" + toField
				+ ", internalId=" + internalId + ", fromClass=" + fromClass + ", toClass=" + toClass + "]";
	}

}
