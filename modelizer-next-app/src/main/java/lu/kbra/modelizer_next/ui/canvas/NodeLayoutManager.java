package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLinkLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedNodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedLayout;

/**
 * Contains node movement and layout update helpers.
 */
public interface NodeLayoutManager extends DiagramCanvasExt {

	/**
	 * Applies the node layout.
	 *
	 * @param type         type value that selects the operation mode
	 * @param objectId     id of the element to read or modify
	 * @param copiedLayout layout object to read or modify
	 * @param deltaX       numeric delta x value
	 * @param deltaY       numeric delta y value
	 */
	default void applyNodeLayout(
			final LayoutObjectType type,
			final String objectId,
			final CopiedNodeLayout copiedLayout,
			final double deltaX,
			final double deltaY) {

		final NodeLayout layout = this.getCanvas().findOrCreateNodeLayout(type, objectId);

		layout.setPosition(new Point2D.Double(copiedLayout.x() + deltaX, copiedLayout.y() + deltaY));
		layout.setSize(new Size2D(copiedLayout.width(), copiedLayout.height()));
	}

	/**
	 * Computes the class bounds.
	 *
	 * @param classModel class model affected by the operation
	 * @param layout     layout object to read or update
	 * @return the compute class bounds result
	 */
	default Rectangle2D computeClassBounds(final ClassModel classModel, final NodeLayout layout) {
		int width = (int) Math.max(DiagramCanvas.CLASS_MIN_WIDTH,
				this.getCanvas().stringWidth(DiagramCanvas.TITLE_FONT, this.getCanvas().resolveClassTitle(classModel))
						+ DiagramCanvas.TEXT_PADDING * 2);

		if (getPanelType() == PanelType.CONCEPTUAL || getPanelType() == PanelType.LOGICAL) {
			for (final FieldModel fieldModel : classModel.getFields(getPanelType())) {
				width = (int) Math.max(width,
						this.getCanvas().stringWidth(DiagramCanvas.BODY_FONT, this.getCanvas().resolveFieldName(fieldModel))
								+ DiagramCanvas.TEXT_PADDING * 2);
			}
		} else {
			width = (int) Math.max(width,
					(double) getCanvas().resolveClassColumWidths(classModel.getFields(getPanelType())).map((a, b, c) -> a + b + c)
							+ 3 * DiagramCanvas.TEXT_PADDING);
		}

		final int visibleFieldCount = classModel.getFieldCount(getPanelType());
		final int height = DiagramCanvas.CLASS_HEADER_HEIGHT + visibleFieldCount * DiagramCanvas.CLASS_ROW_HEIGHT;

		layout.getSize().setWidth(width);
		layout.getSize().setHeight(height);

		return new Rectangle2D.Double(layout.getPosition().getX(),
				layout.getPosition().getY(),
				Math.max(width, layout.getSize().getWidth()),
				height);
	}

	/**
	 * Computes the clipboard bounds on the active canvas.
	 *
	 * @param clipboard clipboard value used by the operation
	 * @return the compute clipboard bounds result
	 */
	default Rectangle2D.Double computeClipboardBounds(final ClipboardSnapshot clipboard) {
		Rectangle2D.Double bounds = null;

		for (final CopiedClass copiedClass : clipboard.classes()) {
			final CopiedNodeLayout layout = copiedClass.layout();
			bounds = this.getCanvas().expandBounds(bounds, layout.x(), layout.y(), layout.width(), layout.height());
		}

		for (final CopiedComment copiedComment : clipboard.comments()) {
			final CopiedNodeLayout layout = copiedComment.layout();
			bounds = this.getCanvas().expandBounds(bounds, layout.x(), layout.y(), layout.width(), layout.height());
		}

		if (bounds != null) {
			return bounds;
		}

		for (final CopiedLink copiedLink : clipboard.links()) {
			final CopiedLinkLayout layout = copiedLink.layout();

			for (final Point2D.Double bendPoint : layout.bendPoints()) {
				bounds = this.getCanvas().expandBounds(bounds, bendPoint.getX(), bendPoint.getY(), 1.0, 1.0);
			}

			if (layout.nameLabelPosition() != null) {
				bounds = this.getCanvas()
						.expandBounds(bounds, layout.nameLabelPosition().getX(), layout.nameLabelPosition().getY(), 1.0, 1.0);
			}
		}

		return bounds;
	}

