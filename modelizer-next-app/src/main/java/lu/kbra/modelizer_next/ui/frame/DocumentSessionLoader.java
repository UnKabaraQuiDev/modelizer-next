package lu.kbra.modelizer_next.ui.frame;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.swing.JOptionPane;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.document.SwingDocumentLoadHandler;
import lu.kbra.modelizer_next.json.LegacyModelizerImporter;
import lu.kbra.modelizer_next.json.ModernModelizerImporter;
import lu.kbra.modelizer_next.json.OnlineModelizerImporter;
import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;
import lu.kbra.pclib.PCUtils;

/**
 * Loads, imports, and creates document sessions for the main frame.
 */
public final class DocumentSessionLoader {

	/**
	 * Confirms whether the document should be loaded even if it has been written in a newer version of
	 * the software.
	 * 
	 * @param parent         parent component used for dialog ownership
	 * @param loadedDocument loaded document value used by the operation
	 * @return {@code true} if it is an older version or a newer one and the user acknowledged;
	 *         otherwise {@code false}
	 */
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

	/**
	 * Confirms whether the document should be loaded even if it has been written in a newer version of
	 * the software, using the given {@link DocumentLoadHandler}.
	 * 
	 * @param loadedDocument loaded document value used by the operation
	 * @param handler        handler value used by the operation
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public static boolean confirmModernDocumentVersion(final ModelDocument loadedDocument, final DocumentLoadHandler handler) {
		final String fileVersion = loadedDocument.getMeta() == null ? null : loadedDocument.getMeta().getApplicationVersion();

		if (fileVersion != null && !fileVersion.isBlank() && VersionComparator.COMPARATOR.compare(fileVersion, App.VERSION) > 0) {
			return handler.confirmNewerVersion(fileVersion, App.VERSION);
		}

		return true;
	}

	/**
	 * Loads a document from a files, supports {@code mod}, {@code mdlz}, {@code mn} file extensions.
	 * Uses the {@link SwingDocumentLoadHandler} by default.
	 * 
	 * @param parent       parent component used for dialog ownership
	 * @param selectedFile file to read or write
	 * @return the created document
	 */
	public static Optional<DocumentSession> createDocument(final Component parent, final File selectedFile) {
		return DocumentSessionLoader.createDocument(selectedFile, new SwingDocumentLoadHandler(parent));
	}

	/**
	 * Loads a document from a files, supports {@code mod}, {@code mdlz}, {@code mn} file extensions.
	 * 
	 * @param selectedFile file to read or write
	 * @param handler      handler value used by the operation
	 * @return the created document
	 */
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
