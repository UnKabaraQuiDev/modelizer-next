package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Rectangle2D;

import lu.kbra.modelizer_next.domain.FieldModel;

/**
 * Hit-test result for a field row inside a class node.
 * @param field field value used by the operation
 * @param bounds bounds used for layout or hit testing
 */
public record FieldHitResult(FieldModel field, Rectangle2D bounds) {
}
