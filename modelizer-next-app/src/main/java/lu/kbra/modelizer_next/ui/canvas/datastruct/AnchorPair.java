package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

public record AnchorPair(Point2D from, Point2D to, AnchorSide fromSide, AnchorSide toSide) {
}
