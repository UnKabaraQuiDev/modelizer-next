package lu.kbra.modelizer_next.cmdline;

import java.io.File;

import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;

public final class ConsoleDocumentLoadHandler implements DocumentLoadHandler {

	private final boolean force;

	public ConsoleDocumentLoadHandler(final boolean force) {
		this.force = force;
	}

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

	@Override
	public void error(final String message, final Exception ex) {
		System.err.println(message + ": " + ex.getMessage());
	}

}