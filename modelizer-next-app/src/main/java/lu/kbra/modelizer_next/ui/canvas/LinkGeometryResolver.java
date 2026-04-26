package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

final class LinkGeometryResolver {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	LinkGeometryResolver(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setLinkGeometryResolver(this);
	}

	Point2D computePolylineMiddlePoint(final List<Point2D> points) {
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

	double computeUprightAngleAtMiddle(final List<Point2D> points) {
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

	Point2D resolveClassCenterAnchor(final Graphics2D g2, final String classId) {
		final ClassModel classModel = this.canvas.findClassById(classId);
		if (classModel == null || !this.canvas.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.canvas
				.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Rectangle2D bounds = this.canvas.computeClassBounds(g2, classModel, layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	Point2D resolveCommentCenterAnchor(final Graphics2D g2, final String commentId) {
		final CommentModel commentModel = this.canvas.findCommentById(commentId);
		if (commentModel == null || !this.canvas.isCommentVisible(commentModel)) {
			return null;
		}

		final NodeLayout layout = this.canvas
				.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Rectangle2D bounds = this.canvas.computeCommentBounds(g2, this.canvas.resolveCommentText(commentModel), layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	LinkGeometry resolveLinkGeometry(final Graphics2D g2, final LinkModel linkModel) {
		final Point2D fromPoint;
		final Point2D toPoint;

		if (this.canvas.panelType == PanelType.CONCEPTUAL) {
			final AnchorPair anchorPair = this.canvas.resolveConceptualAnchorPair(g2, linkModel);
			if (anchorPair == null) {
				return null;
			}
			fromPoint = anchorPair.from();
			toPoint = anchorPair.to();
		} else if (this.canvas.isSelfLink(linkModel)) {
			final AnchorSide selfLinkSide = this.canvas.chooseTechnicalSelfLinkSide(g2, linkModel);
			fromPoint = this.canvas
					.resolveTechnicalSelfLinkAnchor(g2, linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId(), selfLinkSide);
			toPoint = this.canvas
					.resolveTechnicalSelfLinkAnchor(g2, linkModel.getTo().getClassId(), linkModel.getTo().getFieldId(), selfLinkSide);
		} else {
			fromPoint = this.canvas.resolveTechnicalFieldAnchor(g2,
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId(),
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId());
			toPoint = this.canvas.resolveTechnicalFieldAnchor(g2,
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId(),
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId());
		}
		if (fromPoint == null || toPoint == null) {
			return null;
		}

		final List<Point2D> points;
		if (this.canvas.isSelfLink(linkModel)) {
			points = this.canvas.buildSelfLinkPoints(g2, linkModel, fromPoint, toPoint);
		} else {
			points = new ArrayList<>();
			points.add(fromPoint);

			final LinkLayout linkLayout = this.canvas.findOrCreateLinkLayout(linkModel.getId());
			for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
				points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
			}

			points.add(toPoint);
		}

		final Point2D middlePoint = this.canvas.computePolylineMiddlePoint(points);
		final double labelAngle = this.canvas.computeUprightAngleAtMiddle(points);

		final Point2D labelPoint;
		final LinkLayout linkLayout = this.canvas.findOrCreateLinkLayout(linkModel.getId());
		if (linkLayout.getNameLabelPosition() != null) {
			labelPoint = new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());
		} else {
			labelPoint = middlePoint;
		}

		return new LinkGeometry(fromPoint, toPoint, labelPoint, middlePoint, labelAngle, points);
	}

	Point2D resolveLinkMiddleAnchor(final Graphics2D g2, final String linkId) {
		final LinkModel linkModel = this.canvas.findLinkById(linkId);
		final LinkGeometry geometry = linkModel == null ? null : this.canvas.resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.middlePoint();
	}

	Point2D resolvePreviewSourceAnchor(final Graphics2D g2) {
		if (this.canvas.linkCreationState == null) {
			return null;
		}

		final SelectedElement source = this.canvas.getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return this.canvas.resolveCommentCenterAnchor(g2, source.commentId());
		}

		if (this.canvas.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.canvas.linkPreviewTarget != null
					? this.canvas.resolvePreviewTargetAnchor(g2, this.canvas.linkPreviewTarget)
					: this.canvas.linkPreviewMousePoint;
			return this.canvas.resolveConceptualPreviewAnchor(g2, source.classId(), reference);
		}

		final Point2D reference = this.canvas.linkPreviewTarget != null
				? this.canvas.resolvePreviewTargetAnchor(g2, this.canvas.linkPreviewTarget)
				: this.canvas.linkPreviewMousePoint;

		final String oppositeClassId = this.canvas.linkPreviewTarget == null ? null : this.canvas.linkPreviewTarget.classId();
		final String oppositeFieldId = this.canvas.linkPreviewTarget == null ? null : this.canvas.linkPreviewTarget.fieldId();

		if (oppositeClassId != null) {
			return this.canvas.resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), oppositeClassId, oppositeFieldId);
		}

		return this.canvas.resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), reference);
	}

	Point2D resolvePreviewTargetAnchor(final Graphics2D g2, final SelectedElement target) {
		if (target == null) {
			return null;
		}

		final SelectedElement source = this.canvas.getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return switch (target.type()) {
			case CLASS -> this.canvas.resolveClassCenterAnchor(g2, target.classId());
			case LINK -> this.canvas.resolveLinkMiddleAnchor(g2, target.linkId());
			default -> null;
			};
		}

		if (target.type() == SelectedType.LINK) {
			return this.canvas.resolveLinkMiddleAnchor(g2, target.linkId());
		}

		if (this.canvas.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.canvas.resolvePreviewSourceAnchorReference(g2);
			return this.canvas.resolveConceptualPreviewAnchor(g2, target.classId(), reference);
		}

		return this.canvas.resolveTechnicalFieldAnchor(g2, target.classId(), target.fieldId(), source.classId(), source.fieldId());
	}
}
