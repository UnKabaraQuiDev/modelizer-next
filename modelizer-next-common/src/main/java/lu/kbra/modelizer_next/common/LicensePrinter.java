package lu.kbra.modelizer_next.common;

import lu.kbra.pclib.PCUtils;

public final class LicensePrinter {

	private LicensePrinter() {
	}

	public static final void print() {
		System.out.println(PCUtils.readPackagedStringFile("/LICENSE_PART"));
		System.out.println();
	}

}
