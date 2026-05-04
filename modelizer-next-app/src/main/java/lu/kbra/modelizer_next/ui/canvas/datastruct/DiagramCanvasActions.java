package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.function.IntConsumer;

public record DiagramCanvasActions(Runnable renameSelection, IntConsumer moveFieldSelection, IntConsumer moveSelectedFieldInList,
		Runnable addTable, Runnable addField, Runnable addComment, Runnable deleteSelection, Runnable duplicateSelection,
		Runnable clearSelection, Runnable addLink, Runnable selectAll, Runnable edit, Runnable copySelection, Runnable cutSelection,
		Runnable pasteSelection, Runnable undo, Runnable redo) {
}