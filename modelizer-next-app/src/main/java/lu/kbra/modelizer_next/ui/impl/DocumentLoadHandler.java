package lu.kbra.modelizer_next.ui.impl;

import java.io.File;

/**
 * Callback interface used by document loaders to report success or failure.
 */
public interface DocumentLoadHandler {

	/**
	 * Confirms whether the legacy import should continue.
	 * @param file file to read or write
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	boolean confirmLegacyImport(File file);

	/**
	 * Confirms whether the newer version should continue.
	 * @param fileVersion text value for file version
	 * @param appVersion text value for application version
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	boolean confirmNewerVersion(String fileVersion, String appVersion);

	/**
	 * Reports the error.
	 * @param message message shown to the caller or user
	 * @param ex exception that caused the failure
	 */
	void error(String message, Exception ex);

}
