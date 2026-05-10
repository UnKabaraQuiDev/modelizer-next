package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Pair of link anchor points used when resolving geometry.
 * @param from start point or source value
 * @param to target point or destination value
 * @param fromSide from side value used by the operation
 * @param toSide to side value used by the operation
 */
public record AnchorPair(Point2D from, Point2D to, AnchorSide fromSide, AnchorSide toSide) {
}
