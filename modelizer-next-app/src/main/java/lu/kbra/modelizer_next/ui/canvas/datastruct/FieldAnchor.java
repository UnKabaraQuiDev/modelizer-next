package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Resolved anchor point for a field row on a class node.
 * @param point point in canvas coordinates
 * @param side node side to inspect
 */
public record FieldAnchor(Point2D point, AnchorSide side) {

}
