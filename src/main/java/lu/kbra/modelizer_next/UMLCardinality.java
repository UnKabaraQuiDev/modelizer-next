package lu.kbra.modelizer_next;

public enum UMLCardinality {

	ONE_ONE("1..1"), ONE_N("1..*"), N_N("*..*"), ZERO_N("0..*"), ZERO_ONE("0..1");

	protected final String name;

	private UMLCardinality(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
