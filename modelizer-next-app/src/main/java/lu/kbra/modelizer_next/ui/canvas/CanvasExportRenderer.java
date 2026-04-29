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
		final Dimension exportSize = getCanvas().getExportSize(scope);
		final BufferedImage image = new BufferedImage(exportSize.width, exportSize.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			getCanvas().configureGraphics(g2);
			getCanvas().paintExport(g2, scope);
		} finally {
			g2.dispose();
		}
		return image;
	}

	default BufferedImage createExportPreviewImage(final ViewExportScope scope, final int maxWidth, final int maxHeight) {
		final BufferedImage fullSizeImage = getCanvas().createExportImage(scope);
		final Dimension exportSize = new Dimension(fullSizeImage.getWidth(), fullSizeImage.getHeight());
		final double scale = Math.min(maxWidth / (double) exportSize.width, maxHeight / (double) exportSize.height);
		final double safeScale = Math.max(0.05, Math.min(1.0, scale));
		final int previewWidth = Math.max(1, (int) Math.round(exportSize.width * safeScale));
		final int previewHeight = Math.max(1, (int) Math.round(exportSize.height * safeScale));

		final BufferedImage image = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			getCanvas().configureGraphics(g2);
			g2.drawImage(fullSizeImage, 0, 0, previewWidth, previewHeight, null);
		} finally {
			g2.dispose();
		}
		return image;
	}

	default Dimension getExportSize(final ViewExportScope scope) {
		final Graphics2D g2 = getCanvas().createGraphicsContext();
		try {
			return getCanvas().computeExportSize(g2, scope == null ? ViewExportScope.VIEW : scope);
		} finally {
			g2.dispose();
		}
	}

	default void paintExport(final Graphics2D graphics, final ViewExportScope rawScope) {
		final ViewExportScope scope = rawScope == null ? ViewExportScope.VIEW : rawScope;
		getCanvas().invalidateConceptualAnchorCache();
		getCanvas().ensureLayouts();

		final LinkedHashSet<SelectedElement> previousFilter = getCanvas().exportSelectionFilter;
		final boolean previousSuppressSelectionDecorations = getCanvas().suppressSelectionDecorations;
		final boolean previousSuppressInteractiveOverlays = getCanvas().suppressInteractiveOverlays;

		getCanvas().exportSelectionFilter = scope == ViewExportScope.SELECTION ? new LinkedHashSet<>(getCanvas().selectedElements) : null;
		getCanvas().suppressSelectionDecorations = true;
		getCanvas().suppressInteractiveOverlays = true;

		try {
			final Dimension exportSize = getCanvas().computeExportSize(graphics, scope);
			final Rectangle2D.Double worldBounds = getCanvas().computeExportWorldBounds(graphics, scope);

			graphics.setColor(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
			graphics.fillRect(0, 0, exportSize.width, exportSize.height);
			getCanvas().drawExportGrid(graphics, exportSize);

			final AffineTransform oldTransform = graphics.getTransform();
			final double zoom = scope == ViewExportScope.VIEW ? getCanvas().getPanelState().getZoom() : 1.0;
			graphics.translate(-worldBounds.getX() * zoom, -worldBounds.getY() * zoom);
			graphics.scale(zoom, zoom);

			getCanvas().drawComments(graphics);
			getCanvas().drawClasses(graphics);
			getCanvas().drawLinks(graphics);

			graphics.setTransform(oldTransform);
		} finally {
			getCanvas().exportSelectionFilter = previousFilter;
			getCanvas().suppressSelectionDecorations = previousSuppressSelectionDecorations;
			getCanvas().suppressInteractiveOverlays = previousSuppressInteractiveOverlays;
		}
	}

}
