package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;
import java.util.List;

public record LinkGeometry(Point2D fromPoint, Point2D toPoint, Point2D labelPoint, Point2D middlePoint, double labelAngle,
		List<Point2D> points) {
}
