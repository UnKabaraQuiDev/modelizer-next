package lu.kbra.modelizer_next;

import java.lang.ref.WeakReference;

public class UMLLogicalLink extends UMLLink {

	public UMLLogicalLink() {
	}

	public UMLLogicalLink(String internalId, WeakReference<UMLClass> fromClass, WeakReference<UMLClass> toClass) {
		super(internalId, fromClass, toClass);
	}

	@Override
	public String toString() {
		return "UMLLogicalLink@" + System.identityHashCode(this) + " [internalId=" + internalId + ", fromClass="
				+ fromClass + ", toClass=" + toClass + "]";
	}

}
