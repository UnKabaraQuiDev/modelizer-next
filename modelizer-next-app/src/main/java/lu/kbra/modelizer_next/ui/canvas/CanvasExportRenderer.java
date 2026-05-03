package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;

import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

/**
 * Contains export rendering helpers that paint the canvas into images and previews.
 */
interface CanvasExportRenderer extends DiagramCanvasExt {

	default BufferedImage createExportImage(final ViewExportScope scope) {
		final Dimension exportSize = this.getCanvas().getExportSize(scope);
		final BufferedImage image = new BufferedImage(exportSize.width, exportSize.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			this.getCanvas().configureGraphics(g2);
			this.getCanvas().paintExport(g2, scope);
		} finally {
			g2.dispose();
		}
		return image;
	}

	default BufferedImage createExportPreviewImage(final ViewExportScope scope, final int maxWidth, final int maxHeight) {
		final BufferedImage fullSizeImage = this.getCanvas().createExportImage(scope);
		final Dimension exportSize = new Dimension(fullSizeImage.getWidth(), fullSizeImage.getHeight());
		final double scale = Math.min(maxWidth / (double) exportSize.width, maxHeight / (double) exportSize.height);
		final double safeScale = Math.max(0.05, Math.min(1.0, scale));
		final int previewWidth = Math.max(1, (int) Math.round(exportSize.width * safeScale));
		final int previewHeight = Math.max(1, (int) Math.round(exportSize.height * safeScale));

		final BufferedImage image = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			this.getCanvas().configureGraphics(g2);
			g2.drawImage(fullSizeImage, 0, 0, previewWidth, previewHeight, null);
		} finally {
			g2.dispose();
		}
		return image;
	}

	default Dimension getExportSize(final ViewExportScope scope) {
		final Graphics2D g2 = this.getCanvas().createGraphicsContext();
		try {
			return this.getCanvas().computeExportSize(g2, scope == null ? ViewExportScope.VIEW : scope);
		} finally {
			g2.dispose();
		}
	}

	default void paintExport(final Graphics2D graphics, final ViewExportScope rawScope) {
		final ViewExportScope scope = rawScope == null ? ViewExportScope.VIEW : rawScope;
		this.getCanvas().invalidateConceptualAnchorCache();
		this.getCanvas().ensureLayouts();

		final LinkedHashSet<SelectedElement> previousFilter = this.getCanvas().exportSelectionFilter;
		final boolean previousSuppressSelectionDecorations = this.getCanvas().suppressSelectionDecorations;
		final boolean previousSuppressInteractiveOverlays = this.getCanvas().suppressInteractiveOverlays;

		this.getCanvas().exportSelectionFilter = scope == ViewExportScope.SELECTION ? new LinkedHashSet<>(this.getCanvas().selectedElements)
				: null;
		this.getCanvas().suppressSelectionDecorations = true;
		this.getCanvas().suppressInteractiveOverlays = true;

		try {
			final Dimension exportSize = this.getCanvas().computeExportSize(graphics, scope);
			final Rectangle2D.Double worldBounds = this.getCanvas().computeExportWorldBounds(graphics, scope);

			graphics.setColor(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
			graphics.fillRect(0, 0, exportSize.width, exportSize.height);
			this.getCanvas().drawExportGrid(graphics, exportSize);

			final AffineTransform oldTransform = graphics.getTransform();
			final double zoom = scope == ViewExportScope.VIEW ? this.getCanvas().getPanelState().getZoom() : 1.0;
			graphics.translate(-worldBounds.getX() * zoom, -worldBounds.getY() * zoom);
			graphics.scale(zoom, zoom);

			this.getCanvas().drawComments(graphics);
			this.getCanvas().drawClasses(graphics);
			this.getCanvas().drawLinks(graphics);

			graphics.setTransform(oldTransform);
		} finally {
			this.getCanvas().exportSelectionFilter = previousFilter;
			this.getCanvas().suppressSelectionDecorations = previousSuppressSelectionDecorations;
			this.getCanvas().suppressInteractiveOverlays = previousSuppressInteractiveOverlays;
		}
	}

}
