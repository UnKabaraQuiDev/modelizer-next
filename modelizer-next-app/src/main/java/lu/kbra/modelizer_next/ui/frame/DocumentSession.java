package lu.kbra.modelizer_next.ui.frame;

import java.io.File;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.history.DocumentSnapshot;
import lu.kbra.modelizer_next.history.UndoRedoManager;

public final class DocumentSession {

	private final ModelDocument document;
	private final UndoRedoManager undoRedoManager;
	private File currentFile;
	private DocumentSnapshot savedSnapshot;

	public DocumentSession(final ModelDocument document) {
		this(document, null);
	}

	public DocumentSession(final ModelDocument document, final File currentFile) {
		this.document = document;
		this.currentFile = currentFile;
		this.undoRedoManager = new UndoRedoManager();
		this.undoRedoManager.reset(document);
		this.savedSnapshot = DocumentSnapshot.from(document);
	}

	public boolean canRedo() {
		return this.undoRedoManager.canRedo();
	}

	public boolean canUndo() {
		return this.undoRedoManager.canUndo();
	}

	public File getCurrentFile() {
		return this.currentFile;
	}

	public ModelDocument getDocument() {
		return this.document;
	}

	public boolean isDirty() {
		return this.savedSnapshot == null || !this.savedSnapshot.sameDocumentState(this.document);
	}

	public void markChanged() {
		this.undoRedoManager.recordState(this.document);
	}

	public void markSaved(final File file) {
		this.currentFile = file;
		this.savedSnapshot = DocumentSnapshot.from(this.document);
	}

	public boolean redo() {
		return this.undoRedoManager.redo(this.document);
	}

	@Override
	public String toString() {
		return "DocumentSession@" + System.identityHashCode(this) + " [document=" + this.document + ", currentFile=" + this.currentFile
				+ "]";
	}

	public boolean undo() {
		return this.undoRedoManager.undo(this.document);
	}

}
