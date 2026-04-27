package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Rectangle2D;

import lu.kbra.modelizer_next.domain.FieldModel;

public record FieldHitResult(FieldModel field, Rectangle2D bounds) {
}
