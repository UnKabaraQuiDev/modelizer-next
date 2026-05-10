package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Rectangle2D;

import lu.kbra.modelizer_next.layout.NodeLayout;

/**
 * Hit-test result that identifies the element under the pointer.
 *
 * @param layout    layout object to read or update
 * @param bounds    bounds used for layout or hit testing
 * @param selection selection state to read or update
 */
public record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
}
