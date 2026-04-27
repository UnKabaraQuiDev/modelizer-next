package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.HitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkCreationState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ResizingComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

interface MouseInteractionController extends DiagramCanvasExt {

	default MouseAdapter createMouseAdapter() {
		return new MouseAdapter() {

			@Override
			public void mouseDragged(final MouseEvent e) {
				handleMouseDragged(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				handleMousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				handleMouseReleased(e);
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				handleMouseWheelMoved(e);
			}

		};
	}

	default void handleMouseDragged(final MouseEvent event) {
		if (getCanvas().panning && getCanvas().lastScreenPoint != null) {
			final PanelState state = getCanvas().getPanelState();
			state.setPanX(state.getPanX() + event.getX() - getCanvas().lastScreenPoint.x);
			state.setPanY(state.getPanY() + event.getY() - getCanvas().lastScreenPoint.y);
			getCanvas().lastScreenPoint = event.getPoint();
			getCanvas().repaint();
			return;
		}

		if (getCanvas().linkCreationState != null) {
			final Point2D.Double worldPoint = getCanvas().screenToWorld(event.getPoint());
			getCanvas().linkPreviewMousePoint = worldPoint;

			final HitResult hitResult = getCanvas().findTopmostHit(worldPoint);
			getCanvas().linkPreviewTarget = hitResult == null ? null
					: getCanvas().normalizeConnectionTargetSelection(hitResult.selection());

			getCanvas().repaint();
			return;
		}

		if (getCanvas().resizingComment != null) {
			final Point2D.Double worldPoint = getCanvas().screenToWorld(event.getPoint());
			getCanvas().resizingComment.layout()
					.getSize()
					.setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE,
							getCanvas().resizingComment.initialWidth() + (worldPoint.getX() - getCanvas().resizingComment.startWorldX())));
			getCanvas().resizingComment.layout()
					.getSize()
					.setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT,
							getCanvas().resizingComment.initialHeight() + (worldPoint.getY() - getCanvas().resizingComment.startWorldY())));
			getCanvas().repaint();
			return;
		}

		if (getCanvas().draggedSelection == null) {
			return;
		}

		getCanvas().dragOccurred = true;

		final Point2D.Double worldPoint = getCanvas().screenToWorld(event.getPoint());
		final double anchorX = worldPoint.getX() - getCanvas().draggedSelection.offsetX();
		final double anchorY = worldPoint.getY() - getCanvas().draggedSelection.offsetY();

		final double deltaX = anchorX - getCanvas().draggedSelection.anchorStartX();
		final double deltaY = anchorY - getCanvas().draggedSelection.anchorStartY();

		final double zoom = getCanvas().getPanelState().getZoom();
		getCanvas().currentDragOffset = new Point2D.Double(deltaX * zoom, deltaY * zoom);

		getCanvas().repaint();
	}

	default void handleMousePressed(final MouseEvent event) {
		getCanvas().requestFocusInWindow();
		getCanvas().lastScreenPoint = event.getPoint();

		if (SwingUtilities.isMiddleMouseButton(event)) {
			getCanvas().panning = true;
			getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}

		final Point2D.Double worldPoint = getCanvas().screenToWorld(event.getPoint());
		final HitResult hitResult = getCanvas().findTopmostHit(worldPoint);

		if (SwingUtilities.isRightMouseButton(event)) {
			if (hitResult == null) {
				return;
			}

			final SelectedElement source = getCanvas().normalizeConnectionSourceSelection(hitResult.selection());
			if (source == null) {
				return;
			}

			if (!getCanvas().selectedElements.contains(source)) {
				getCanvas().select(source);
			} else {
				getCanvas().selectedElement = source;
				getCanvas().notifySelectionChanged();
			}

			getCanvas().linkCreationState = LinkCreationState.fromSelection(source);
			getCanvas().linkPreviewTarget = null;
			getCanvas().linkPreviewMousePoint = worldPoint;
			getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			getCanvas().repaint();
			return;
		}

		if (!SwingUtilities.isLeftMouseButton(event)) {
			return;
		}

		getCanvas().pendingClickSelection = hitResult == null ? null : hitResult.selection();
		getCanvas().pendingModifierSelection = event.isShiftDown() || event.isControlDown();
		getCanvas().dragOccurred = false;

		if (hitResult == null) {
			if (!getCanvas().pendingModifierSelection) {
				getCanvas().clearSelection();
			}
			return;
		}

		final SelectedElement clickedElement = hitResult.selection();
		final boolean clickedAlreadySelected = getCanvas().isElementSelected(clickedElement);

		if (!getCanvas().pendingModifierSelection) {
			if (clickedAlreadySelected) {
				getCanvas().selectedElement = clickedElement;
				getCanvas().document.getModel().getClasses().sort(getCanvas().comparator);
				getCanvas().notifySelectionChanged();
				getCanvas().repaint();
			} else {
				getCanvas().select(clickedElement);
			}
		}

		if (!getCanvas().pendingModifierSelection && event.getClickCount() == 2) {
			getCanvas().openEditDialogForSelection();
			return;
		}

		if (!getCanvas().pendingModifierSelection && hitResult.selection().type() == SelectedType.COMMENT && hitResult.bounds() != null
				&& getCanvas().isInCommentResizeHandle(hitResult.bounds(), worldPoint)) {
			getCanvas().resizingComment = new ResizingComment(hitResult
					.layout(), hitResult.bounds().getWidth(), hitResult.bounds().getHeight(), worldPoint.getX(), worldPoint.getY());
			getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}

		if (hitResult.layout() != null) {
			if (getCanvas().pendingModifierSelection && !getCanvas().isElementSelected(clickedElement)) {
				return;
			}

			getCanvas().draggedSelection = getCanvas()
					.createDraggedSelection(clickedElement, hitResult.layout(), worldPoint, hitResult.bounds());
			getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	default void handleMouseReleased(final MouseEvent event) {
		boolean documentChanged = false;

		if (SwingUtilities.isRightMouseButton(event) && getCanvas().linkCreationState != null) {
			getCanvas().finishLinkCreation(getCanvas().screenToWorld(event.getPoint()));
		}

		if (SwingUtilities.isLeftMouseButton(event) && getCanvas().draggedSelection != null && getCanvas().dragOccurred) {
			final double zoom = getCanvas().getPanelState().getZoom();
			final double deltaX = getCanvas().currentDragOffset.getX() / zoom;
			final double deltaY = getCanvas().currentDragOffset.getY() / zoom;

			if (Math.abs(deltaX) > 0.0001 || Math.abs(deltaY) > 0.0001) {
				for (final DraggedLayout draggedLayout : getCanvas().draggedSelection.layouts()) {
					draggedLayout.layout().getPosition().setLocation(draggedLayout.startX() + deltaX, draggedLayout.startY() + deltaY);
				}
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && getCanvas().resizingComment != null) {
			final double currentWidth = getCanvas().resizingComment.layout().getSize().getWidth();
			final double currentHeight = getCanvas().resizingComment.layout().getSize().getHeight();

			if (Math.abs(currentWidth - getCanvas().resizingComment.initialWidth()) > 0.0001
					|| Math.abs(currentHeight - getCanvas().resizingComment.initialHeight()) > 0.0001) {
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && getCanvas().pendingModifierSelection && !getCanvas().dragOccurred) {
			getCanvas().updateSelectionFromMouse(getCanvas().pendingClickSelection, event);
		}

		getCanvas().draggedSelection = null;
		getCanvas().resizingComment = null;
		getCanvas().panning = false;
		getCanvas().lastScreenPoint = null;
		getCanvas().linkCreationState = null;
		getCanvas().linkPreviewTarget = null;
		getCanvas().linkPreviewMousePoint = null;

		getCanvas().pendingClickSelection = null;
		getCanvas().pendingModifierSelection = false;
		getCanvas().dragOccurred = false;

		getCanvas().currentDragOffset = new Point2D.Double();

		if (documentChanged) {
			getCanvas().notifyDocumentChanged();
		}

		getCanvas().setCursor(Cursor.getDefaultCursor());
		getCanvas().repaint();
	}

	default void handleMouseWheelMoved(final MouseWheelEvent event) {
		final PanelState state = getCanvas().getPanelState();
		final Point2D.Double worldBefore = getCanvas().screenToWorld(event.getPoint());

		final double zoomFactor = event.getWheelRotation() < 0 ? 1.1 : 1.0 / 1.1;
		final double newZoom = getCanvas().clamp(state.getZoom() * zoomFactor, 0.2, 4.0);
		state.setZoom(newZoom);

		state.setPanX(event.getX() - worldBefore.getX() * newZoom);
		state.setPanY(event.getY() - worldBefore.getY() * newZoom);

		getCanvas().repaint();
	}

}
