package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

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

interface CanvasHitTester extends DiagramCanvasExt {

	default FieldHitResult findFieldHit(final ClassModel classModel, final Rectangle2D classBounds, final Point2D.Double worldPoint) {
		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);

		for (int i = 0; i < visibleFields.size(); i++) {
			final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
					classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
					classBounds.getWidth(),
					DiagramCanvas.ROW_HEIGHT);

			if (fieldBounds.contains(worldPoint.getX(), worldPoint.getY())) {
				return new FieldHitResult(visibleFields.get(i), fieldBounds);
			}
		}

		return null;
	}

	default HitResult findTopmostHit(final Point2D.Double worldPoint) {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		try {
			for (int i = getCanvas().getActiveLinks().size() - 1; i >= 0; i--) {
				final LinkModel linkModel = getCanvas().getActiveLinks().get(i);
				final LinkGeometry geometry = getCanvas().resolveLinkGeometry(g2, linkModel);

				if (geometry != null && getCanvas().isPointNearGeometry(worldPoint, geometry)) {
					return new HitResult(null,
							new Rectangle2D.Double(worldPoint.getX(), worldPoint.getY(), 1, 1),
							SelectedElement.forLink(linkModel.getId()));
				}
			}

			for (int i = getCanvas().document.getModel().getComments().size() - 1; i >= 0; i--) {
				final CommentModel commentModel = getCanvas().document.getModel().getComments().get(i);
				final String text = getCanvas().resolveCommentText(commentModel);

				if (text == null || text.isBlank() || !getCanvas().isCommentVisible(commentModel)) {
					continue;
				}

				final NodeLayout layout = getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
				final Rectangle2D bounds = getCanvas().computeCommentBounds(g2, text, layout);

				if (bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					return new HitResult(layout, bounds, SelectedElement.forComment(commentModel.getId()));
				}
			}

			for (int i = getCanvas().document.getModel().getClasses().size() - 1; i >= 0; i--) {
				final ClassModel classModel = getCanvas().document.getModel().getClasses().get(i);
				if (!getCanvas().isVisible(classModel)) {
					continue;
				}

				final NodeLayout layout = getCanvas()
						.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
				final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);

				if (!bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					continue;
				}

				final FieldHitResult fieldHitResult = getCanvas().findFieldHit(classModel, bounds, worldPoint);
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

	default boolean isInCommentResizeHandle(final Rectangle2D bounds, final Point2D.Double worldPoint) {
		return worldPoint.getX() >= bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE
				&& worldPoint.getY() >= bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE;
	}

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
