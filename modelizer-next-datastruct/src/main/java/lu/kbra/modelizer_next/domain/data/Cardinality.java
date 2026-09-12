package lu.kbra.modelizer_next.domain.data;

/**
 * Cardinality values used on association ends.
 */
public enum Cardinality implements DisplayValueOwner {

	ZERO_OR_ONE("0..1"),
	ONE("1..1"),
	ZERO_OR_MANY("0..*"),
	ONE_OR_MANY("1..*");

	private final String displayValue;

	/**
	 * Creates a cardinality instance.
	 *
	 * @param displayValue text value for display value
	 */
	Cardinality(final String displayValue) {
		this.displayValue = displayValue;
	}

	/**
	 * Returns the display value.
	 *
	 * @return the display value
	 */
	@Override
	public String getDisplayValue() {
		return this.displayValue;
	}

}
