package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.layout.NodeLayout;

/**
 * Original node layout captured when a drag operation starts.
 * @param layout layout object to read or update
 * @param startX numeric start x value
 * @param startY numeric start y value
 */
public record DraggedLayout(NodeLayout layout, double startX, double startY) {
}
