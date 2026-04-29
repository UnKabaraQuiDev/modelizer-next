package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSidePair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLinkLayout;

/**
 * Contains link layout helpers for bends, labels, and association class placement.
 */
public interface LinkLayoutManager extends DiagramCanvasExt {

	default void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double offset) {

		final LinkLayout linkLayout = getCanvas().findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + offset, bendPoint.getY() + offset));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + offset, copiedLayout.nameLabelPosition().getY() + offset));
		}
	}

	default void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double deltaX, final double deltaY) {

		final LinkLayout linkLayout = getCanvas().findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + deltaX, bendPoint.getY() + deltaY));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + deltaX, copiedLayout.nameLabelPosition().getY() + deltaY));
		}
	}

	default AnchorSidePair chooseBestConceptualSidePair(
			final String fromClassId,
			final Rectangle2D fromBounds,
			final String toClassId,
			final Rectangle2D toBounds) {

		AnchorSidePair bestPair = new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT);
		double bestScore = Double.POSITIVE_INFINITY;

		final List<AnchorSidePair> allowedPairs = List.of(new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT),
				new AnchorSidePair(AnchorSide.RIGHT, AnchorSide.LEFT),
				new AnchorSidePair(AnchorSide.TOP, AnchorSide.BOTTOM),
				new AnchorSidePair(AnchorSide.BOTTOM, AnchorSide.TOP));

		for (final AnchorSidePair pair : allowedPairs) {
			final Point2D fromCenter = getCanvas().computeConceptualSideCenter(fromBounds, pair.fromSide());
			final Point2D toCenter = getCanvas().computeConceptualSideCenter(toBounds, pair.toSide());

			final double distance = fromCenter.distance(toCenter);
			final double loadPenalty = (getCanvas().getConceptualSideLinkCount(fromClassId, pair.fromSide())
					+ getCanvas().getConceptualSideLinkCount(toClassId, pair.toSide())) * 12.0;

			final double score = distance + loadPenalty;

			if (score < bestScore) {
				bestScore = score;
				bestPair = pair;
			}
		}

		return bestPair;
	}

	default AnchorSide chooseSelfLinkFromSide(final String classId) {
		AnchorSide bestSide = AnchorSide.TOP;
		int bestCount = Integer.MAX_VALUE;

		for (final AnchorSide side : AnchorSide.values()) {
			final int sideCount = getCanvas().getConceptualSideLinkCount(classId, side);
			if (sideCount < bestCount) {
				bestCount = sideCount;
				bestSide = side;
			}
		}

		return bestSide;
	}

	default AnchorSide chooseTechnicalSelfLinkSide(final Graphics2D g2, final LinkModel linkModel) {
		final String classId = linkModel.getFrom().getClassId();
		final int leftCount = getCanvas().getTechnicalSideLinkCount(g2, classId, AnchorSide.LEFT, linkModel.getId());
		final int rightCount = getCanvas().getTechnicalSideLinkCount(g2, classId, AnchorSide.RIGHT, linkModel.getId());
		return leftCount <= rightCount ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	default double computeConceptualSortValue(
			final String linkId,
			final String classId,
			final AnchorSide side,
			final Map<String, Rectangle2D> boundsByClassId,
			final Map<String, AnchorSidePair> sidePairs) {
		final LinkModel linkModel = getCanvas().findLinkById(linkId);
		if (linkModel == null) {
			return 0.0;
		}

		final AnchorSidePair sidePair = sidePairs.get(linkId);
		if (sidePair == null) {
			return 0.0;
		}

		final boolean fromEndpoint = classId.equals(linkModel.getFrom().getClassId()) && side == sidePair.fromSide();
		final boolean toEndpoint = classId.equals(linkModel.getTo().getClassId()) && side == sidePair.toSide();
		if (!fromEndpoint && !toEndpoint) {
			return 0.0;
		}

		if (linkModel.isSelfLinking()) {
			final Rectangle2D bounds = boundsByClassId.get(classId);
			if (bounds == null) {
				return 0.0;
			}

			final AnchorSide oppositeSide = fromEndpoint ? sidePair.toSide() : sidePair.fromSide();
			final Point2D oppositePoint = getCanvas().computeConceptualSideCenter(bounds, oppositeSide);
			return switch (side) {
			case TOP, BOTTOM -> oppositePoint.getX();
			case LEFT, RIGHT -> oppositePoint.getY();
			};
		}

		final String otherClassId = fromEndpoint ? linkModel.getTo().getClassId() : linkModel.getFrom().getClassId();
		final Rectangle2D otherBounds = boundsByClassId.get(otherClassId);
		if (otherBounds == null) {
			return 0.0;
		}

		return switch (side) {
		case TOP, BOTTOM -> otherBounds.getCenterX();
		case LEFT, RIGHT -> otherBounds.getCenterY();
		};
	}

	default AnchorPair resolveConceptualAnchorPair(final Graphics2D g2, final LinkModel targetLink) {
		getCanvas().ensureConceptualAnchorCache(g2);
		return getCanvas().conceptualAnchorCache.get(targetLink.getId());
	}

}
