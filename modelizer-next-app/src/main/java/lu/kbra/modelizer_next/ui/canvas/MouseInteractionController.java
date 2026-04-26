package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.HitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkCreationState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ResizingComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

final class MouseInteractionController {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	MouseInteractionController(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setMouseInteractionController(this);
	}

	void handleMouseDragged(final MouseEvent event) {
		if (this.canvas.panning && this.canvas.lastScreenPoint != null) {
			final PanelState state = this.canvas.getPanelState();
			state.setPanX(state.getPanX() + event.getX() - this.canvas.lastScreenPoint.x);
			state.setPanY(state.getPanY() + event.getY() - this.canvas.lastScreenPoint.y);
			this.canvas.lastScreenPoint = event.getPoint();
			this.canvas.repaint();
			return;
		}

		if (this.canvas.linkCreationState != null) {
			final Point2D.Double worldPoint = this.canvas.screenToWorld(event.getPoint());
			this.canvas.linkPreviewMousePoint = worldPoint;

			final HitResult hitResult = this.canvas.findTopmostHit(worldPoint);
			this.canvas.linkPreviewTarget = hitResult == null ? null
					: this.canvas.normalizeConnectionTargetSelection(hitResult.selection());

			this.canvas.repaint();
			return;
		}

		if (this.canvas.resizingComment != null) {
			final Point2D.Double worldPoint = this.canvas.screenToWorld(event.getPoint());
			this.canvas.resizingComment.layout()
					.getSize()
					.setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE,
							this.canvas.resizingComment.initialWidth() + (worldPoint.getX() - this.canvas.resizingComment.startWorldX())));
			this.canvas.resizingComment.layout()
					.getSize()
					.setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT,
							this.canvas.resizingComment.initialHeight() + (worldPoint.getY() - this.canvas.resizingComment.startWorldY())));
			this.canvas.repaint();
			return;
		}

		if (this.canvas.draggedSelection == null) {
			return;
		}

		this.canvas.dragOccurred = true;

		final Point2D.Double worldPoint = this.canvas.screenToWorld(event.getPoint());
		final double anchorX = worldPoint.getX() - this.canvas.draggedSelection.offsetX();
		final double anchorY = worldPoint.getY() - this.canvas.draggedSelection.offsetY();

		final double deltaX = anchorX - this.canvas.draggedSelection.anchorStartX();
		final double deltaY = anchorY - this.canvas.draggedSelection.anchorStartY();

		final double zoom = this.canvas.getPanelState().getZoom();
		this.canvas.currentDragOffset = new Point2D.Double(deltaX * zoom, deltaY * zoom);

		this.canvas.repaint();
	}

	void handleMousePressed(final MouseEvent event) {
		this.canvas.requestFocusInWindow();
		this.canvas.lastScreenPoint = event.getPoint();

		if (SwingUtilities.isMiddleMouseButton(event)) {
			this.canvas.panning = true;
			this.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}

		final Point2D.Double worldPoint = this.canvas.screenToWorld(event.getPoint());
		final HitResult hitResult = this.canvas.findTopmostHit(worldPoint);

		if (SwingUtilities.isRightMouseButton(event)) {
			if (hitResult == null) {
				return;
			}

			final SelectedElement source = this.canvas.normalizeConnectionSourceSelection(hitResult.selection());
			if (source == null) {
				return;
			}

			if (!this.canvas.selectedElements.contains(source)) {
				this.canvas.select(source);
			} else {
				this.canvas.selectedElement = source;
				this.canvas.notifySelectionChanged();
			}

			this.canvas.linkCreationState = LinkCreationState.fromSelection(source);
			this.canvas.linkPreviewTarget = null;
			this.canvas.linkPreviewMousePoint = worldPoint;
			this.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			this.canvas.repaint();
			return;
		}

		if (!SwingUtilities.isLeftMouseButton(event)) {
			return;
		}

		this.canvas.pendingClickSelection = hitResult == null ? null : hitResult.selection();
		this.canvas.pendingModifierSelection = event.isShiftDown() || event.isControlDown();
		this.canvas.dragOccurred = false;

		if (hitResult == null) {
			if (!this.canvas.pendingModifierSelection) {
				this.canvas.clearSelection();
			}
			return;
		}

		final SelectedElement clickedElement = hitResult.selection();
		final boolean clickedAlreadySelected = this.canvas.isElementSelected(clickedElement);

		if (!this.canvas.pendingModifierSelection) {
			if (clickedAlreadySelected) {
				this.canvas.selectedElement = clickedElement;
				this.canvas.document.getModel().getClasses().sort(this.canvas.comparator);
				this.canvas.notifySelectionChanged();
				this.canvas.repaint();
			} else {
				this.canvas.select(clickedElement);
			}
		}

		if (!this.canvas.pendingModifierSelection && event.getClickCount() == 2) {
			this.canvas.openEditDialogForSelection();
			return;
		}

		if (!this.canvas.pendingModifierSelection && hitResult.selection().type() == SelectedType.COMMENT && hitResult.bounds() != null
				&& this.canvas.isInCommentResizeHandle(hitResult.bounds(), worldPoint)) {
			this.canvas.resizingComment = new ResizingComment(hitResult
					.layout(), hitResult.bounds().getWidth(), hitResult.bounds().getHeight(), worldPoint.getX(), worldPoint.getY());
			this.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}

		if (hitResult.layout() != null) {
			if (this.canvas.pendingModifierSelection && !this.canvas.isElementSelected(clickedElement)) {
				return;
			}

			this.canvas.draggedSelection = this.canvas
					.createDraggedSelection(clickedElement, hitResult.layout(), worldPoint, hitResult.bounds());
			this.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	void handleMouseReleased(final MouseEvent event) {
		boolean documentChanged = false;

		if (SwingUtilities.isRightMouseButton(event) && this.canvas.linkCreationState != null) {
			this.canvas.finishLinkCreation(this.canvas.screenToWorld(event.getPoint()));
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.canvas.draggedSelection != null && this.canvas.dragOccurred) {
			final double zoom = this.canvas.getPanelState().getZoom();
			final double deltaX = this.canvas.currentDragOffset.getX() / zoom;
			final double deltaY = this.canvas.currentDragOffset.getY() / zoom;

			if (Math.abs(deltaX) > 0.0001 || Math.abs(deltaY) > 0.0001) {
				for (final DraggedLayout draggedLayout : this.canvas.draggedSelection.layouts()) {
					draggedLayout.layout().getPosition().setLocation(draggedLayout.startX() + deltaX, draggedLayout.startY() + deltaY);
				}
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.canvas.resizingComment != null) {
			final double currentWidth = this.canvas.resizingComment.layout().getSize().getWidth();
			final double currentHeight = this.canvas.resizingComment.layout().getSize().getHeight();

			if (Math.abs(currentWidth - this.canvas.resizingComment.initialWidth()) > 0.0001
					|| Math.abs(currentHeight - this.canvas.resizingComment.initialHeight()) > 0.0001) {
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.canvas.pendingModifierSelection && !this.canvas.dragOccurred) {
			this.canvas.updateSelectionFromMouse(this.canvas.pendingClickSelection, event);
		}

		this.canvas.draggedSelection = null;
		this.canvas.resizingComment = null;
		this.canvas.panning = false;
		this.canvas.lastScreenPoint = null;
		this.canvas.linkCreationState = null;
		this.canvas.linkPreviewTarget = null;
		this.canvas.linkPreviewMousePoint = null;

		this.canvas.pendingClickSelection = null;
		this.canvas.pendingModifierSelection = false;
		this.canvas.dragOccurred = false;

		this.canvas.currentDragOffset = new Point2D.Double();

		if (documentChanged) {
			this.canvas.notifyDocumentChanged();
		}

		this.canvas.setCursor(Cursor.getDefaultCursor());
		this.canvas.repaint();
	}

	void handleMouseWheelMoved(final MouseWheelEvent event) {
		final PanelState state = this.canvas.getPanelState();
		final Point2D.Double worldBefore = this.canvas.screenToWorld(event.getPoint());

		final double zoomFactor = event.getWheelRotation() < 0 ? 1.1 : 1.0 / 1.1;
		final double newZoom = this.canvas.clamp(state.getZoom() * zoomFactor, 0.2, 4.0);
		state.setZoom(newZoom);

		state.setPanX(event.getX() - worldBefore.getX() * newZoom);
		state.setPanY(event.getY() - worldBefore.getY() * newZoom);

		this.canvas.repaint();
	}
}
