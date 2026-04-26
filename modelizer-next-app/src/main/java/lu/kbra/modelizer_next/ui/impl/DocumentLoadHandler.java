package lu.kbra.modelizer_next.ui.impl;

import java.io.File;

public interface DocumentLoadHandler {

	boolean confirmLegacyImport(File file);

	boolean confirmNewerVersion(String fileVersion, String appVersion);

	void error(String message, Exception ex);

}