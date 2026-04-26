package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics;
import java.util.Objects;

final class CanvasRenderer {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	CanvasRenderer(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setCanvasRenderer(this);
	}

	void paintComponent(final Graphics graphics) {
		this.canvas.paintCanvasComponent(graphics);
	}
}
