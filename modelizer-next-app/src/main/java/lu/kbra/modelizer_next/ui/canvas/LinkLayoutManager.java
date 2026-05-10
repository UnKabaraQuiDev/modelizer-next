package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSidePair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLinkLayout;

/**
 * Contains link layout helpers for bends, labels, and association class placement.
 */
public interface LinkLayoutManager extends DiagramCanvasExt {

	/**
	 * Applies the link layout.
	 * @param linkId id of the link to look up or modify
	 * @param copiedLayout layout object to read or modify
	 * @param offset numeric offset value
	 */
	default void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double offset) {

		final LinkLayout linkLayout = this.getCanvas().findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + offset, bendPoint.getY() + offset));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + offset, copiedLayout.nameLabelPosition().getY() + offset));
		}
	}

	/**
	 * Applies the link layout.
	 * @param linkId id of the link to look up or modify
	 * @param copiedLayout layout object to read or modify
	 * @param deltaX numeric delta x value
	 * @param deltaY numeric delta y value
	 */
	default void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double deltaX, final double deltaY) {

		final LinkLayout linkLayout = this.getCanvas().findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + deltaX, bendPoint.getY() + deltaY));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + deltaX, copiedLayout.nameLabelPosition().getY() + deltaY));
		}
	}

	/**
	 * Chooses the best conceptual side pair on the active canvas.
	 * @param fromClassId id of the element to read or modify
	 * @param fromBounds bounds used for layout or hit testing
	 * @param toClassId id of the element to read or modify
	 * @param toBounds bounds used for layout or hit testing
	 * @param big whether big is enabled
	 * @return the choose best conceptual side pair result
	 */
	default AnchorSidePair chooseBestConceptualSidePair(
			final String fromClassId,
			final Rectangle2D fromBounds,
			final String toClassId,
			final Rectangle2D toBounds,
			final boolean big) {

		AnchorSidePair bestPair = new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT);
		double bestScore = Double.POSITIVE_INFINITY;

		final List<AnchorSidePair> allowedPairs = List.of(new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT),
				new AnchorSidePair(AnchorSide.RIGHT, AnchorSide.LEFT),
				new AnchorSidePair(AnchorSide.TOP, AnchorSide.BOTTOM),
				new AnchorSidePair(AnchorSide.BOTTOM, AnchorSide.TOP));

		for (final AnchorSidePair pair : allowedPairs) {
			final Point2D fromCenter = this.getCanvas().computeConceptualSideCenter(fromBounds, pair.fromSide(), big);
			final Point2D toCenter = this.getCanvas().computeConceptualSideCenter(toBounds, pair.toSide(), big);

			final double distance = fromCenter.distance(toCenter);
			final double loadPenalty = (this.getCanvas().getConceptualSideLinkCount(fromClassId, pair.fromSide())
					+ this.getCanvas().getConceptualSideLinkCount(toClassId, pair.toSide())) * 12.0;

			final double score = distance + loadPenalty;

			if (score < bestScore) {
				bestScore = score;
				bestPair = pair;
			}
		}

		return bestPair;
	}

	/**
	 * Chooses the self link from side.
	 * @param classId id of the class to look up or modify
	 * @return the choose self link from side result
	 */
	default AnchorSide chooseSelfLinkFromSide(final String classId) {
		AnchorSide bestSide = AnchorSide.TOP;
		int bestCount = Integer.MAX_VALUE;

		for (final AnchorSide side : AnchorSide.values()) {
			final int sideCount = this.getCanvas().getConceptualSideLinkCount(classId, side);
			if (sideCount < bestCount) {
				bestCount = sideCount;
				bestSide = side;
			}
		}

		return bestSide;
	}

	/**
	 * Chooses the technical self link side.
	 * @param linkModel link model affected by the operation
	 * @return the choose technical self link side result
	 */
	default AnchorSide chooseTechnicalSelfLinkSide(final LinkModel linkModel) {
		final String classId = linkModel.getFrom().getClassId();
		final int leftCount = this.getCanvas().getTechnicalSideLinkCount(classId, AnchorSide.LEFT, linkModel.getId());
		final int rightCount = this.getCanvas().getTechnicalSideLinkCount(classId, AnchorSide.RIGHT, linkModel.getId());
		return leftCount <= rightCount ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	/**
	 * Computes the conceptual sort value on the active canvas.
	 * @param linkId id of the link to look up or modify
	 * @param classId id of the class to look up or modify
	 * @param side node side to inspect
	 * @param boundsByClassId id of the element to read or modify
	 * @param sidePairs side pairs value used by the operation
	 * @return the compute conceptual sort value result
	 */
	default double computeConceptualSortValue(
			final String linkId,
			final String classId,
			final AnchorSide side,
			final Map<String, Rectangle2D> boundsByClassId,
			final Map<String, AnchorSidePair> sidePairs) {
		final LinkModel linkModel = this.getCanvas().findLinkById(linkId);
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
			final Point2D oppositePoint = this.getCanvas().computeConceptualSideCenter(bounds, oppositeSide, linkModel.hasTargetLabel());
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

	/**
	 * Resolves the conceptual anchor pair from the current model and layout state.
	 * @param targetLink target link value used by the operation
	 * @return the resolved conceptual anchor pair
	 */
	default AnchorPair resolveConceptualAnchorPair(final LinkModel targetLink) {
		this.getCanvas().ensureConceptualAnchorCache();
		return this.getCanvas().conceptualAnchorCache.get(targetLink.getId());
	}

}
