package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Objects;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

final class CanvasExportRenderer {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	CanvasExportRenderer(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setCanvasExportRenderer(this);
	}

	BufferedImage createExportImage(final ViewExportScope scope) {
		final Dimension exportSize = this.canvas.getExportSize(scope);
		final BufferedImage image = new BufferedImage(exportSize.width, exportSize.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			this.canvas.configureGraphics(g2);
			this.canvas.paintExport(g2, scope);
		} finally {
			g2.dispose();
		}
		return image;
	}

	BufferedImage createExportPreviewImage(final ViewExportScope scope, final int maxWidth, final int maxHeight) {
		final BufferedImage fullSizeImage = this.canvas.createExportImage(scope);
		final Dimension exportSize = new Dimension(fullSizeImage.getWidth(), fullSizeImage.getHeight());
		final double scale = Math.min(maxWidth / (double) exportSize.width, maxHeight / (double) exportSize.height);
		final double safeScale = Math.max(0.05, Math.min(1.0, scale));
		final int previewWidth = Math.max(1, (int) Math.round(exportSize.width * safeScale));
		final int previewHeight = Math.max(1, (int) Math.round(exportSize.height * safeScale));

		final BufferedImage image = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			this.canvas.configureGraphics(g2);
			g2.drawImage(fullSizeImage, 0, 0, previewWidth, previewHeight, null);
		} finally {
			g2.dispose();
		}
		return image;
	}

	Dimension getExportSize(final ViewExportScope scope) {
		final Graphics2D g2 = this.canvas.createGraphicsContext();
		try {
			return this.canvas.computeExportSize(g2, scope == null ? ViewExportScope.VIEW : scope);
		} finally {
			g2.dispose();
		}
	}

	void paintExport(final Graphics2D graphics, final ViewExportScope rawScope) {
		final ViewExportScope scope = rawScope == null ? ViewExportScope.VIEW : rawScope;
		this.canvas.invalidateConceptualAnchorCache();
		this.canvas.ensureLayouts();

		final LinkedHashSet<SelectedElement> previousFilter = this.canvas.exportSelectionFilter;
		final boolean previousSuppressSelectionDecorations = this.canvas.suppressSelectionDecorations;
		final boolean previousSuppressInteractiveOverlays = this.canvas.suppressInteractiveOverlays;

		this.canvas.exportSelectionFilter = scope == ViewExportScope.SELECTION ? new LinkedHashSet<>(this.canvas.selectedElements) : null;
		this.canvas.suppressSelectionDecorations = true;
		this.canvas.suppressInteractiveOverlays = true;

		try {
			final Dimension exportSize = this.canvas.computeExportSize(graphics, scope);
			final Rectangle2D.Double worldBounds = this.canvas.computeExportWorldBounds(graphics, scope);

			graphics.setColor(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
			graphics.fillRect(0, 0, exportSize.width, exportSize.height);
			this.canvas.drawExportGrid(graphics, exportSize);

			final AffineTransform oldTransform = graphics.getTransform();
			final double zoom = scope == ViewExportScope.VIEW ? this.canvas.getPanelState().getZoom() : 1.0;
			graphics.translate(-worldBounds.getX() * zoom, -worldBounds.getY() * zoom);
			graphics.scale(zoom, zoom);

			this.canvas.drawComments(graphics);
			this.canvas.drawClasses(graphics);
			this.canvas.drawLinks(graphics);

			graphics.setTransform(oldTransform);
		} finally {
			this.canvas.exportSelectionFilter = previousFilter;
			this.canvas.suppressSelectionDecorations = previousSuppressSelectionDecorations;
			this.canvas.suppressInteractiveOverlays = previousSuppressInteractiveOverlays;
		}
	}
}
