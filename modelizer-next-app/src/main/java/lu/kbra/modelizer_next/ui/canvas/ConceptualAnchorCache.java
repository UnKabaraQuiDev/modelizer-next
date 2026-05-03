package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
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
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSidePair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClassSideKey;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;

/**
 * Contains cache and placement logic for conceptual link anchors.
 */
interface ConceptualAnchorCache extends DiagramCanvasExt {

	default Point2D computeConceptualAnchorPoint(final Rectangle2D bounds, final AnchorSide side, final int index, final int totalCount) {
		final double offset = (index - (totalCount - 1) / 2.0) * DiagramCanvas.CONCEPTUAL_ANCHOR_SPACING;
		return switch (side) {
		case TOP -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getY());
		case BOTTOM -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getMaxY());
		case LEFT -> new Point2D.Double(bounds.getX(), bounds.getCenterY() + offset);
		case RIGHT -> new Point2D.Double(bounds.getMaxX(), bounds.getCenterY() + offset);
		};
	}

	default Point2D computeConceptualSideCenter(final Rectangle2D bounds, final AnchorSide side) {
		return this.getCanvas().computeConceptualAnchorPoint(bounds, side, 0, 1);
	}

	default void ensureConceptualAnchorCache(final Graphics2D g2) {
		if (this.getPanelType() != PanelType.CONCEPTUAL || this.getCanvas().conceptualAnchorCacheValid) {
			return;
		}

		this.getCanvas().rebuildConceptualAnchorCache(g2);
	}

	default int getConceptualSideLinkCount(final String classId, final AnchorSide side) {
		final List<String> links = this.getCanvas().conceptualSideLinkCache.get(new ClassSideKey(classId, side));
		return links == null ? 0 : links.size();
	}

	default void invalidateConceptualAnchorCache() {
		this.getCanvas().conceptualAnchorCache.clear();
		this.getCanvas().conceptualAnchorPlacements.clear();
		this.getCanvas().conceptualSideLinkCache.clear();
		this.getCanvas().conceptualAnchorCacheValid = false;
	}

	default void rebuildConceptualAnchorCache(final Graphics2D g2) {
		this.getCanvas().invalidateConceptualAnchorCache();

		final Map<String, Rectangle2D> boundsByClassId = new HashMap<>();
		final List<LinkModel> visibleLinks = new ArrayList<>();
		final Map<String, AnchorSidePair> sidePairs = new HashMap<>();

		for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
			final ClassModel fromClass = this.getCanvas().findClassById(linkModel.getFrom().getClassId());
			final ClassModel toClass = this.getCanvas().findClassById(linkModel.getTo().getClassId());
			if (fromClass == null || toClass == null || !fromClass.isVisible(this.getPanelType())
					|| !toClass.isVisible(this.getPanelType())) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.computeIfAbsent(fromClass.getId(), classId -> {
				final NodeLayout layout = this.getCanvas()
						.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.getCanvas().computeClassBounds(g2, fromClass, layout);
			});
			final Rectangle2D toBounds = boundsByClassId.computeIfAbsent(toClass.getId(), classId -> {
				final NodeLayout layout = this.getCanvas()
						.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.getCanvas().computeClassBounds(g2, toClass, layout);
			});

			final AnchorSidePair sidePair;
			if (linkModel.isSelfLinking()) {
				final AnchorSide fromSide = this.getCanvas().chooseSelfLinkFromSide(fromClass.getId());
				sidePair = new AnchorSidePair(fromSide, this.getCanvas().clockwise(fromSide));
			} else {
				sidePair = this.getCanvas().chooseBestConceptualSidePair(fromClass.getId(), fromBounds, toClass.getId(), toBounds);
			}

			sidePairs.put(linkModel.getId(), sidePair);
			this.getCanvas().conceptualSideLinkCache
					.computeIfAbsent(new ClassSideKey(fromClass.getId(), sidePair.fromSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			this.getCanvas().conceptualSideLinkCache
					.computeIfAbsent(new ClassSideKey(toClass.getId(), sidePair.toSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			visibleLinks.add(linkModel);
		}

		final Map<ClassSideKey, Map<String, Integer>> indexByKey = new HashMap<>();
		for (final Map.Entry<ClassSideKey, List<String>> entry : this.getCanvas().conceptualSideLinkCache.entrySet()) {
			final ClassSideKey key = entry.getKey();
			final List<String> linkIds = entry.getValue();
			linkIds.sort(Comparator
					.comparingDouble((final String linkId) -> this.getCanvas()
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
			final List<String> fromLinks = this.getCanvas().conceptualSideLinkCache.get(fromKey);
			final List<String> toLinks = this.getCanvas().conceptualSideLinkCache.get(toKey);
			if (fromLinks == null || toLinks == null) {
				continue;
			}

			final int fromIndex = indexByKey.get(fromKey).get(linkModel.getId());
			final int toIndex = indexByKey.get(toKey).get(linkModel.getId());
			final Point2D fromPoint = this.getCanvas()
					.computeConceptualAnchorPoint(fromBounds, sidePair.fromSide(), fromIndex, fromLinks.size());
			final Point2D toPoint = this.getCanvas().computeConceptualAnchorPoint(toBounds, sidePair.toSide(), toIndex, toLinks.size());

			this.getCanvas().conceptualAnchorCache.put(linkModel.getId(), new AnchorPair(fromPoint, toPoint));
			this.getCanvas().conceptualAnchorPlacements.put(linkModel.getId(),
					new LinkAnchorPlacement(sidePair.fromSide(), sidePair.toSide(), fromIndex, fromLinks.size(), toIndex, toLinks.size()));
		}

		this.getCanvas().conceptualAnchorCacheValid = true;
	}

}
