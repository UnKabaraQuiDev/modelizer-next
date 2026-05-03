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

/**
 * Contains mouse interaction handling for selection, dragging, panning, and link creation.
 */
interface MouseInteractionController extends DiagramCanvasExt {

	default MouseAdapter createMouseAdapter() {
		return new MouseAdapter() {

			@Override
			public void mouseDragged(final MouseEvent e) {
				MouseInteractionController.this.handleMouseDragged(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				MouseInteractionController.this.handleMousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				MouseInteractionController.this.handleMouseReleased(e);
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				MouseInteractionController.this.handleMouseWheelMoved(e);
			}

		};
	}

	default void handleMouseDragged(final MouseEvent event) {
		if (this.getCanvas().panning && this.getCanvas().lastScreenPoint != null) {
			final PanelState state = this.getCanvas().getPanelState();
			state.setPanX(state.getPanX() + event.getX() - this.getCanvas().lastScreenPoint.x);
			state.setPanY(state.getPanY() + event.getY() - this.getCanvas().lastScreenPoint.y);
			this.getCanvas().lastScreenPoint = event.getPoint();
			this.getCanvas().repaint();
			return;
		}

		if (this.getCanvas().linkCreationState != null) {
			final Point2D.Double worldPoint = this.getCanvas().screenToWorld(event.getPoint());
			this.getCanvas().linkPreviewMousePoint = worldPoint;

			final HitResult hitResult = this.getCanvas().findTopmostHit(worldPoint);
			this.getCanvas().linkPreviewTarget = hitResult == null ? null
					: this.getCanvas().normalizeConnectionTargetSelection(hitResult.selection());

			this.getCanvas().repaint();
			return;
		}

		if (this.getCanvas().resizingComment != null) {
			final Point2D.Double worldPoint = this.getCanvas().screenToWorld(event.getPoint());
			this.getCanvas().resizingComment.layout()
					.getSize()
					.setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE,
							this.getCanvas().resizingComment.initialWidth()
									+ (worldPoint.getX() - this.getCanvas().resizingComment.startWorldX())));
			this.getCanvas().resizingComment.layout()
					.getSize()
					.setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT,
							this.getCanvas().resizingComment.initialHeight()
									+ (worldPoint.getY() - this.getCanvas().resizingComment.startWorldY())));
			this.getCanvas().repaint();
			return;
		}

		if (this.getCanvas().draggedSelection == null) {
			return;
		}

		this.getCanvas().dragOccurred = true;

		final Point2D.Double worldPoint = this.getCanvas().screenToWorld(event.getPoint());
		final double anchorX = worldPoint.getX() - this.getCanvas().draggedSelection.offsetX();
		final double anchorY = worldPoint.getY() - this.getCanvas().draggedSelection.offsetY();

		final double deltaX = anchorX - this.getCanvas().draggedSelection.anchorStartX();
		final double deltaY = anchorY - this.getCanvas().draggedSelection.anchorStartY();

		final double zoom = this.getCanvas().getPanelState().getZoom();
		this.getCanvas().currentDragOffset = new Point2D.Double(deltaX * zoom, deltaY * zoom);

		this.getCanvas().repaint();
	}

	default void handleMousePressed(final MouseEvent event) {
		this.getCanvas().requestFocusInWindow();
		this.getCanvas().lastScreenPoint = event.getPoint();

		if (SwingUtilities.isMiddleMouseButton(event)) {
			this.getCanvas().panning = true;
			this.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}

		final Point2D.Double worldPoint = this.getCanvas().screenToWorld(event.getPoint());
		final HitResult hitResult = this.getCanvas().findTopmostHit(worldPoint);

		if (SwingUtilities.isRightMouseButton(event)) {
			if (hitResult == null) {
				return;
			}

			final SelectedElement source = this.getCanvas().normalizeConnectionSourceSelection(hitResult.selection());
			if (source == null) {
				return;
			}

			if (!this.getCanvas().selectedElements.contains(source)) {
				this.getCanvas().select(source);
			} else {
				this.getCanvas().selectedElement = source;
				this.getCanvas().notifySelectionChanged();
			}

			this.getCanvas().linkCreationState = LinkCreationState.fromSelection(source);
			this.getCanvas().linkPreviewTarget = null;
			this.getCanvas().linkPreviewMousePoint = worldPoint;
			this.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			this.getCanvas().repaint();
			return;
		}

		if (!SwingUtilities.isLeftMouseButton(event)) {
			return;
		}

		this.getCanvas().pendingClickSelection = hitResult == null ? null : hitResult.selection();
		this.getCanvas().pendingModifierSelection = event.isShiftDown() || event.isControlDown();
		this.getCanvas().dragOccurred = false;

		if (hitResult == null) {
			if (!this.getCanvas().pendingModifierSelection) {
				this.getCanvas().clearSelection();
			}
			return;
		}

		final SelectedElement clickedElement = hitResult.selection();
		final boolean clickedAlreadySelected = this.getCanvas().isElementSelected(clickedElement);

		if (!this.getCanvas().pendingModifierSelection) {
			if (clickedAlreadySelected) {
				this.getCanvas().selectedElement = clickedElement;
				this.getCanvas().document.getModel().getClasses().sort(this.getCanvas().comparator);
				this.getCanvas().notifySelectionChanged();
				this.getCanvas().repaint();
			} else {
				this.getCanvas().select(clickedElement);
			}
		}

		if (!this.getCanvas().pendingModifierSelection && event.getClickCount() == 2) {
			this.getCanvas().openEditDialogForSelection();
			return;
		}

		if (!this.getCanvas().pendingModifierSelection && hitResult.selection().type() == SelectedType.COMMENT && hitResult.bounds() != null
				&& this.getCanvas().isInCommentResizeHandle(hitResult.bounds(), worldPoint)) {
			this.getCanvas().resizingComment = new ResizingComment(hitResult
					.layout(), hitResult.bounds().getWidth(), hitResult.bounds().getHeight(), worldPoint.getX(), worldPoint.getY());
			this.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}

		if (hitResult.layout() != null) {
			if (this.getCanvas().pendingModifierSelection && !this.getCanvas().isElementSelected(clickedElement)) {
				return;
			}

			this.getCanvas().draggedSelection = this.getCanvas()
					.createDraggedSelection(clickedElement, hitResult.layout(), worldPoint, hitResult.bounds());
			this.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	default void handleMouseReleased(final MouseEvent event) {
		boolean documentChanged = false;

		if (SwingUtilities.isRightMouseButton(event) && this.getCanvas().linkCreationState != null) {
			this.getCanvas().finishLinkCreation(this.getCanvas().screenToWorld(event.getPoint()));
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.getCanvas().draggedSelection != null && this.getCanvas().dragOccurred) {
			final double zoom = this.getCanvas().getPanelState().getZoom();
			final double deltaX = this.getCanvas().currentDragOffset.getX() / zoom;
			final double deltaY = this.getCanvas().currentDragOffset.getY() / zoom;

			if (Math.abs(deltaX) > 0.0001 || Math.abs(deltaY) > 0.0001) {
				for (final DraggedLayout draggedLayout : this.getCanvas().draggedSelection.layouts()) {
					draggedLayout.layout().getPosition().setLocation(draggedLayout.startX() + deltaX, draggedLayout.startY() + deltaY);
				}
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.getCanvas().resizingComment != null) {
			final double currentWidth = this.getCanvas().resizingComment.layout().getSize().getWidth();
			final double currentHeight = this.getCanvas().resizingComment.layout().getSize().getHeight();

			if (Math.abs(currentWidth - this.getCanvas().resizingComment.initialWidth()) > 0.0001
					|| Math.abs(currentHeight - this.getCanvas().resizingComment.initialHeight()) > 0.0001) {
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.getCanvas().pendingModifierSelection && !this.getCanvas().dragOccurred) {
			this.getCanvas().updateSelectionFromMouse(this.getCanvas().pendingClickSelection, event);
		}

		this.getCanvas().draggedSelection = null;
		this.getCanvas().resizingComment = null;
		this.getCanvas().panning = false;
		this.getCanvas().lastScreenPoint = null;
		this.getCanvas().linkCreationState = null;
		this.getCanvas().linkPreviewTarget = null;
		this.getCanvas().linkPreviewMousePoint = null;

		this.getCanvas().pendingClickSelection = null;
		this.getCanvas().pendingModifierSelection = false;
		this.getCanvas().dragOccurred = false;

		this.getCanvas().currentDragOffset = new Point2D.Double();

		if (documentChanged) {
			this.getCanvas().notifyDocumentChanged();
		}

		this.getCanvas().setCursor(Cursor.getDefaultCursor());
		this.getCanvas().repaint();
	}

	default void handleMouseWheelMoved(final MouseWheelEvent event) {
		final PanelState state = this.getCanvas().getPanelState();
		final Point2D.Double worldBefore = this.getCanvas().screenToWorld(event.getPoint());

		final double zoomFactor = event.getWheelRotation() < 0 ? 1.1 : 1.0 / 1.1;
		final double newZoom = this.getCanvas().clamp(state.getZoom() * zoomFactor, 0.2, 4.0);
		state.setZoom(newZoom);

		state.setPanX(event.getX() - worldBefore.getX() * newZoom);
		state.setPanY(event.getY() - worldBefore.getY() * newZoom);

		this.getCanvas().repaint();
	}

}
