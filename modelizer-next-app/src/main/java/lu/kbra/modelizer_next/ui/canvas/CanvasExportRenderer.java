package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

/**
 * Contains export rendering helpers that paint the canvas into images and previews.
 */
interface CanvasExportRenderer extends DiagramCanvasExt {

	/**
	 * Creates an export image on the active canvas.
	 *
	 * @param scope export scope to use
	 * @return the created export image
	 */
	default BufferedImage createExportImage(final ViewExportScope scope, final Optional<Color> backgroundColor) {
		final Dimension exportSize = this.getCanvas().getExportSize(scope);
		final BufferedImage image = new BufferedImage(exportSize.width, exportSize.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		try {
			backgroundColor.ifPresent(c -> {
				g2.setColor(c);
				g2.fillRect(0, 0, image.getWidth(), image.getHeight());
			});

			this.getCanvas().configureGraphics(g2);
			this.getCanvas().paintExport(g2, scope);
		} finally {
			g2.dispose();
		}
		return image;
	}

	/**
	 * Creates an export preview image on the active canvas.
	 *
	 * @param scope     export scope to use
	 * @param maxWidth  width value
	 * @param maxHeight height value
	 * @return the created export preview image
	 */
	default BufferedImage createExportPreviewImage(final ViewExportScope scope, final int maxWidth, final int maxHeight, final Optional<Color> backgroundColor) {
		final BufferedImage fullSizeImage = this.getCanvas().createExportImage(scope, backgroundColor);
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

	/**
	 * Returns the export size on the active canvas.
	 *
	 * @param scope export scope to use
	 * @return the export size
	 */
	default Dimension getExportSize(final ViewExportScope scope) {
		final Graphics2D g2 = this.getCanvas().createGraphicsContext();
		try {
			return this.getCanvas().computeExportSize(g2, scope == null ? ViewExportScope.VIEW : scope);
		} finally {
			g2.dispose();
		}
	}

	/**
	 * Paints the export.
	 *
	 * @param graphics graphics context used for drawing
	 * @param rawScope raw scope value used by the operation
	 */
	default void paintExport(final Graphics2D graphics, final ViewExportScope rawScope) {
		final ViewExportScope scope = rawScope == null ? ViewExportScope.VIEW : rawScope;
		this.getCanvas().invalidateConceptualAnchorCache();
		this.getCanvas().ensureLayouts();

		final LinkedHashSet<SelectedElement> previousFilter = this.getCanvas().exportSelectionFilter;
		final boolean previousSuppressSelectionDecorations = this.getCanvas().suppressSelectionDecorations;
		final boolean previousSuppressInteractiveOverlays = this.getCanvas().suppressInteractiveOverlays;

		this.getCanvas().exportSelectionFilter = scope == ViewExportScope.SELECTION ? new LinkedHashSet<>(this.getCanvas().selectedElements)
				: null;
		if (scope == ViewExportScope.SELECTION) {
			final Set<String> classIds = new HashSet<>();
			this.getCanvas().exportSelectionFilter.parallelStream()
					.filter(c -> c.type() == SelectedType.CLASS)
					.forEach(c -> classIds.add(c.classId()));
			if (this.getPanelType() == PanelType.CONCEPTUAL) {
				this.getDocument()
						.getModel()
						.getConceptualLinks()
						.parallelStream()
						.filter(l -> classIds.contains(l.getFrom().getClassId()) && classIds.contains(l.getTo().getClassId()))
						.forEach(l -> this.getCanvas().exportSelectionFilter.add(SelectedElement.forLink(l.getId())));
			} else if (this.getPanelType().isTechnical()) {
				this.getDocument()
						.getModel()
						.getTechnicalLinks()
						.parallelStream()
						.filter(l -> classIds.contains(l.getFrom().getClassId()) && classIds.contains(l.getTo().getClassId()))
						.forEach(l -> this.getCanvas().exportSelectionFilter.add(SelectedElement.forLink(l.getId())));
			}
		}
		this.getCanvas().suppressSelectionDecorations = true;
		this.getCanvas().suppressInteractiveOverlays = true;

		try {
			final Rectangle2D.Double worldBounds = this.getCanvas().computeExportWorldBounds(graphics, scope);

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
