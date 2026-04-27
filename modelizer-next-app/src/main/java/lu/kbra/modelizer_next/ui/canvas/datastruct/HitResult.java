package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.geom.Rectangle2D;

import lu.kbra.modelizer_next.layout.NodeLayout;

public record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
}
