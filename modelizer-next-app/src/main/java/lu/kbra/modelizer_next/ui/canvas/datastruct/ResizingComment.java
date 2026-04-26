package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.layout.NodeLayout;

public record ResizingComment(NodeLayout layout, double initialWidth, double initialHeight, double startWorldX, double startWorldY) {
}