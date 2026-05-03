package lu.kbra.modelizer_next.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;

public final class SwingDocumentLoadHandler implements DocumentLoadHandler {

	private final Component parent;

	public SwingDocumentLoadHandler(final Component parent) {
		this.parent = parent;
	}

	@Override
	public boolean confirmLegacyImport(final File file) {
		final int choice = JOptionPane.showConfirmDialog(this.parent, """
				This file comes from an older version of Modelizer.
				There may be errors or unsupported elements during import.
				Do you want to continue?""", "Legacy Modelizer import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		return choice == JOptionPane.YES_OPTION;
	}

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

	@Override
	public void error(final String message, final Exception ex) {
		JOptionPane.showMessageDialog(this.parent, message + ":\n" + ex.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
	}

}
