package lu.kbra.modelizer_next.ui.frame;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.swing.JOptionPane;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.json.LegacyModelizerImporter;
import lu.kbra.modelizer_next.json.ModernModelizerImporter;
import lu.kbra.modelizer_next.json.OnlineModelizerImporter;
import lu.kbra.modelizer_next.ui.SwingDocumentLoadHandler;
import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;
import lu.kbra.pclib.PCUtils;

public final class DocumentSessionLoader {

	public static boolean confirmModernDocumentVersion(final Component parent, final ModelDocument loadedDocument) {
		final String fileVersion = loadedDocument.getMeta() == null ? null : loadedDocument.getMeta().getApplicationVersion();

		if (fileVersion != null && !fileVersion.isBlank() && VersionComparator.COMPARATOR.compare(fileVersion, App.VERSION) > 0) {
			final int choice = JOptionPane.showConfirmDialog(parent,
					"This file was created with a newer version of the application (" + fileVersion
							+ ").\nDo you want to try to load the file anyways ?",
					"Newer file version",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			return choice == JOptionPane.YES_OPTION;
		}

		return true;
	}

	public static boolean confirmModernDocumentVersion(final ModelDocument loadedDocument, final DocumentLoadHandler handler) {
		final String fileVersion = loadedDocument.getMeta() == null ? null : loadedDocument.getMeta().getApplicationVersion();

		if (fileVersion != null && !fileVersion.isBlank() && VersionComparator.COMPARATOR.compare(fileVersion, App.VERSION) > 0) {
			return handler.confirmNewerVersion(fileVersion, App.VERSION);
		}

		return true;
	}

	public static Optional<DocumentSession> createDocument(final Component parent, final File selectedFile) {
		return DocumentSessionLoader.createDocument(selectedFile, new SwingDocumentLoadHandler(parent));
	}

	public static Optional<DocumentSession> createDocument(final File selectedFile, final DocumentLoadHandler handler) {
		final String extension = PCUtils.getFileExtension(selectedFile.getName());

		try {
			final ModelDocument loadedDocument;
			final File openedFile;

			switch (extension) {
			case "mod" -> {
				if (!handler.confirmLegacyImport(selectedFile)) {
					return Optional.empty();
				}

				loadedDocument = LegacyModelizerImporter.importFile(selectedFile);
				openedFile = null;
			}
			case "mdlz" -> {
				loadedDocument = OnlineModelizerImporter.importFile(selectedFile);
				openedFile = null;
			}
			case "mn" -> {
				loadedDocument = ModernModelizerImporter.importFile(selectedFile);

				if (!DocumentSessionLoader.confirmModernDocumentVersion(loadedDocument, handler)) {
					return Optional.empty();
				}

				openedFile = selectedFile;
			}
			default -> throw new IOException("Unsupported file extension: ." + extension);
			}

			if (loadedDocument == null) {
				return Optional.empty();
			}

			loadedDocument.setSource(selectedFile.getPath());

			return Optional.of(new DocumentSession(loadedDocument, openedFile));
		} catch (final IOException ex) {
			handler.error("Failed to load file", ex);
			return Optional.empty();
		}
	}

	private DocumentSessionLoader() {
	}

}
