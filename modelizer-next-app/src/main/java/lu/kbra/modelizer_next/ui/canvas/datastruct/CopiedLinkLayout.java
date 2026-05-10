package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Link layout data stored in the clipboard snapshot.
 *
 * @param bendPoints        points in canvas coordinates
 * @param nameLabelPosition name label position value used by the operation
 */
public record CopiedLinkLayout(List<Point2D.Double> bendPoints, Point2D.Double nameLabelPosition) {
}
