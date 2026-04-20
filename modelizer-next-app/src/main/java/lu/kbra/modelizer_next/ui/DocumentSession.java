package lu.kbra.modelizer_next.ui;

import java.io.File;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.history.DocumentSnapshot;
import lu.kbra.modelizer_next.history.UndoRedoManager;

final class DocumentSession {

	private final ModelDocument document;
	private final UndoRedoManager undoRedoManager;
	private File currentFile;
	private DocumentSnapshot savedSnapshot;

	DocumentSession(final ModelDocument document) {
		this(document, null);
	}

	DocumentSession(final ModelDocument document, final File currentFile) {
		this.document = document;
		this.currentFile = currentFile;
		this.undoRedoManager = new UndoRedoManager();
		this.undoRedoManager.reset(document);
		this.savedSnapshot = DocumentSnapshot.from(document);
	}

	boolean canRedo() {
		return this.undoRedoManager.canRedo();
	}

	boolean canUndo() {
		return this.undoRedoManager.canUndo();
	}

	File getCurrentFile() {
		return this.currentFile;
	}

	ModelDocument getDocument() {
		return this.document;
	}

	boolean isDirty() {
		return this.savedSnapshot == null || !this.savedSnapshot.sameDocumentState(this.document);
	}

	void markChanged() {
		this.undoRedoManager.recordState(this.document);
	}

	void markSaved(final File file) {
		this.currentFile = file;
		this.savedSnapshot = DocumentSnapshot.from(this.document);
	}

	boolean redo() {
		return this.undoRedoManager.redo(this.document);
	}

	boolean undo() {
		return this.undoRedoManager.undo(this.document);
	}
}
