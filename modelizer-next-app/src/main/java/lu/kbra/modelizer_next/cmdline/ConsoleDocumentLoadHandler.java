package lu.kbra.modelizer_next.cmdline;

import java.io.File;

import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;

/**
 * Document load handler that reports load failures to the console instead of showing Swing dialogs.
 */
public final class ConsoleDocumentLoadHandler implements DocumentLoadHandler {

	private final boolean force;

	/**
	 * Creates a console document load handler instance.
	 * @param force whether force is enabled
	 */
	public ConsoleDocumentLoadHandler(final boolean force) {
		this.force = force;
	}

	/**
	 * Confirms whether the legacy import should continue.
	 * @param file file to read or write
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public boolean confirmLegacyImport(final File file) {
		System.err.println("Warning: " + file.getName() + " is a legacy Modelizer file.");
		System.err.println("There may be errors or unsupported elements during import.");

		if (!this.force) {
			System.err.println("Use --force to continue.");
			return false;
		}

		return true;
	}

	/**
	 * Confirms whether the newer version should continue.
	 * @param fileVersion text value for file version
	 * @param appVersion text value for application version
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public boolean confirmNewerVersion(final String fileVersion, final String appVersion) {
		System.err.println("Warning: this file was created with a newer version of the application.");
		System.err.println("File version: " + fileVersion);
		System.err.println("App version:  " + appVersion);

		if (!this.force) {
			System.err.println("Use --force to continue.");
			return false;
		}

		return true;
	}

	/**
	 * Reports the error.
	 * @param message message shown to the caller or user
	 * @param ex exception that caused the failure
	 */
	@Override
	public void error(final String message, final Exception ex) {
		System.err.println(message + ": " + ex.getMessage());
	}

}