	/**
	 * Computes the comment bounds.
	 *
	 * @param text   text to display or edit
	 * @param layout layout object to read or update
	 * @return the compute comment bounds result
	 */
	default Rectangle2D computeCommentBounds(final String text, final NodeLayout layout) {
		final double width = layout.getSize().getWidth() > 0.0 ? layout.getSize().getWidth() : DiagramCanvas.COMMENT_MIN_WIDTH;
		final List<String> wrappedLines = this.getCanvas()
				.wrapText(text, DiagramCanvas.BODY_FONT, (int) Math.max(40, width - DiagramCanvas.TEXT_PADDING * 2));
		final int contentHeight = (int) (wrappedLines.size() * (this.getCanvas().getHeight(DiagramCanvas.BODY_FONT) + 2)
				+ DiagramCanvas.TEXT_PADDING * 2);

		if (layout.getSize().getWidth() <= 0.0) {
			layout.getSize().setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH, width));
		}
		if (layout.getSize().getHeight() <= 0.0) {
			layout.getSize().setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, contentHeight));
		}

		return new Rectangle2D.Double(layout.getPosition().getX(),
				layout.getPosition().getY(),
				Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE, layout.getSize().getWidth()),
				Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, layout.getSize().getHeight()));
	}

	/**
	 * Expands the bounds on the active canvas.
	 *
	 * @param bounds bounds used for layout or hit testing
	 * @param x      x coordinate
	 * @param y      y coordinate
	 * @param width  width value
	 * @param height height value
	 * @return the expand bounds result
	 */
	default Rectangle2D.Double expandBounds(
			final Rectangle2D.Double bounds,
			final double x,
			final double y,
			final double width,
			final double height) {

		final double safeWidth = Math.max(1.0, width);
		final double safeHeight = Math.max(1.0, height);

		if (bounds == null) {
			return new Rectangle2D.Double(x, y, safeWidth, safeHeight);
		}

		final double minX = Math.min(bounds.getMinX(), x);
		final double minY = Math.min(bounds.getMinY(), y);
		final double maxX = Math.max(bounds.getMaxX(), x + safeWidth);
		final double maxY = Math.max(bounds.getMaxY(), y + safeHeight);

		bounds.setRect(minX, minY, maxX - minX, maxY - minY);
		return bounds;
	}

	/**
	 * Resolves the render layout from the current model and layout state.
	 *
	 * @param layout layout object to read or update
	 * @return the resolved render layout
	 */
	default NodeLayout resolveRenderLayout(final NodeLayout layout) {
		if (layout == null || !this.getCanvas().isDragRenderingActive()) {
			return layout;
		}

		for (final DraggedLayout dragged : this.getCanvas().draggedSelection.layouts()) {
			if (dragged.layout() == layout) {
				final double zoom = this.getCanvas().getPanelState().getZoom();
				final double dx = this.getCanvas().currentDragOffset.getX() / zoom;
				final double dy = this.getCanvas().currentDragOffset.getY() / zoom;

				final NodeLayout copy = new NodeLayout();
				copy.setObjectType(layout.getObjectType());
				copy.setObjectId(layout.getObjectId());
				copy.setPosition(new Point2D.Double(dragged.startX() + dx, dragged.startY() + dy));
				copy.setSize(new Size2D(layout.getSize().getWidth(), layout.getSize().getHeight()));
				return copy;
			}
		}

		return layout;
	}

}
