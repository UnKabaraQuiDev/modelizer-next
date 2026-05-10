package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;

/**
 * Computed colors used to draw status-dependent UI elements.
 * @param foreground foreground color to use
 * @param background background color to use
 * @param border border value used by the operation
 */
public record StatusStyleAppearance(Color foreground, Color background, Color border) {
}
