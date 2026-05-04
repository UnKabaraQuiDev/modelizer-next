package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;
import java.util.List;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

public record LinkGeometry(Point2D fromPoint, Point2D toPoint, AnchorSide fromSide, AnchorSide toSide, Point2D labelPoint,
		Point2D middlePoint, double labelAngle, List<Point2D> points) {
}
