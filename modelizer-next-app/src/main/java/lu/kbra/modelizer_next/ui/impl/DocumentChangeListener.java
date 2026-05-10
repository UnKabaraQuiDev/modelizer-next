package lu.kbra.modelizer_next.ui.impl;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;

/**
 * Listener notified when a document changes and the UI needs to update dirty state or history.
 */
public interface DocumentChangeListener {

	DocumentChangeListener NOOP = new DocumentChangeListener() {

		@Override
		public void onDocumentChanged() {

		}

		@Override
		public void onSelectionChanged(final SelectionInfo selectionInfo) {

		}

		@Override
		public void redo() {

		}

		@Override
		public void undo() {

		}

	};

	/**
	 * Handles the document changed event.
	 */
	void onDocumentChanged();

	/**
	 * Handles the selection changed event.
	 * @param selectionInfo selection info value used by the operation
	 */
	void onSelectionChanged(SelectionInfo selectionInfo);

	/**
	 * Restores the next snapshot in the redo history.
	 */
	void redo();

	/**
	 * Restores the previous snapshot in the undo history.
	 */
	void undo();

}
