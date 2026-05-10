package lu.kbra.modelizer_next.common;

import lu.kbra.pclib.PCUtils;

/**
 * Loads and prints bundled license text.
 */
public final class LicensePrinter {

	/**
	 * Creates a license printer instance.
	 */
	private LicensePrinter() {
	}

	/**
	 * Prints bundled license text to the supplied output.
	 */
	public static final void print() {
		System.out.println(PCUtils.readPackagedStringFile(LicensePrinter.class.getClass(), "/LICENSE_PART"));
		System.out.println();
	}

}
