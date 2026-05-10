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

/**
 * Contains drag-selection helpers for moving selected canvas elements.
 */
interface DragSelectionController extends DiagramCanvasExt {

	/**
	 * Adds the dragged layout.
	 *
	 * @param layouts        layout objects to read or modify
	 * @param seen           seen value used by the operation
	 * @param element        element value used by the operation
	 * @param fallbackLayout layout object to read or modify
	 */
	default void addDraggedLayout(
			final List<DraggedLayout> layouts,
			final Set<String> seen,
			final SelectedElement element,
			final NodeLayout fallbackLayout) {
		final NodeLayout layout = this.getCanvas().resolveNodeLayoutForSelection(element, fallbackLayout);
		if (layout == null) {
			return;
		}

		final String key = layout.getObjectType() + ":" + layout.getObjectId();
		if (!seen.add(key)) {
			return;
		}

		layouts.add(new DraggedLayout(layout, layout.getPosition().getX(), layout.getPosition().getY()));
	}

	/**
	 * Builds a drag render layers.
	 *
	 * @param selection selection state to read or update
	 */
	default void buildDragRenderLayers(final DraggedSelection selection) {
		this.getCanvas().currentDragOffset = new Point2D.Double();
	}

	/**
	 * Creates a dragged selection.
	 *
	 * @param hitSelection hit selection value used by the operation
	 * @param hitLayout    layout object to read or modify
	 * @param worldPoint   point in canvas coordinates
	 * @param hitBounds    bounds used for layout or hit testing
	 * @return the created dragged selection
	 */
	default DraggedSelection createDraggedSelection(
			final SelectedElement hitSelection,
			final NodeLayout hitLayout,
			final Point2D.Double worldPoint,
			final Rectangle2D hitBounds) {
		final List<DraggedLayout> layouts = new ArrayList<>();
		final Set<String> seen = new HashSet<>();

		if (this.getCanvas().selectedElements.isEmpty() || !this.getCanvas().isElementSelected(hitSelection)) {
			this.getCanvas().addDraggedLayout(layouts, seen, hitSelection, hitLayout);
		} else {
			for (final SelectedElement element : this.getCanvas().selectedElements) {
				this.getCanvas().addDraggedLayout(layouts, seen, element, null);
			}

			if (layouts.isEmpty()) {
				this.getCanvas().addDraggedLayout(layouts, seen, hitSelection, hitLayout);
			}
		}

		final DraggedSelection selection = new DraggedSelection(layouts,
				worldPoint.getX() - hitBounds.getX(),
				worldPoint.getY() - hitBounds.getY(),
				hitLayout.getPosition().getX(),
				hitLayout.getPosition().getY());

		this.getCanvas().buildDragRenderLayers(selection);
		return selection;
	}

	/**
	 * Checks whether drag rendering active is enabled or applies.
	 *
	 * @return {@code true} if drag rendering active is enabled or applies; otherwise {@code false}
	 */
	default boolean isDragRenderingActive() {
		return this.getCanvas().draggedSelection != null;
	}

	/**
	 * Resolves the node layout for selection from the current model and layout state.
	 *
	 * @param element        element value used by the operation
	 * @param fallbackLayout layout object to read or modify
	 * @return the resolved node layout for selection
	 */
	default NodeLayout resolveNodeLayoutForSelection(final SelectedElement element, final NodeLayout fallbackLayout) {
		if (element == null) {
			return fallbackLayout;
		}

		return (switch (element.type()) {
		case CLASS, FIELD -> this.getCanvas().findNodeLayout(LayoutObjectType.CLASS, element.classId());
		case COMMENT -> this.getCanvas().findNodeLayout(LayoutObjectType.COMMENT, element.commentId());
		default -> Optional.<NodeLayout>empty();
		}).orElse(fallbackLayout);
	}

}
