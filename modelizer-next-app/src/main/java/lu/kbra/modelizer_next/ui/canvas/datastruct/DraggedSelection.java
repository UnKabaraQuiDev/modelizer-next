package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.List;

public record DraggedSelection(List<DraggedLayout> layouts, double offsetX, double offsetY, double anchorStartX, double anchorStartY) {
}
