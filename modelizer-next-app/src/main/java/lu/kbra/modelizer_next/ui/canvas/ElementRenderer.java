package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

/**
 * Contains drawing helpers for classes, comments, fields, links, and overlays.
 */
public interface ElementRenderer extends DiagramCanvasExt {

	default void drawAlignedLinkLabel(final Graphics2D g2, final String text, final Point2D center, final double angle) {
		final Graphics2D labelGraphics = (Graphics2D) g2.create();
		try {
			final FontMetrics metrics = labelGraphics.getFontMetrics();
			final Rectangle2D textBounds = metrics.getStringBounds(text, labelGraphics);

			final double normalX = -Math.sin(angle);
			final double normalY = Math.cos(angle);
			final double offset = -8;

			labelGraphics.translate(center.getX() + normalX * offset, center.getY() + normalY * offset);
			labelGraphics.rotate(angle);

			labelGraphics.setColor(g2.getColor());
			labelGraphics
					.drawString(text, (float) (-textBounds.getWidth() / 2.0), (float) (metrics.getAscent() - textBounds.getHeight() / 2.0));
		} finally {
			labelGraphics.dispose();
		}
	}

	default void drawArrowHead(final Graphics2D g2, final Point2D from, final Point2D to) {
		final double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
		final double arrowLength = 12.0;
		final double wingAngle = Math.PI / 7.0;

		final Point2D left = new Point2D.Double(to.getX() - arrowLength * Math.cos(angle - wingAngle),
				to.getY() - arrowLength * Math.sin(angle - wingAngle));
		final Point2D right = new Point2D.Double(to.getX() - arrowLength * Math.cos(angle + wingAngle),
				to.getY() - arrowLength * Math.sin(angle + wingAngle));

		g2.draw(new Line2D.Double(to, left));
		g2.draw(new Line2D.Double(to, right));
	}

	default void drawAssociationClassConnector(final Graphics2D g2, final LinkModel linkModel, final LinkGeometry geometry) {
		if (this.getPanelType() != PanelType.CONCEPTUAL || !this.getCanvas().hasAssociationClass(linkModel) || geometry == null) {
			return;
		}

		final ClassModel associationClass = this.getCanvas().findClassById(linkModel.getAssociationClassId());
		if (associationClass == null || !associationClass.isVisible(this.getPanelType()) || geometry.middlePoint() == null) {
			return;
		}

		final Point2D associationAnchor = this.getCanvas()
				.resolveConceptualPreviewAnchor(g2, associationClass.getId(), geometry.middlePoint());

		if (associationAnchor == null) {
			return;
		}

		final Graphics2D connectorGraphics = (Graphics2D) g2.create();
		try {
			connectorGraphics.setColor(
					this.getCanvas().isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			connectorGraphics
					.setStroke(this.getCanvas().isLinkSelected(linkModel.getId()) ? DiagramCanvas.ASSOCIATION_CONNECTOR_SELECTION_STROKE
							: DiagramCanvas.ASSOCIATION_CONNECTOR_DEFAULT_STROKE);
			connectorGraphics.draw(new Line2D.Double(associationAnchor, geometry.middlePoint()));
		} finally {
			connectorGraphics.dispose();
		}
	}

	default void drawCardinalityLabel(
			final Graphics2D g2,
			final String text,
			final Point2D anchor,
			final Point2D adjacentPoint,
			final double angle) {
		final double dx = adjacentPoint.getX() - anchor.getX();
		final double dy = adjacentPoint.getY() - anchor.getY();

		double ux = 0.0;
		double uy = 0.0;
		final double length = Math.hypot(dx, dy);
		if (length > 0.0) {
			ux = dx / length;
			uy = dy / length;
		}

		final double alongOffset = 16.0;

		final Point2D center = new Point2D.Double(anchor.getX() + ux * alongOffset, anchor.getY() + uy * alongOffset);

		this.getCanvas().drawAlignedLinkLabel(g2, text, center, angle);
	}

	default void drawClasses(final Graphics2D g2) {
		for (final ClassModel classModel : this.getCanvas().document.getModel().getClasses()) {
			if (!classModel.isVisible(this.getPanelType()) || !this.getCanvas().shouldExportClass(classModel)) {
				continue;
			}

			final NodeLayout layout = this.getCanvas()
					.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.getCanvas().computeClassBounds(g2, classModel, layout);
			this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()).getSize().setWidth(bounds.getWidth());
			this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()).getSize().setHeight(bounds.getHeight());

			g2.setColor(classModel.getBackgroundColor());
			g2.fill(bounds);

			g2.setFont(DiagramCanvas.TITLE_FONT);
			g2.setColor(classModel.getTextColor());
			g2.drawString(this.getCanvas().resolveClassTitle(classModel),
					(float) bounds.getX() + DiagramCanvas.PADDING,
					(float) bounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT - 9);

			g2.setFont(DiagramCanvas.BODY_FONT);
			double rowY = bounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT;
			final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);

