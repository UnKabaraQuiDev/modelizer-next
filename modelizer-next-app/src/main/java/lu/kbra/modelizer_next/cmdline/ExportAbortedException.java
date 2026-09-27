package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Exception raised when export aborted fails.
 */
final class ExportAbortedException extends IOException {

	private static final long serialVersionUID = 513442251233551029L;

	/**
	 * Creates an export aborted exception instance.
	 *
	 * @param inputFile file to read or write
	 */
	ExportAbortedException(final File inputFile) {
		super("Export aborted for input file: " + inputFile);
	}

	ExportAbortedException(final URI inputFile) {
		super("Export aborted for input file: " + inputFile);
	}

}