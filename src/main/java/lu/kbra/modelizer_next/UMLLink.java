package lu.kbra.modelizer_next;

import java.lang.ref.WeakReference;

public abstract class UMLLink {

	protected String internalId;

//	protected String fromClassInternalId;
	protected WeakReference<UMLClass> fromClass;
//	protected String toClassInternalId;
	protected WeakReference<UMLClass> toClass;

	public UMLLink() {
	}

	public UMLLink(String internalId, WeakReference<UMLClass> fromClass, WeakReference<UMLClass> toClass) {
		this.internalId = internalId;
		this.fromClass = fromClass;
		this.toClass = toClass;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public WeakReference<UMLClass> getFromClass() {
		return fromClass;
	}

	public void setFromClass(WeakReference<UMLClass> fromClass) {
		this.fromClass = fromClass;
	}

	public WeakReference<UMLClass> getToClass() {
		return toClass;
	}

	public void setToClass(WeakReference<UMLClass> toClass) {
		this.toClass = toClass;
	}

	@Override
	public String toString() {
		return "UMLLink@" + System.identityHashCode(this) + " [internalId=" + internalId + ", fromClass=" + fromClass
				+ ", toClass=" + toClass + "]";
	}

}
