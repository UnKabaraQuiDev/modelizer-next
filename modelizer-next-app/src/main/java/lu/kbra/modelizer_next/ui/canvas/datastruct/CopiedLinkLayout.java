package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;
import java.util.List;

public record CopiedLinkLayout(List<Point2D.Double> bendPoints, Point2D.Double nameLabelPosition) {
}