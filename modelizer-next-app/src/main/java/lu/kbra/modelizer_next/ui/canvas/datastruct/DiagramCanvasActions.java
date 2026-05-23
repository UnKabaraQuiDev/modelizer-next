package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.function.IntConsumer;

/**
 * Runnable actions registered as keyboard shortcuts on the canvas.
 *
 * @param renameSelection         rename selection value used by the operation
 * @param renameSelectionAlt      rename selection alt value used by the operation
 * @param moveFieldSelection      move field selection value used by the operation
 * @param moveSelectedFieldInList list to read or update
 * @param addTable                add table value used by the operation
 * @param addField                add field value used by the operation
 * @param addComment              add comment value used by the operation
 * @param deleteSelection         delete selection value used by the operation
 * @param duplicateSelection      duplicate selection value used by the operation
 * @param clearSelection          clear selection value used by the operation
 * @param addLink                 add link value used by the operation
 * @param selectAll               select all value used by the operation
 * @param edit                    edit value used by the operation
 * @param copySelection           copy selection value used by the operation
 * @param cutSelection            cut selection value used by the operation
 * @param pasteSelection          paste selection value used by the operation
 * @param undo                    undo value used by the operation
 * @param redo                    redo value used by the operation
 * @param editStyle               edit style value used by the operation
 * @param editStyleAlt            edit style alt value used by the operation
 * @param focusSelection          move the view to be centered around the selected element(s)
 * @param focusAll                move the view to contain all the elements in the current canvas
 */
public record DiagramCanvasActions(Runnable renameSelection, Runnable renameSelectionAlt, IntConsumer moveFieldSelection,
		IntConsumer moveSelectedFieldInList, Runnable addTable, Runnable addField, Runnable addComment, Runnable deleteSelection,
		Runnable duplicateSelection, Runnable clearSelection, Runnable addLink, Runnable selectAll, Runnable edit, Runnable copySelection,
		Runnable cutSelection, Runnable pasteSelection, Runnable undo, Runnable redo, Runnable editStyle, Runnable editStyleAlt,
		Runnable focusSelection, Runnable focusAll) {

}
