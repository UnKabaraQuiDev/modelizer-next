package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

/**
 * Contains anchor and geometry resolution for links and link labels.
 */
interface LinkGeometryResolver extends DiagramCanvasExt {

	default Point2D computePolylineMiddlePoint(final List<Point2D> points) {
		if (points == null || points.size() < 2) {
			return null;
		}

		double totalLength = 0.0;
		for (int i = 0; i < points.size() - 1; i++) {
			totalLength += points.get(i).distance(points.get(i + 1));
		}

		if (totalLength <= 0.0) {
			return new Point2D.Double(points.get(0).getX(), points.get(0).getY());
		}

		final double halfLength = totalLength / 2.0;
		double walked = 0.0;

		for (int i = 0; i < points.size() - 1; i++) {
			final Point2D a = points.get(i);
			final Point2D b = points.get(i + 1);
			final double segmentLength = a.distance(b);

			if (walked + segmentLength >= halfLength) {
				final double remaining = halfLength - walked;
				final double t = segmentLength == 0.0 ? 0.0 : remaining / segmentLength;
				return new Point2D.Double(a.getX() + (b.getX() - a.getX()) * t, a.getY() + (b.getY() - a.getY()) * t);
			}

			walked += segmentLength;
		}

		final Point2D last = points.get(points.size() - 1);
		return new Point2D.Double(last.getX(), last.getY());
	}

	default double computeUprightAngleAtMiddle(final List<Point2D> points) {
		if (points == null || points.size() < 2) {
			return 0.0;
		}

		double totalLength = 0.0;
		for (int i = 0; i < points.size() - 1; i++) {
			totalLength += points.get(i).distance(points.get(i + 1));
		}

		if (totalLength <= 0.0) {
			return 0.0;
		}

		final double halfLength = totalLength / 2.0;
		double walked = 0.0;

		for (int i = 0; i < points.size() - 1; i++) {
			final Point2D a = points.get(i);
			final Point2D b = points.get(i + 1);
			final double segmentLength = a.distance(b);

			if (walked + segmentLength >= halfLength) {
				double angle = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

				if (angle > Math.PI / 2.0) {
					angle -= Math.PI;
				} else if (angle <= -Math.PI / 2.0) {
					angle += Math.PI;
				}

				return angle;
			}

			walked += segmentLength;
		}

		final Point2D a = points.get(points.size() - 2);
		final Point2D b = points.get(points.size() - 1);
		double angle = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

		if (angle > Math.PI / 2.0) {
			angle -= Math.PI;
		} else if (angle <= -Math.PI / 2.0) {
			angle += Math.PI;
		}

		return angle;
	}

