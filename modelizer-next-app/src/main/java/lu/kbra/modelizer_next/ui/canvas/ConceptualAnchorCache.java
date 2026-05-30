package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSidePair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClassSideKey;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;

/**
 * Contains cache and placement logic for conceptual link anchors.
 */
interface ConceptualAnchorCache extends DiagramCanvasExt {

	/**
	 * Computes the conceptual anchor point on the active canvas.
	 *
	 * @param bounds     bounds used for layout or hit testing
	 * @param side       node side to inspect
	 * @param index      zero-based index to read or update
	 * @param big        whether big is enabled
	 * @param totalCount count value to use
	 * @return the compute conceptual anchor point result
	 */
	default Point2D computeConceptualAnchorPoint(
			final Rectangle2D bounds,
			final AnchorSide side,
			final int index,
			final boolean big,
			final int totalCount) {
		final double offset = (index - (totalCount - 1) / 2.0) * (big ? 2 : 1) * DiagramCanvas.CONCEPTUAL_ANCHOR_SPACING;
		return switch (side) {
		case TOP -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getY());
		case BOTTOM -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getMaxY());
		case LEFT -> new Point2D.Double(bounds.getX(), bounds.getCenterY() + offset);
		case RIGHT -> new Point2D.Double(bounds.getMaxX(), bounds.getCenterY() + offset);
		};
	}

	/**
	 * Computes the conceptual side center on the active canvas.
	 *
	 * @param bounds bounds used for layout or hit testing
	 * @param side   node side to inspect
	 * @param big    whether big is enabled
	 * @return the compute conceptual side center result
	 */
	default Point2D computeConceptualSideCenter(final Rectangle2D bounds, final AnchorSide side, final boolean big) {
		return this.getCanvas().computeConceptualAnchorPoint(bounds, side, 0, big, 1);
	}

	/**
	 * Ensures that the conceptual anchor cache exists or is up to date on the active canvas.
	 */
	default void ensureConceptualAnchorCache() {
		if (this.getPanelType() != PanelType.CONCEPTUAL || this.getCanvas().conceptualAnchorCacheValid) {
			return;
		}

		this.getCanvas().rebuildConceptualAnchorCache();
	}

	/**
	 * Returns the conceptual side link count.
	 *
	 * @param classId id of the class to look up or modify
	 * @param side    node side to inspect
	 * @return the conceptual side link count
	 */
	default int getConceptualSideLinkCount(final String classId, final AnchorSide side) {
		final List<String> links = this.getCanvas().conceptualSideLinkCache.get(new ClassSideKey(classId, side));
		return links == null ? 0 : links.size();
	}

	/**
	 * Invalidates the conceptual anchor cache so it will be recalculated on the active canvas.
	 */
	default void invalidateConceptualAnchorCache() {
		this.getCanvas().conceptualAnchorCache.clear();
		this.getCanvas().conceptualAnchorPlacements.clear();
		this.getCanvas().conceptualSideLinkCache.clear();
		this.getCanvas().conceptualAnchorCacheValid = false;
	}

	/**
	 * Rebuilds the conceptual anchor cache on the active canvas.
	 */
	default void rebuildConceptualAnchorCache() {
		final DiagramCanvas canvas = this.getCanvas();

		canvas.invalidateConceptualAnchorCache();

		final Map<String, Rectangle2D> boundsByClassId = new HashMap<>();
		final List<LinkModel> visibleLinks = new ArrayList<>();
		final Map<String, AnchorSidePair> sidePairs = new HashMap<>();

		for (final LinkModel linkModel : canvas.getActiveLinks()) {
			final ClassModel fromClass = canvas.findClassById(linkModel.getFrom().getClassId());
			final ClassModel toClass = canvas.findClassById(linkModel.getTo().getClassId());
			if (fromClass == null || toClass == null || !fromClass.isVisible(this.getPanelType())
					|| !toClass.isVisible(this.getPanelType())) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.computeIfAbsent(fromClass.getId(), classId -> {
				final NodeLayout layout = canvas.resolveRenderLayout(canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return canvas.computeClassBounds(fromClass, layout);
			});
			final Rectangle2D toBounds = boundsByClassId.computeIfAbsent(toClass.getId(), classId -> {
				final NodeLayout layout = canvas.resolveRenderLayout(canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return canvas.computeClassBounds(toClass, layout);
			});

			final AnchorSidePair sidePair;
			if (linkModel.isSelfLinking()) {
				final AnchorSide fromSide = canvas.chooseSelfLinkFromSide(fromClass.getId());
				sidePair = new AnchorSidePair(fromSide, fromSide.clockwise());
			} else {
				sidePair = canvas
						.chooseBestConceptualSidePair(fromClass.getId(), fromBounds, toClass.getId(), toBounds, linkModel.hasTargetLabel());
			}

			sidePairs.put(linkModel.getId(), sidePair);
			canvas.conceptualSideLinkCache
					.computeIfAbsent(new ClassSideKey(fromClass.getId(), sidePair.fromSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			canvas.conceptualSideLinkCache.computeIfAbsent(new ClassSideKey(toClass.getId(), sidePair.toSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			visibleLinks.add(linkModel);
		}

		final Map<ClassSideKey, Map<String, Integer>> indexByKey = new HashMap<>();
		for (final Map.Entry<ClassSideKey, List<String>> entry : canvas.conceptualSideLinkCache.entrySet()) {
			final ClassSideKey key = entry.getKey();
			final List<String> linkIds = entry.getValue();
			linkIds.sort(Comparator
					.comparingDouble((final String linkId) -> canvas
							.computeConceptualSortValue(linkId, key.classId(), key.side(), boundsByClassId, sidePairs))
					.thenComparing(linkId -> linkId));

			final Map<String, Integer> indices = new HashMap<>();
			for (int i = 0; i < linkIds.size(); i++) {
				indices.put(linkIds.get(i), i);
			}
			indexByKey.put(key, indices);
		}

		for (final LinkModel linkModel : visibleLinks) {
			final AnchorSidePair sidePair = sidePairs.get(linkModel.getId());
			if (sidePair == null) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.get(linkModel.getFrom().getClassId());
			final Rectangle2D toBounds = boundsByClassId.get(linkModel.getTo().getClassId());
			if (fromBounds == null || toBounds == null) {
				continue;
			}

			final ClassSideKey fromKey = new ClassSideKey(linkModel.getFrom().getClassId(), sidePair.fromSide());
			final ClassSideKey toKey = new ClassSideKey(linkModel.getTo().getClassId(), sidePair.toSide());
			final List<String> fromLinks = canvas.conceptualSideLinkCache.get(fromKey);
			final List<String> toLinks = canvas.conceptualSideLinkCache.get(toKey);
			if (fromLinks == null || toLinks == null) {
				continue;
			}

			final int fromIndex = indexByKey.get(fromKey).get(linkModel.getId());
			final int toIndex = indexByKey.get(toKey).get(linkModel.getId());
			final Point2D fromPoint = canvas
					.computeConceptualAnchorPoint(fromBounds, sidePair.fromSide(), fromIndex, linkModel.hasTargetLabel(), fromLinks.size());
			final Point2D toPoint = canvas
					.computeConceptualAnchorPoint(toBounds, sidePair.toSide(), toIndex, linkModel.hasTargetLabel(), toLinks.size());

			canvas.conceptualAnchorCache.put(linkModel.getId(), new AnchorPair(fromPoint, toPoint, fromKey.side(), toKey.side()));
			canvas.conceptualAnchorPlacements.put(linkModel.getId(),
					new LinkAnchorPlacement(sidePair.fromSide(), sidePair.toSide(), fromIndex, fromLinks.size(), toIndex, toLinks.size()));
		}

		canvas.conceptualAnchorCacheValid = true;
	}

}
