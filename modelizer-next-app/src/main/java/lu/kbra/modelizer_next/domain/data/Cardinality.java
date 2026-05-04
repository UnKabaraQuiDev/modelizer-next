package lu.kbra.modelizer_next.domain.data;

public enum Cardinality implements DisplayValueOwner {

	ZERO_OR_ONE("0..1"),
	ONE("1..1"),
	ZERO_OR_MANY("0..*"),
	ONE_OR_MANY("1..*");

	private final String displayValue;

	Cardinality(final String displayValue) {
		this.displayValue = displayValue;
	}

	@Override
	public String getDisplayValue() {
		return this.displayValue;
	}

}