	default Point2D resolveClassCenterAnchor(final Graphics2D g2, final String classId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas()
				.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	default Point2D resolveCommentCenterAnchor(final Graphics2D g2, final String commentId) {
		final CommentModel commentModel = getCanvas().findCommentById(commentId);
		if (commentModel == null || !getCanvas().isCommentVisible(commentModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas()
				.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Rectangle2D bounds = getCanvas().computeCommentBounds(g2, getCanvas().resolveCommentText(commentModel), layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	default LinkGeometry resolveLinkGeometry(final Graphics2D g2, final LinkModel linkModel) {
		final Point2D fromPoint;
		final Point2D toPoint;

		if (getPanelType() == PanelType.CONCEPTUAL) {
			final AnchorPair anchorPair = getCanvas().resolveConceptualAnchorPair(g2, linkModel);
			if (anchorPair == null) {
				return null;
			}
			fromPoint = anchorPair.from();
			toPoint = anchorPair.to();
		} else if (linkModel.isSelfLinking()) {
			final AnchorSide selfLinkSide = getCanvas().chooseTechnicalSelfLinkSide(g2, linkModel);
			fromPoint = getCanvas()
					.resolveTechnicalSelfLinkAnchor(g2, linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId(), selfLinkSide);
			toPoint = getCanvas()
					.resolveTechnicalSelfLinkAnchor(g2, linkModel.getTo().getClassId(), linkModel.getTo().getFieldId(), selfLinkSide);
		} else {
			fromPoint = getCanvas().resolveTechnicalFieldAnchor(g2,
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId(),
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId());
			toPoint = getCanvas().resolveTechnicalFieldAnchor(g2,
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId(),
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId());
		}
		if (fromPoint == null || toPoint == null) {
			return null;
		}

		final List<Point2D> points;
		if (linkModel.isSelfLinking()) {
			points = getCanvas().buildSelfLinkPoints(g2, linkModel, fromPoint, toPoint);
		} else {
			points = new ArrayList<>();
			points.add(fromPoint);

			final LinkLayout linkLayout = getCanvas().findOrCreateLinkLayout(linkModel.getId());
			for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
				points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
			}

			points.add(toPoint);
		}

		final Point2D middlePoint = getCanvas().computePolylineMiddlePoint(points);
		final double labelAngle = getCanvas().computeUprightAngleAtMiddle(points);

		final Point2D labelPoint;
		final LinkLayout linkLayout = getCanvas().findOrCreateLinkLayout(linkModel.getId());
		if (linkLayout.getNameLabelPosition() != null) {
			labelPoint = new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());
		} else {
			labelPoint = middlePoint;
		}

		return new LinkGeometry(fromPoint, toPoint, labelPoint, middlePoint, labelAngle, points);
	}

	default Point2D resolveLinkMiddleAnchor(final Graphics2D g2, final String linkId) {
		final LinkModel linkModel = getCanvas().findLinkById(linkId);
		final LinkGeometry geometry = linkModel == null ? null : getCanvas().resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.middlePoint();
	}

	default Point2D resolvePreviewSourceAnchor(final Graphics2D g2) {
		if (getCanvas().linkCreationState == null) {
			return null;
		}

		final SelectedElement source = getCanvas().getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return getCanvas().resolveCommentCenterAnchor(g2, source.commentId());
		}

		if (getPanelType() == PanelType.CONCEPTUAL) {
			final Point2D reference = getCanvas().linkPreviewTarget != null
					? getCanvas().resolvePreviewTargetAnchor(g2, getCanvas().linkPreviewTarget)
					: getCanvas().linkPreviewMousePoint;
			return getCanvas().resolveConceptualPreviewAnchor(g2, source.classId(), reference);
		}

		final Point2D reference = getCanvas().linkPreviewTarget != null
				? getCanvas().resolvePreviewTargetAnchor(g2, getCanvas().linkPreviewTarget)
				: getCanvas().linkPreviewMousePoint;

		final String oppositeClassId = getCanvas().linkPreviewTarget == null ? null : getCanvas().linkPreviewTarget.classId();
		final String oppositeFieldId = getCanvas().linkPreviewTarget == null ? null : getCanvas().linkPreviewTarget.fieldId();

		if (oppositeClassId != null) {
			return getCanvas().resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), oppositeClassId, oppositeFieldId);
		}

		return getCanvas().resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), reference);
	}

	default Point2D resolvePreviewTargetAnchor(final Graphics2D g2, final SelectedElement target) {
		if (target == null) {
			return null;
		}

		final SelectedElement source = getCanvas().getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return switch (target.type()) {
			case CLASS -> getCanvas().resolveClassCenterAnchor(g2, target.classId());
			case LINK -> getCanvas().resolveLinkMiddleAnchor(g2, target.linkId());
			default -> null;
			};
		}

		if (target.type() == SelectedType.LINK) {
			return getCanvas().resolveLinkMiddleAnchor(g2, target.linkId());
		}

		if (getPanelType() == PanelType.CONCEPTUAL) {
			final Point2D reference = getCanvas().resolvePreviewSourceAnchorReference(g2);
			return getCanvas().resolveConceptualPreviewAnchor(g2, target.classId(), reference);
		}

		return getCanvas().resolveTechnicalFieldAnchor(g2, target.classId(), target.fieldId(), source.classId(), source.fieldId());
	}

	default List<Point2D> buildSelfLinkPoints(
			final Graphics2D g2,
			final LinkModel linkModel,
			final Point2D fromPoint,
			final Point2D toPoint) {
		final List<Point2D> points = new ArrayList<>();
		points.add(fromPoint);

		final ClassModel classModel = getCanvas().findClassById(linkModel.getFrom().getClassId());
		if (classModel == null) {
			points.add(toPoint);
			return points;
		}

		if (getPanelType() != PanelType.CONCEPTUAL) {
			final NodeLayout layout = getCanvas()
					.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);
			final AnchorSide side = getCanvas().chooseTechnicalSelfLinkSide(g2, linkModel);
			final int sideLoad = getCanvas().getTechnicalSideLinkCount(g2, classModel.getId(), side, linkModel.getId());
			final double outsideOffset = DiagramCanvas.SELF_LINK_OUTSIDE_OFFSET + sideLoad * 12.0;
			final double outsideX = side == AnchorSide.LEFT ? bounds.getX() - outsideOffset : bounds.getMaxX() + outsideOffset;

			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
			points.add(toPoint);
			return points;
		}

		final LinkAnchorPlacement placement = getCanvas().conceptualAnchorPlacements.get(linkModel.getId());
		if (placement == null) {
			points.add(toPoint);
			return points;
		}

		final NodeLayout layout = getCanvas()
				.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);
		final double outsideOffset = DiagramCanvas.SELF_LINK_OUTSIDE_OFFSET + Math.max(placement.fromCount(), placement.toCount()) * 4.0;

		switch (placement.fromSide()) {
		case TOP -> {
			final double outsideY = bounds.getY() - outsideOffset;
			final double outsideX = bounds.getMaxX() + outsideOffset;
			points.add(new Point2D.Double(fromPoint.getX(), outsideY));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
		}
		case RIGHT -> {
			final double outsideX = bounds.getMaxX() + outsideOffset;
			final double outsideY = bounds.getMaxY() + outsideOffset;
			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(toPoint.getX(), outsideY));
		}
		case BOTTOM -> {
			final double outsideY = bounds.getMaxY() + outsideOffset;
			final double outsideX = bounds.getX() - outsideOffset;
			points.add(new Point2D.Double(fromPoint.getX(), outsideY));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
		}
		case LEFT -> {
			final double outsideX = bounds.getX() - outsideOffset;
			final double outsideY = bounds.getY() - outsideOffset;
			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(toPoint.getX(), outsideY));
		}
		}

		points.add(toPoint);
		return points;
	}

}
