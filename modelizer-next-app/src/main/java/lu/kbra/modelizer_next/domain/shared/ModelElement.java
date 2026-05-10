package lu.kbra.modelizer_next.domain.shared;

/**
 * Common contract for persistent model elements that combine id, names, style, and visibility.
 */
public interface ModelElement extends Cloneable {

	/**
	 * Creates a copy of this object so callers can modify it without changing the original.
	 * @return the clone result
	 */
	Object clone();

}
