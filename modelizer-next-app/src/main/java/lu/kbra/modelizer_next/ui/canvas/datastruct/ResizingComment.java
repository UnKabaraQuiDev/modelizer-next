package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.layout.NodeLayout;

/**
 * State kept while a comment node is resized.
 *
 * @param layout        layout object to read or update
 * @param initialWidth  width value
 * @param initialHeight height value
 * @param startWorldX   numeric start world x value
 * @param startWorldY   numeric start world y value
 */
public record ResizingComment(NodeLayout layout, double initialWidth, double initialHeight, double startWorldX, double startWorldY) {
}
