package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.FieldHitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.HitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

/**
 * Contains hit-testing helpers that map mouse positions to canvas elements.
 */
interface CanvasHitTester extends DiagramCanvasExt {

	/**
	 * Finds the field hit that matches the supplied input.
	 * @param classModel class model affected by the operation
	 * @param classBounds bounds used for layout or hit testing
	 * @param worldPoint point in canvas coordinates
	 * @return the matching field hit, or {@code null} when no match exists
	 */
	default FieldHitResult findFieldHit(final ClassModel classModel, final Rectangle2D classBounds, final Point2D.Double worldPoint) {
		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);

		for (int i = 0; i < visibleFields.size(); i++) {
			final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
					classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT,
					classBounds.getWidth(),
					DiagramCanvas.CLASS_ROW_HEIGHT);

			if (fieldBounds.contains(worldPoint.getX(), worldPoint.getY())) {
				return new FieldHitResult(visibleFields.get(i), fieldBounds);
			}
		}

		return null;
	}

	/**
	 * Finds the topmost hit that matches the supplied input.
	 * @param worldPoint point in canvas coordinates
	 * @return the matching topmost hit, or {@code null} when no match exists
	 */
	default HitResult findTopmostHit(final Point2D.Double worldPoint) {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		try {
			for (int i = this.getCanvas().getActiveLinks().size() - 1; i >= 0; i--) {
				final LinkModel linkModel = this.getCanvas().getActiveLinks().get(i);
				final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(linkModel);

				if (geometry != null && this.getCanvas().isPointNearGeometry(worldPoint, geometry)) {
					return new HitResult(null,
							new Rectangle2D.Double(worldPoint.getX(), worldPoint.getY(), 1, 1),
							SelectedElement.forLink(linkModel.getId()));
				}
			}

			for (int i = this.getCanvas().document.getModel().getComments().size() - 1; i >= 0; i--) {
				final CommentModel commentModel = this.getCanvas().document.getModel().getComments().get(i);
				if (commentModel == null || !this.getCanvas().isCommentVisible(commentModel)) {
					continue;
				}

				final NodeLayout layout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
				final Rectangle2D bounds = this.getCanvas().computeCommentBounds(commentModel.getText(), layout);

				if (bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					return new HitResult(layout, bounds, SelectedElement.forComment(commentModel.getId()));
				}
			}

			for (int i = this.getCanvas().document.getModel().getClasses().size() - 1; i >= 0; i--) {
				final ClassModel classModel = this.getCanvas().document.getModel().getClasses().get(i);
				if (!classModel.isVisible(this.getPanelType())) {
					continue;
				}

				final NodeLayout layout = this.getCanvas()
						.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
				final Rectangle2D bounds = this.getCanvas().computeClassBounds(classModel, layout);

				if (!bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					continue;
				}

				final FieldHitResult fieldHitResult = this.getCanvas().findFieldHit(classModel, bounds, worldPoint);
				if (fieldHitResult != null) {
					return new HitResult(layout,
							fieldHitResult.bounds(),
							SelectedElement.forField(classModel.getId(), fieldHitResult.field().getId()));
				}

				return new HitResult(layout, bounds, SelectedElement.forClass(classModel.getId()));
			}
		} finally {
			g2.dispose();
		}

		return null;
	}

	/**
	 * Checks whether in comment resize handle is enabled or applies.
	 * @param bounds bounds used for layout or hit testing
	 * @param worldPoint point in canvas coordinates
	 * @return {@code true} if in comment resize handle is enabled or applies; otherwise {@code false}
	 */
	default boolean isInCommentResizeHandle(final Rectangle2D bounds, final Point2D.Double worldPoint) {
		return worldPoint.getX() >= bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE
				&& worldPoint.getY() >= bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE;
	}

	/**
	 * Checks whether point near geometry is enabled or applies on the active canvas.
	 * @param worldPoint point in canvas coordinates
	 * @param geometry resolved link geometry to draw or inspect
	 * @return {@code true} if point near geometry is enabled or applies; otherwise {@code false}
	 */
	default boolean isPointNearGeometry(final Point2D.Double worldPoint, final LinkGeometry geometry) {
		for (int i = 0; i < geometry.points().size() - 1; i++) {
			final Point2D first = geometry.points().get(i);
			final Point2D second = geometry.points().get(i + 1);

			if (Line2D.ptSegDist(first.getX(),
					first.getY(),
					second.getX(),
					second.getY(),
					worldPoint.getX(),
					worldPoint.getY()) <= DiagramCanvas.LINK_HIT_DISTANCE) {
				return true;
			}
		}

		return false;
	}

}
