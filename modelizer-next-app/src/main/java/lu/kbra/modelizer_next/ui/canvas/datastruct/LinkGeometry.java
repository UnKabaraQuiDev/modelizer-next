package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;
import java.util.List;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Resolved geometry for drawing a link path and its labels.
 *
 * @param fromPoint   point in canvas coordinates
 * @param toPoint     point in canvas coordinates
 * @param fromSide    from side value used by the operation
 * @param toSide      to side value used by the operation
 * @param labelPoint  point in canvas coordinates
 * @param middlePoint point in canvas coordinates
 * @param labelAngle  numeric label angle value
 * @param points      points in canvas coordinates
 */
public record LinkGeometry(
		Point2D fromPoint,
		Point2D toPoint,
		AnchorSide fromSide,
		AnchorSide toSide,
		Point2D labelPoint,
		Point2D middlePoint,
		double labelAngle,
		List<Point2D> points) {
}
