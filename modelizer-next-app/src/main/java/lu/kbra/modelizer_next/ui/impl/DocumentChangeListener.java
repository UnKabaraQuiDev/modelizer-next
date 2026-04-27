package lu.kbra.modelizer_next.ui.impl;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;

public interface DocumentChangeListener {

	DocumentChangeListener NOOP = new DocumentChangeListener() {

		@Override
		public void onSelectionChanged(SelectionInfo selectionInfo) {

		}

		@Override
		public void onDocumentChanged() {

		}

		@Override
		public void redo() {

		}

		@Override
		public void undo() {

		}

	};

	void onDocumentChanged();

	void onSelectionChanged(SelectionInfo selectionInfo);

	void redo();

	void undo();

}
