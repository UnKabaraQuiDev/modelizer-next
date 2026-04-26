package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

final class ConceptualAnchorCache {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	ConceptualAnchorCache(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setConceptualAnchorCache(this);
	}

	Point2D computeConceptualAnchorPoint(final Rectangle2D bounds, final AnchorSide side, final int index, final int totalCount) {
		final double offset = (index - (totalCount - 1) / 2.0) * DiagramCanvas.CONCEPTUAL_ANCHOR_SPACING;
		return switch (side) {
		case TOP -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getY());
		case BOTTOM -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getMaxY());
		case LEFT -> new Point2D.Double(bounds.getX(), bounds.getCenterY() + offset);
		case RIGHT -> new Point2D.Double(bounds.getMaxX(), bounds.getCenterY() + offset);
		};
	}

	Point2D computeConceptualSideCenter(final Rectangle2D bounds, final AnchorSide side) {
		return this.canvas.computeConceptualAnchorPoint(bounds, side, 0, 1);
	}

	void ensureConceptualAnchorCache(final Graphics2D g2) {
		if (this.canvas.panelType != PanelType.CONCEPTUAL || this.canvas.conceptualAnchorCacheValid) {
			return;
		}

		this.canvas.rebuildConceptualAnchorCache(g2);
	}

	int getConceptualSideLinkCount(final String classId, final AnchorSide side) {
		final List<String> links = this.canvas.conceptualSideLinkCache.get(new ClassSideKey(classId, side));
		return links == null ? 0 : links.size();
	}

	void invalidateConceptualAnchorCache() {
		this.canvas.conceptualAnchorCache.clear();
		this.canvas.conceptualAnchorPlacements.clear();
		this.canvas.conceptualSideLinkCache.clear();
		this.canvas.conceptualAnchorCacheValid = false;
	}

	void rebuildConceptualAnchorCache(final Graphics2D g2) {
		this.canvas.invalidateConceptualAnchorCache();

		final Map<String, Rectangle2D> boundsByClassId = new HashMap<>();
		final List<LinkModel> visibleLinks = new ArrayList<>();
		final Map<String, AnchorSidePair> sidePairs = new HashMap<>();

		for (final LinkModel linkModel : this.canvas.getActiveLinks()) {
			final ClassModel fromClass = this.canvas.findClassById(linkModel.getFrom().getClassId());
			final ClassModel toClass = this.canvas.findClassById(linkModel.getTo().getClassId());
			if (fromClass == null || toClass == null || !this.canvas.isVisible(fromClass) || !this.canvas.isVisible(toClass)) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.computeIfAbsent(fromClass.getId(), classId -> {
				final NodeLayout layout = this.canvas
						.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.canvas.computeClassBounds(g2, fromClass, layout);
			});
			final Rectangle2D toBounds = boundsByClassId.computeIfAbsent(toClass.getId(), classId -> {
				final NodeLayout layout = this.canvas
						.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.canvas.computeClassBounds(g2, toClass, layout);
			});

			final AnchorSidePair sidePair;
			if (linkModel.isSelfLinking()) {
				final AnchorSide fromSide = this.canvas.chooseSelfLinkFromSide(fromClass.getId());
				sidePair = new AnchorSidePair(fromSide, this.canvas.clockwise(fromSide));
			} else {
				sidePair = this.canvas.chooseBestConceptualSidePair(fromClass.getId(), fromBounds, toClass.getId(), toBounds);
			}

			sidePairs.put(linkModel.getId(), sidePair);
			this.canvas.conceptualSideLinkCache
					.computeIfAbsent(new ClassSideKey(fromClass.getId(), sidePair.fromSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			this.canvas.conceptualSideLinkCache
					.computeIfAbsent(new ClassSideKey(toClass.getId(), sidePair.toSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			visibleLinks.add(linkModel);
		}

		final Map<ClassSideKey, Map<String, Integer>> indexByKey = new HashMap<>();
		for (final Map.Entry<ClassSideKey, List<String>> entry : this.canvas.conceptualSideLinkCache.entrySet()) {
			final ClassSideKey key = entry.getKey();
			final List<String> linkIds = entry.getValue();
			linkIds.sort(Comparator
					.comparingDouble((final String linkId) -> this.canvas
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
			final List<String> fromLinks = this.canvas.conceptualSideLinkCache.get(fromKey);
			final List<String> toLinks = this.canvas.conceptualSideLinkCache.get(toKey);
			if (fromLinks == null || toLinks == null) {
				continue;
			}

			final int fromIndex = indexByKey.get(fromKey).get(linkModel.getId());
			final int toIndex = indexByKey.get(toKey).get(linkModel.getId());
			final Point2D fromPoint = this.canvas
					.computeConceptualAnchorPoint(fromBounds, sidePair.fromSide(), fromIndex, fromLinks.size());
			final Point2D toPoint = this.canvas.computeConceptualAnchorPoint(toBounds, sidePair.toSide(), toIndex, toLinks.size());

			this.canvas.conceptualAnchorCache.put(linkModel.getId(), new AnchorPair(fromPoint, toPoint));
			this.canvas.conceptualAnchorPlacements.put(linkModel.getId(),
					new LinkAnchorPlacement(sidePair.fromSide(), sidePair.toSide(), fromIndex, fromLinks.size(), toIndex, toLinks.size()));
		}

		this.canvas.conceptualAnchorCacheValid = true;
	}
}