			for (final FieldModel fieldModel : visibleFields) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(bounds.getX(),
						rowY,
						bounds.getWidth(),
						DiagramCanvas.CLASS_ROW_HEIGHT);

				g2.setColor(fieldModel.getBackgroundColor());
				g2.fill(fieldBounds);

				if (this.getCanvas().isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(DiagramCanvas.SELECTION_FILL_COLOR);
					g2.fill(fieldBounds);
				}

				g2.setColor(classModel.getBorderColor());
				g2.draw(new Line2D.Double(bounds.getX(), rowY, bounds.getMaxX(), rowY));

				g2.setColor(fieldModel.getTextColor());
				g2.drawString(this.getCanvas().resolveFieldName(fieldModel),
						(float) bounds.getX() + DiagramCanvas.PADDING,
						(float) rowY + 15);

				if (this.getCanvas().isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(DiagramCanvas.SELECTION_COLOR);
					g2.setStroke(DiagramCanvas.FIELD_SELECTION_STROKE);
					g2.draw(fieldBounds);
					g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
				}

				rowY += DiagramCanvas.CLASS_ROW_HEIGHT;
			}

			if (this.getCanvas().isClassSelected(classModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.setStroke(DiagramCanvas.SELECTION_STROKE);
				g2.draw(bounds);
				g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
			} else {
				g2.setColor(classModel.getBorderColor());
				g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
				g2.draw(bounds);
				g2.draw(new Line2D.Double(bounds.getX(),
						bounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT,
						bounds.getMaxX(),
						bounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT));
			}
		}
	}

	default void drawCommentConnector(final Graphics2D g2, final CommentModel commentModel, final Rectangle2D bounds) {
		if (commentModel.getKind() != CommentKind.BOUND || commentModel.getBinding() == null) {
			return;
		}

		final Point2D anchor = this.getCanvas().findBoundTargetAnchor(g2, commentModel);
		if (anchor == null) {
			return;
		}

		g2.setColor(this.getCanvas().isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR
				: DiagramCanvas.COMMENT_CONNECTOR_COLOR);
		g2.setStroke(this.getCanvas().isCommentSelected(commentModel.getId()) ? DiagramCanvas.COMMENT_CONNECTOR_SELECTION_STROKE
				: DiagramCanvas.DEFAULT_STROKE);
		g2.draw(new Line2D.Double(anchor.getX(), anchor.getY(), bounds.getCenterX(), bounds.getCenterY()));
		g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
	}

	default void drawComments(final Graphics2D g2) {
		for (final CommentModel commentModel : this.getCanvas().document.getModel().getComments()) {
			final String commentText = this.getCanvas().resolveCommentText(commentModel);
			if (commentText == null || commentText.isBlank() || !this.getCanvas().isCommentVisible(commentModel)
					|| !this.getCanvas().shouldExportComment(commentModel)) {
				continue;
			}

			final NodeLayout layout = this.getCanvas()
					.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
			final Rectangle2D bounds = this.getCanvas().computeCommentBounds(g2, commentText, layout);
			this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()).getSize().setWidth(bounds.getWidth());
			this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()).getSize().setHeight(bounds.getHeight());

			this.getCanvas().drawCommentConnector(g2, commentModel, bounds);

			g2.setColor(commentModel.getBackgroundColor());
			g2.fill(bounds);

			g2.setColor(this.getCanvas().isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR
					: commentModel.getBorderColor());
			g2.setStroke(this.getCanvas().isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_STROKE
					: DiagramCanvas.DEFAULT_STROKE);
			g2.draw(bounds);
			g2.setStroke(DiagramCanvas.DEFAULT_STROKE);

			g2.setFont(DiagramCanvas.BODY_FONT);
			g2.setColor(commentModel.getTextColor());
			this.getCanvas().drawMultilineText(g2, commentText, bounds, DiagramCanvas.PADDING);

			if (this.getCanvas().isCommentSelected(commentModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.fill(new Rectangle2D.Double(bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE));
			}
		}
	}

	default void drawExportGrid(final Graphics2D g2, final Dimension size) {
		g2.setColor(DiagramCanvas.GRID_COLOR);
		for (int x = 0; x < size.width; x += 40) {
			g2.drawLine(x, 0, x, size.height);
		}
		for (int y = 0; y < size.height; y += 40) {
			g2.drawLine(0, y, size.width, y);
		}
	}

	default void drawGrid(final Graphics2D g2) {
		g2.setColor(DiagramCanvas.GRID_COLOR);
		for (int x = 0; x < this.getCanvas().getWidth(); x += 40) {
			g2.drawLine(x, 0, x, this.getCanvas().getHeight());
		}
		for (int y = 0; y < this.getCanvas().getHeight(); y += 40) {
			g2.drawLine(0, y, this.getCanvas().getWidth(), y);
		}
	}

	default void drawLinkPreview(final Graphics2D g2) {
		if (this.getCanvas().suppressInteractiveOverlays || this.getCanvas().linkCreationState == null) {
			return;
		}

		final Point2D fromAnchor = this.getCanvas().resolvePreviewSourceAnchor(g2);
		if (fromAnchor == null) {
			return;
		}

		final SelectedElement target = this.getCanvas().linkPreviewTarget;
		final boolean valid = this.getCanvas().isValidPreviewTarget(target);

		final Point2D toAnchor;
		if (target != null) {
			toAnchor = this.getCanvas().resolvePreviewTargetAnchor(g2, target);
		} else {
			toAnchor = this.getCanvas().linkPreviewMousePoint;
		}

		if (toAnchor == null) {
			return;
		}

		final Graphics2D previewGraphics = (Graphics2D) g2.create();
		try {
			previewGraphics.setColor(valid ? DiagramCanvas.SELECTION_COLOR : Color.RED);
			previewGraphics.setStroke(DiagramCanvas.LINK_PREVIEW_STROKE);
			previewGraphics.draw(new Line2D.Double(fromAnchor, toAnchor));
		} finally {
			previewGraphics.dispose();
		}
	}

	default void drawLinks(final Graphics2D g2) {
		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			this.getCanvas().ensureConceptualAnchorCache(g2);
		}

		g2.setFont(DiagramCanvas.BODY_FONT);

		for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
			if (!this.getCanvas().shouldExportLink(linkModel)) {
				continue;
			}

			final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(g2, linkModel);
			if (geometry == null) {
				continue;
			}

			g2.setColor(this.getCanvas().isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			g2.setStroke(this.getCanvas().isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_STROKE
					: DiagramCanvas.LINK_DEFAULT_STROKE);

			for (int i = 0; i < geometry.points().size() - 1; i++) {
				g2.draw(new Line2D.Double(geometry.points().get(i), geometry.points().get(i + 1)));
			}

			if (this.getPanelType() != PanelType.CONCEPTUAL) {
				this.getCanvas().drawArrowHead(g2, geometry.points().get(geometry.points().size() - 2), geometry.toPoint());
			}

			g2.setStroke(DiagramCanvas.DEFAULT_STROKE);

			if (this.getPanelType() == PanelType.CONCEPTUAL && linkModel.getName() != null && !linkModel.getName().isBlank()) {
				this.getCanvas().drawAlignedLinkLabel(g2, linkModel.getName(), geometry.labelPoint(), geometry.labelAngle());
			}

			if (this.getPanelType() == PanelType.CONCEPTUAL) {
				// TODO: fix getCanvas()
				final String from = (linkModel.getCardinalityFrom() != null ? linkModel.getCardinalityFrom().getDisplayValue() : "") + " "
						+ (linkModel.getLabelFrom() != null ? linkModel.getLabelFrom() : "");
				if (!from.isBlank()) {
					this.getCanvas().drawCardinalityLabel(g2, from, geometry.fromPoint(), geometry.points().get(1), geometry.labelAngle());
				}

				final String to = (linkModel.getCardinalityTo() != null ? linkModel.getCardinalityTo().getDisplayValue() : "") + " "
						+ (linkModel.getLabelTo() != null ? linkModel.getLabelTo() : "");
				if (!to.isBlank()) {
					this.getCanvas()
							.drawCardinalityLabel(g2,
									to,
									geometry.toPoint(),
									geometry.points().get(geometry.points().size() - 2),
									geometry.labelAngle());
				}
			}

			this.getCanvas().drawAssociationClassConnector(g2, linkModel, geometry);
		}
	}

	default void drawMultilineText(final Graphics2D g2, final String text, final Rectangle2D bounds, final int padding) {
		final FontMetrics metrics = g2.getFontMetrics();
		final List<String> wrappedLines = this.getCanvas().wrapText(text, metrics, (int) Math.max(40, bounds.getWidth() - padding * 2));

		float y = (float) bounds.getY() + padding + metrics.getAscent();
		for (final String line : wrappedLines) {
			g2.drawString(line, (float) bounds.getX() + padding, y);
			y += metrics.getHeight() + 2;
		}
	}

}
