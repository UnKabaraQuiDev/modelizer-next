package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedSelection;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;

interface DragSelectionController extends DiagramCanvasExt {

	default void addDraggedLayout(
			final List<DraggedLayout> layouts,
			final Set<String> seen,
			final SelectedElement element,
			final NodeLayout fallbackLayout) {
		final NodeLayout layout = getCanvas().resolveNodeLayoutForSelection(element, fallbackLayout);
		if (layout == null) {
			return;
		}

		final String key = layout.getObjectType() + ":" + layout.getObjectId();
		if (!seen.add(key)) {
			return;
		}

		layouts.add(new DraggedLayout(layout, layout.getPosition().getX(), layout.getPosition().getY()));
	}

	default void buildDragRenderLayers(final DraggedSelection selection) {
		getCanvas().currentDragOffset = new Point2D.Double();
	}

	default DraggedSelection createDraggedSelection(
			final SelectedElement hitSelection,
			final NodeLayout hitLayout,
			final Point2D.Double worldPoint,
			final Rectangle2D hitBounds) {
		final List<DraggedLayout> layouts = new ArrayList<>();
		final Set<String> seen = new HashSet<>();

		if (getCanvas().selectedElements.isEmpty() || !getCanvas().isElementSelected(hitSelection)) {
			getCanvas().addDraggedLayout(layouts, seen, hitSelection, hitLayout);
		} else {
			for (final SelectedElement element : getCanvas().selectedElements) {
				getCanvas().addDraggedLayout(layouts, seen, element, null);
			}

			if (layouts.isEmpty()) {
				getCanvas().addDraggedLayout(layouts, seen, hitSelection, hitLayout);
			}
		}

		final DraggedSelection selection = new DraggedSelection(layouts,
				worldPoint.getX() - hitBounds.getX(),
				worldPoint.getY() - hitBounds.getY(),
				hitLayout.getPosition().getX(),
				hitLayout.getPosition().getY());

		getCanvas().buildDragRenderLayers(selection);
		return selection;
	}

	default boolean isDragRenderingActive() {
		return getCanvas().draggedSelection != null;
	}

	default NodeLayout resolveNodeLayoutForSelection(final SelectedElement element, final NodeLayout fallbackLayout) {
		if (element == null) {
			return fallbackLayout;
		}

		return (switch (element.type()) {
		case CLASS, FIELD -> getCanvas().findNodeLayout(LayoutObjectType.CLASS, element.classId());
		case COMMENT -> getCanvas().findNodeLayout(LayoutObjectType.COMMENT, element.commentId());
		default -> Optional.<NodeLayout>empty();
		}).orElse(fallbackLayout);
	}

}
