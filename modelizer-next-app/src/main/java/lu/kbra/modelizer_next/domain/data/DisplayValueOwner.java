package lu.kbra.modelizer_next.domain.data;

/**
 * Contract for enum-like values that expose a human-readable display value.
 */
public interface DisplayValueOwner {

	/**
	 * Returns the display value.
	 * @return the display value
	 */
	String getDisplayValue();

}
