package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.List;

/**
 * Selection state captured while selected elements are dragged.
 * @param layouts layout objects to read or modify
 * @param offsetX numeric offset x value
 * @param offsetY numeric offset y value
 * @param anchorStartX numeric anchor start x value
 * @param anchorStartY numeric anchor start y value
 */
public record DraggedSelection(List<DraggedLayout> layouts, double offsetX, double offsetY, double anchorStartX, double anchorStartY) {
}
