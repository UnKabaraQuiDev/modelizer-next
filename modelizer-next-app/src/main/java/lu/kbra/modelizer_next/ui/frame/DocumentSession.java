package lu.kbra.modelizer_next.ui.frame;

import java.io.File;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.history.DocumentSnapshot;
import lu.kbra.modelizer_next.history.UndoRedoManager;

/**
 * Open document session containing the document, its file path, and dirty state.
 */
public final class DocumentSession {

	private final ModelDocument document;
	private final UndoRedoManager undoRedoManager;
	private File currentFile;
	private DocumentSnapshot savedSnapshot;

	/**
	 * Creates a document session instance.
	 *
	 * @param document document to read or modify
	 */
	public DocumentSession(final ModelDocument document) {
		this(document, null);
	}

	/**
	 * Creates a document session instance.
	 *
	 * @param document    document to read or modify
	 * @param currentFile file to read or write
	 */
	public DocumentSession(final ModelDocument document, final File currentFile) {
		this.document = document;
		this.currentFile = currentFile;
		this.undoRedoManager = new UndoRedoManager();
		this.undoRedoManager.reset(document);
		this.savedSnapshot = DocumentSnapshot.from(document);
	}

	/**
	 * Checks whether this object can redo.
	 *
	 * @return {@code true} if the operation is allowed; otherwise {@code false}
	 */
	public boolean canRedo() {
		return this.undoRedoManager.canRedo();
	}

	/**
	 * Checks whether this object can undo.
	 *
	 * @return {@code true} if the operation is allowed; otherwise {@code false}
	 */
	public boolean canUndo() {
		return this.undoRedoManager.canUndo();
	}

	/**
	 * Returns the current file.
	 *
	 * @return the current file
	 */
	public File getCurrentFile() {
		return this.currentFile;
	}

	/**
	 * Returns the document.
	 *
	 * @return the document
	 */
	public ModelDocument getDocument() {
		return this.document;
	}

	/**
	 * Checks whether dirty is enabled or applies.
	 *
	 * @return {@code true} if dirty is enabled or applies; otherwise {@code false}
	 */
	public boolean isDirty() {
		return this.savedSnapshot == null || !this.savedSnapshot.sameDocumentState(this.document);
	}

	/**
	 * Marks the current document session as changed.
	 */
	public void markChanged() {
		this.undoRedoManager.recordState(this.document);
	}
	
	public void unmarkChanged() {
		undoRedoManager.reset(document);
		this.savedSnapshot = DocumentSnapshot.from(this.document);
	}

	/**
	 * Marks the current document session as saved.
	 *
	 * @param file file to read or write
	 */
	public void markSaved(final File file) {
		this.currentFile = file;
		this.savedSnapshot = DocumentSnapshot.from(this.document);
	}

	/**
	 * Restores the next snapshot in the redo history.
	 *
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public boolean redo() {
		return this.undoRedoManager.redo(this.document);
	}

	/**
	 * Builds a debug string for this document session.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "DocumentSession@" + System.identityHashCode(this) + " [document=" + this.document + ", currentFile=" + this.currentFile
				+ "]";
	}

	/**
	 * Restores the previous snapshot in the undo history.
	 *
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public boolean undo() {
		return this.undoRedoManager.undo(this.document);
	}

}
