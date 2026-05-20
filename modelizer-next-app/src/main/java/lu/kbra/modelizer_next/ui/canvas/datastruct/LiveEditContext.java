package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.domain.shared.ElementStyle;

/**
 * Context for applying an inline rename to a specific model element.
 *
 * @param pos       pos value used by the operation
 * @param size      size value used by the operation
 * @param value     value to process
 * @param style     style value used by the operation
 * @param valueType type value to use
 * @param owner     parent window used for dialog ownership
 */
public record LiveEditContext(Point2D pos, @Deprecated Point2D size, Object value, ElementStyle style, Class<?> valueType, Object owner,
		boolean fixedSize) {
}
