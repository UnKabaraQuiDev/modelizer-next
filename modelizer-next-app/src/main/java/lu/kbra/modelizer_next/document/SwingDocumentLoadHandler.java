package lu.kbra.modelizer_next.document;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;

/**
 * Document load handler that reports failures through Swing UI components.
 */
public final class SwingDocumentLoadHandler implements DocumentLoadHandler {

	private final Component parent;

	/**
	 * Creates a Swing document load handler instance.
	 * @param parent parent component used for dialog ownership
	 */
	public SwingDocumentLoadHandler(final Component parent) {
		this.parent = parent;
	}

	/**
	 * Confirms whether the legacy import should continue.
	 * @param file file to read or write
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public boolean confirmLegacyImport(final File file) {
		final int choice = JOptionPane.showConfirmDialog(this.parent, """
				This file comes from an older version of Modelizer.
				There may be errors or unsupported elements during import.
				Do you want to continue?""", "Legacy Modelizer import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		return choice == JOptionPane.YES_OPTION;
	}

	/**
	 * Confirms whether the newer version should continue.
	 * @param fileVersion text value for file version
	 * @param appVersion text value for application version
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	@Override
	public boolean confirmNewerVersion(final String fileVersion, final String appVersion) {
		final int choice = JOptionPane.showConfirmDialog(this.parent,
				"This file was created with a newer version of the application (" + fileVersion
						+ ").\nDo you want to try to load the file anyways?",
				"Newer file version",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		return choice == JOptionPane.YES_OPTION;
	}

	/**
	 * Reports the error.
	 * @param message message shown to the caller or user
	 * @param ex exception that caused the failure
	 */
	@Override
	public void error(final String message, final Exception ex) {
		JOptionPane.showMessageDialog(this.parent, message + ":\n" + ex.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
	}

}
