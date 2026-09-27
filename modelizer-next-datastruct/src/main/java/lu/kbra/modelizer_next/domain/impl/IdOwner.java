package lu.kbra.modelizer_next.domain.impl;

/**
 * Contract for model elements that carry a stable string id.
 */
public interface IdOwner {

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	String getId();

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	void setId(String id);

}
