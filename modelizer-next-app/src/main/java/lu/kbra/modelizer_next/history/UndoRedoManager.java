package lu.kbra.modelizer_next.history;

import java.util.ArrayDeque;
import java.util.Deque;

import lu.kbra.modelizer_next.document.ModelDocument;

public class UndoRedoManager {

	private static final int MAX_HISTORY = 100;

	private final Deque<DocumentSnapshot> undoStack = new ArrayDeque<>();
	private final Deque<DocumentSnapshot> redoStack = new ArrayDeque<>();

	public boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	public boolean canUndo() {
		return this.undoStack.size() > 1;
	}

	public void recordState(final ModelDocument document) {
		final DocumentSnapshot snapshot = DocumentSnapshot.from(document);
		final DocumentSnapshot current = this.undoStack.peek();
		if (snapshot.equals(current)) {
			return;
		}

		this.undoStack.push(snapshot);
		while (this.undoStack.size() > UndoRedoManager.MAX_HISTORY) {
			this.undoStack.removeLast();
		}
		this.redoStack.clear();
	}

	public boolean redo(final ModelDocument document) {
		if (!this.canRedo()) {
			return false;
		}

		final DocumentSnapshot next = this.redoStack.pop();
		next.restoreInto(document);
		this.undoStack.push(next);
		return true;
	}

	public void reset(final ModelDocument document) {
		this.undoStack.clear();
		this.redoStack.clear();
		this.undoStack.push(DocumentSnapshot.from(document));
	}

	public boolean undo(final ModelDocument document) {
		if (!this.canUndo()) {
			return false;
		}

		final DocumentSnapshot current = this.undoStack.pop();
		this.redoStack.push(current);

		final DocumentSnapshot previous = this.undoStack.peek();
		if (previous == null) {
			return false;
		}

		previous.restoreInto(document);
		return true;
	}
}
