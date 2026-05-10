package lu.kbra.modelizer_next.history;

import java.util.ArrayDeque;
import java.util.Deque;

import lu.kbra.modelizer_next.document.ModelDocument;

/**
 * Manages the document snapshot history and exposes undo/redo navigation.
 */
public class UndoRedoManager {

	private static final int MAX_HISTORY = 100;

	private final Deque<DocumentSnapshot> undoStack = new ArrayDeque<>();
	private final Deque<DocumentSnapshot> redoStack = new ArrayDeque<>();

	/**
	 * Checks whether this object can redo.
	 *
	 * @return {@code true} if the operation is allowed; otherwise {@code false}
	 */
	public boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	/**
	 * Checks whether this object can undo.
	 *
	 * @return {@code true} if the operation is allowed; otherwise {@code false}
	 */
	public boolean canUndo() {
		return this.undoStack.size() > 1;
	}

	/**
	 * Records the state in history.
	 *
	 * @param document document to read or modify
	 */
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

	/**
	 * Restores the next snapshot in the redo history.
	 *
	 * @param document document to read or modify
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public boolean redo(final ModelDocument document) {
		if (!this.canRedo()) {
			return false;
		}

		final DocumentSnapshot next = this.redoStack.pop();
		next.restoreInto(document);
		this.undoStack.push(next);
		return true;
	}

	/**
	 * Clears the current state and starts a new history baseline.
	 *
	 * @param document document to read or modify
	 */
	public void reset(final ModelDocument document) {
		this.undoStack.clear();
		this.redoStack.clear();
		this.undoStack.push(DocumentSnapshot.from(document));
	}

	/**
	 * Restores the previous snapshot in the undo history.
	 *
	 * @param document document to read or modify
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
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
