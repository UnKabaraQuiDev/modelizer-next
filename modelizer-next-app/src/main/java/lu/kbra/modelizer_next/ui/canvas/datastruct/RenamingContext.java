package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.domain.shared.ElementStyle;

public record RenamingContext(Point2D pos, Point2D size, Object value, ElementStyle style, Class<?> valueType, Object owner) {
}
