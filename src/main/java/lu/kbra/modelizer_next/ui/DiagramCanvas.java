package lu.kbra.modelizer_next.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.common.Point2;
import lu.kbra.modelizer_next.common.Size2;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;

public class DiagramCanvas extends JPanel {
	private static final long serialVersionUID = -3410889122837999151L;
	private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	private static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	private static final int CLASS_MIN_WIDTH = 140;
	private static final int COMMENT_MIN_WIDTH = 120;
	private static final int HEADER_HEIGHT = 28;
	private static final int ROW_HEIGHT = 22;
	private static final int PADDING = 8;
	private static final int COMMENT_RESIZE_HANDLE_SIZE = 12;
	private static final int COMMENT_MIN_HEIGHT = 40;
	private static final int COMMENT_MIN_WIDTH_VALUE = 120;

	private static final Color GRID_COLOR = new Color(0xE4E4E4);
	private static final Color LINK_COLOR = new Color(0x555555);
	private static final Color SELECTION_COLOR = new Color(0x2F7DFF);

	private static final double LINK_HIT_DISTANCE = 6.0;

	private final ModelDocument document;
	private final PanelType panelType;
	private final CanvasStatusListener statusListener;

	private DraggedNode draggedNode;
	private Point lastScreenPoint;
	private boolean panning;

	private LinkCreationState linkCreationState;
	private Point2 currentLinkPreviewPoint;

	private SelectedElement selectedElement;
	private ResizingComment resizingComment;

	public DiagramCanvas(final ModelDocument document, final PanelType panelType,
			final CanvasStatusListener statusListener) {
		this.document = document;
		this.panelType = panelType;
		this.statusListener = statusListener;

		this.setBackground(new Color(0xF2F2F2));
		this.setOpaque(true);
		this.setFocusable(true);

		this.installKeyBindings();

		final MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				DiagramCanvas.this.handleMousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				DiagramCanvas.this.handleMouseReleased(e);
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				DiagramCanvas.this.handleMouseDragged(e);
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				DiagramCanvas.this.handleMouseWheelMoved(e);
			}
		};

		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		this.addMouseWheelListener(mouseAdapter);
	}

	public PanelType getPanelType() {
		return this.panelType;
	}

	public SelectionInfo getSelectionInfo() {
		return new SelectionInfo(this.panelType, this.buildSelectionPath());
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		this.ensureLayouts();

		final Graphics2D g2 = (Graphics2D) graphics.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		this.drawGrid(g2);

		final AffineTransform oldTransform = g2.getTransform();
		final PanelState state = this.getPanelState();
		g2.translate(state.getPanX(), state.getPanY());
		g2.scale(state.getZoom(), state.getZoom());

		this.drawLinks(g2);
		this.drawComments(g2);
		this.drawClasses(g2);
		this.drawLinkPreview(g2);

		g2.setTransform(oldTransform);
		g2.dispose();
	}

	private void installKeyBindings() {
		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSelection");
		this.getActionMap().put("renameSelection", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.renameSelection();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectFieldUp");
		this.getActionMap().put("selectFieldUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveFieldSelection(-1);
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectFieldDown");
		this.getActionMap().put("selectFieldDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveFieldSelection(1);
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
				"moveFieldUp");
		this.getActionMap().put("moveFieldUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveSelectedFieldInList(-1);
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), "moveFieldDown");
		this.getActionMap().put("moveFieldDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveSelectedFieldInList(1);
			}
		});
	}

	private void moveFieldSelection(final int delta) {
		if (this.selectedElement != null && this.selectedElement.type() == SelectedType.CLASS) {
			final ClassModel classModel = findClassById(selectedElement.classId);
			if (classModel.getFields().isEmpty()) {
				return;
			}
			this.select(SelectedElement.forField(selectedElement.classId, classModel.getFields().get(0).getId()));
			return;
		}

		if (this.selectedElement == null || this.selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.findClassById(this.selectedElement.classId());
		if (classModel == null) {
			return;
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		int currentIndex = -1;
		for (int i = 0; i < visibleFields.size(); i++) {
			if (Objects.equals(visibleFields.get(i).getId(), this.selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex == 0 && delta == -1) {
			select(SelectedElement.forClass(classModel.getId()));
			return;
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= visibleFields.size()) {
			return;
		}

		this.select(SelectedElement.forField(classModel.getId(), visibleFields.get(newIndex).getId()));
	}

	private void moveSelectedFieldInList(final int delta) {
		if (this.selectedElement == null || this.selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.findClassById(this.selectedElement.classId());
		if (classModel == null) {
			return;
		}

		int currentIndex = -1;
		for (int i = 0; i < classModel.getFields().size(); i++) {
			if (Objects.equals(classModel.getFields().get(i).getId(), this.selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= classModel.getFields().size()) {
			return;
		}

		final FieldModel moved = classModel.getFields().remove(currentIndex);
		classModel.getFields().add(newIndex, moved);

		this.select(SelectedElement.forField(classModel.getId(), moved.getId()));
		this.repaint();
	}

	private void handleMousePressed(final MouseEvent event) {
		this.requestFocusInWindow();
		this.lastScreenPoint = event.getPoint();

		if (SwingUtilities.isMiddleMouseButton(event)) {
			this.panning = true;
			this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}

		final Point2 worldPoint = this.screenToWorld(event.getPoint());
		final HitResult hitResult = this.findTopmostHit(worldPoint);

		if (SwingUtilities.isRightMouseButton(event)) {
			if (hitResult == null) {
				return;
			}

			if (this.panelType == PanelType.CONCEPTUAL && hitResult.selection().type() == SelectedType.CLASS) {
				this.select(hitResult.selection());
				this.linkCreationState = new LinkCreationState(hitResult.selection().classId(), null);
				this.currentLinkPreviewPoint = worldPoint;
				this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.repaint();
				return;
			}

			if (this.panelType != PanelType.CONCEPTUAL && hitResult.selection().type() == SelectedType.FIELD) {
				this.select(hitResult.selection());
				this.linkCreationState = new LinkCreationState(hitResult.selection().classId(),
						hitResult.selection().fieldId());
				this.currentLinkPreviewPoint = worldPoint;
				this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.repaint();
			}
			return;
		}

		if (!SwingUtilities.isLeftMouseButton(event)) {
			return;
		}

		if (hitResult == null) {
			this.clearSelection();
			return;
		}

		this.select(hitResult.selection());

		if (event.getClickCount() == 2) {
			this.openEditDialogForSelection();
			return;
		}

		if (hitResult.selection().type() == SelectedType.COMMENT && hitResult.bounds() != null
				&& this.isInCommentResizeHandle(hitResult.bounds(), worldPoint)) {
			this.resizingComment = new ResizingComment(hitResult.layout(), hitResult.bounds().getWidth(),
					hitResult.bounds().getHeight(), worldPoint.getX(), worldPoint.getY());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}

		if (hitResult.layout() != null) {
			this.draggedNode = new DraggedNode(hitResult.layout(), worldPoint.getX() - hitResult.bounds().getX(),
					worldPoint.getY() - hitResult.bounds().getY());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private boolean isInCommentResizeHandle(final Rectangle2D bounds, final Point2 worldPoint) {
		return worldPoint.getX() >= bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE
				&& worldPoint.getY() >= bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE;
	}

	private void openEditDialogForSelection() {
		if (this.selectedElement == null) {
			return;
		}

		switch (this.selectedElement.type()) {
		case FIELD -> this.editField(this.selectedElement.classId(), this.selectedElement.fieldId());
		case COMMENT -> this.editComment(this.selectedElement.commentId());
		case CLASS -> this.editClass(this.selectedElement.classId());
		case LINK -> this.editLink(this.selectedElement.linkId());
		default -> {
		}
		}
	}

	private void editField(final String classId, final String fieldId) {
		final FieldModel fieldModel = this.findFieldById(classId, fieldId);
		if (fieldModel == null) {
			return;
		}

		final FieldEditorDialog.Result result = FieldEditorDialog.showDialog(this, fieldModel);
		if (result == null) {
			return;
		}

		fieldModel.getNames().setName(result.name());
		fieldModel.getNames().setTechnicalName(result.technicalName());
		fieldModel.getStyle().setTextColor(result.textColor());
		fieldModel.getStyle().setBackgroundColor(result.backgroundColor());

		if (result.moveDelta() < 0) {
			this.moveSelectedFieldInList(-1);
		} else if (result.moveDelta() > 0) {
			this.moveSelectedFieldInList(1);
		}

		this.notifySelectionChanged();
		this.repaint();
	}

	private void editComment(final String commentId) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		final CommentEditorDialog.Result result = CommentEditorDialog.showDialog(this,
				this.getEditableCommentText(commentId), commentModel.getTextColor(), commentModel.getBackgroundColor(),
				commentModel.getBorderColor());
		if (result == null) {
			return;
		}

		this.setEditableCommentText(commentId, result.text());
		commentModel.setTextColor(result.textColor());
		commentModel.setBackgroundColor(result.backgroundColor());
		commentModel.setBorderColor(result.borderColor());

		this.notifySelectionChanged();
		this.repaint();
	}

	private void editClass(final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return;
		}

		final ClassEditorDialog.Result result = ClassEditorDialog.showDialog(this, classModel);
		if (result == null) {
			return;
		}

		classModel.getNames().setConceptualName(result.conceptualName());
		classModel.getNames().setTechnicalName(result.technicalName());
		classModel.setComment(result.comment());
		classModel.getStyle().setTextColor(result.textColor());
		classModel.getStyle().setBackgroundColor(result.backgroundColor());
		classModel.getStyle().setBorderColor(result.borderColor());

		this.notifySelectionChanged();
		this.repaint();
	}

	private void editLink(final String linkId) {
		final LinkModel linkModel = this.findLinkById(linkId);
		if (linkModel == null) {
			return;
		}

		final LinkEditorDialog.Result result = LinkEditorDialog.showDialog(this, this.document, linkModel,
				this.panelType);
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		linkModel.setName(result.name());
		linkModel.setComment(result.comment());
		linkModel.setLineColor(result.lineColor());
		linkModel.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		linkModel.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));

		if (this.panelType == PanelType.CONCEPTUAL) {
			linkModel.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			linkModel.setCardinalityTo(
					result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
		} else {
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		this.notifySelectionChanged();
		this.repaint();
	}

	private void handleMouseReleased(final MouseEvent event) {
		if (SwingUtilities.isRightMouseButton(event) && this.linkCreationState != null) {
			this.finishLinkCreation(this.screenToWorld(event.getPoint()));
		}

		this.draggedNode = null;
		this.resizingComment = null;
		this.panning = false;
		this.lastScreenPoint = null;
		this.linkCreationState = null;
		this.currentLinkPreviewPoint = null;
		this.setCursor(Cursor.getDefaultCursor());

		this.repaint();
	}

	private void handleMouseDragged(final MouseEvent event) {
		if (this.panning && this.lastScreenPoint != null) {
			final PanelState state = this.getPanelState();
			state.setPanX(state.getPanX() + event.getX() - this.lastScreenPoint.x);
			state.setPanY(state.getPanY() + event.getY() - this.lastScreenPoint.y);
			this.lastScreenPoint = event.getPoint();
			this.repaint();
			return;
		}

		if (this.linkCreationState != null) {
			this.currentLinkPreviewPoint = this.screenToWorld(event.getPoint());
			this.repaint();
			return;
		}

		if (this.resizingComment != null) {
			final Point2 worldPoint = this.screenToWorld(event.getPoint());
			this.resizingComment.layout().getSize().setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE,
					this.resizingComment.initialWidth() + (worldPoint.getX() - this.resizingComment.startWorldX())));
			this.resizingComment.layout().getSize().setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT,
					this.resizingComment.initialHeight() + (worldPoint.getY() - this.resizingComment.startWorldY())));
			this.repaint();
			return;
		}

		if (this.draggedNode == null) {
			return;
		}

		final Point2 worldPoint = this.screenToWorld(event.getPoint());
		this.draggedNode.layout().getPosition().setX(worldPoint.getX() - this.draggedNode.offsetX());
		this.draggedNode.layout().getPosition().setY(worldPoint.getY() - this.draggedNode.offsetY());
		this.repaint();
	}

	private void handleMouseWheelMoved(final MouseWheelEvent event) {
		final PanelState state = this.getPanelState();
		final Point2 worldBefore = this.screenToWorld(event.getPoint());

		final double zoomFactor = event.getWheelRotation() < 0 ? 1.1 : 1.0 / 1.1;
		final double newZoom = this.clamp(state.getZoom() * zoomFactor, 0.2, 4.0);
		state.setZoom(newZoom);

		state.setPanX(event.getX() - worldBefore.getX() * newZoom);
		state.setPanY(event.getY() - worldBefore.getY() * newZoom);

		this.repaint();
	}

	private void renameSelection() {
		if (this.selectedElement == null || this.selectedElement.type() == SelectedType.NONE) {
			return;
		}

		final String title;
		final String currentValue;

		switch (this.selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = this.findClassById(this.selectedElement.classId());
			if (classModel == null) {
				return;
			}
			title = "Rename class";
			currentValue = this.getEditableClassName(classModel);
		}
		case FIELD -> {
			final FieldModel fieldModel = this.findFieldById(this.selectedElement.classId(),
					this.selectedElement.fieldId());
			if (fieldModel == null) {
				return;
			}
			title = "Rename field";
			currentValue = this.getEditableFieldName(fieldModel);
		}
		case COMMENT -> {
			title = "Rename comment";
			currentValue = this.getEditableCommentText(this.selectedElement.commentId());
		}
		case LINK -> {
			final LinkModel linkModel = this.findLinkById(this.selectedElement.linkId());
			if (linkModel == null) {
				return;
			}
			title = "Rename link";
			currentValue = linkModel.getName();
		}
		default -> {
			return;
		}
		}

		final String newValue = RenameDialog.showDialog(this, title, currentValue);
		if (newValue == null) {
			return;
		}

		switch (this.selectedElement.type()) {
		case CLASS -> this.setEditableClassName(this.findClassById(this.selectedElement.classId()), newValue);
		case FIELD -> this.setEditableFieldName(
				this.findFieldById(this.selectedElement.classId(), this.selectedElement.fieldId()), newValue);
		case COMMENT -> this.setEditableCommentText(this.selectedElement.commentId(), newValue);
		case LINK -> this.findLinkById(this.selectedElement.linkId()).setName(newValue);
		default -> {
		}
		}

		this.notifySelectionChanged();
		this.repaint();
	}

	private void drawGrid(final Graphics2D g2) {
		g2.setColor(DiagramCanvas.GRID_COLOR);
		for (int x = 0; x < this.getWidth(); x += 40) {
			g2.drawLine(x, 0, x, this.getHeight());
		}
		for (int y = 0; y < this.getHeight(); y += 40) {
			g2.drawLine(0, y, this.getWidth(), y);
		}
	}

	private void drawClasses(final Graphics2D g2) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (!this.isVisible(classModel)) {
				continue;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			layout.getSize().setWidth(bounds.getWidth());
			layout.getSize().setHeight(bounds.getHeight());

			g2.setColor(classModel.getStyle().getBackgroundColor());
			g2.fill(bounds);

			g2.setFont(DiagramCanvas.TITLE_FONT);
			g2.setColor(classModel.getStyle().getTextColor());
			g2.drawString(this.resolveClassTitle(classModel), (float) bounds.getX() + DiagramCanvas.PADDING,
					(float) bounds.getY() + DiagramCanvas.HEADER_HEIGHT - 9);

			g2.setFont(DiagramCanvas.BODY_FONT);
			double rowY = bounds.getY() + DiagramCanvas.HEADER_HEIGHT;
			final List<FieldModel> visibleFields = this.getVisibleFields(classModel);

			for (final FieldModel fieldModel : visibleFields) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(bounds.getX(), rowY, bounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);

				g2.setColor(fieldModel.getStyle().getBackgroundColor());
				g2.fill(fieldBounds);

				if (this.isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(this.withAlpha(DiagramCanvas.SELECTION_COLOR, 60));
					g2.fill(fieldBounds);
				}

				g2.setColor(classModel.getStyle().getBorderColor());
				g2.draw(new Line2D.Double(bounds.getX(), rowY, bounds.getMaxX(), rowY));

				g2.setColor(fieldModel.getStyle().getTextColor());
				g2.drawString(this.resolveFieldName(fieldModel), (float) bounds.getX() + DiagramCanvas.PADDING,
						(float) rowY + 15);

				if (this.isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(DiagramCanvas.SELECTION_COLOR);
					g2.setStroke(new BasicStroke(2.0f));
					g2.draw(fieldBounds);
					g2.setStroke(new BasicStroke(1.0f));
				}

				rowY += DiagramCanvas.ROW_HEIGHT;
			}

			if (this.isClassSelected(classModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.setStroke(new BasicStroke(2.5f));
				g2.draw(bounds);
				g2.setStroke(new BasicStroke(1.0f));
			} else {
				g2.setColor(classModel.getStyle().getBorderColor());
				g2.setStroke(new BasicStroke(1.0f));
				g2.draw(bounds);
				g2.draw(new Line2D.Double(bounds.getX(), bounds.getY() + DiagramCanvas.HEADER_HEIGHT, bounds.getMaxX(),
						bounds.getY() + DiagramCanvas.HEADER_HEIGHT));
			}
		}
	}

	private void drawComments(final Graphics2D g2) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String commentText = this.resolveCommentText(commentModel);
			if (commentText == null || commentText.isBlank() || !this.isCommentVisible(commentModel)) {
				continue;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			final Rectangle2D bounds = this.computeCommentBounds(g2, commentText, layout);
			layout.getSize().setWidth(bounds.getWidth());
			layout.getSize().setHeight(bounds.getHeight());

			if (commentModel.getKind() == CommentKind.BOUND && commentModel.getBinding() != null) {
				final Point2D anchor = this.findBoundTargetAnchor(g2, commentModel);
				if (anchor != null) {
					g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR
							: new Color(0x777777));
					g2.setStroke(new BasicStroke(this.isCommentSelected(commentModel.getId()) ? 2.0f : 1.0f));
					g2.draw(new Line2D.Double(anchor.getX(), anchor.getY(), bounds.getCenterX(), bounds.getCenterY()));
					g2.setStroke(new BasicStroke(1.0f));
				}
			}

			g2.setColor(commentModel.getBackgroundColor());
			g2.fill(bounds);

			g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR
					: commentModel.getBorderColor());
			g2.setStroke(new BasicStroke(this.isCommentSelected(commentModel.getId()) ? 2.5f : 1.0f));
			g2.draw(bounds);
			g2.setStroke(new BasicStroke(1.0f));

			g2.setFont(DiagramCanvas.BODY_FONT);
			g2.setColor(commentModel.getTextColor());
			this.drawMultilineText(g2, commentText, bounds, DiagramCanvas.PADDING);

			if (this.isCommentSelected(commentModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.fill(new Rectangle2D.Double(bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE, DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE));
			}
		}
	}

	private void drawLinks(final Graphics2D g2) {
		g2.setFont(DiagramCanvas.BODY_FONT);

		for (final LinkModel linkModel : this.document.getModel().getLinks()) {
			final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);
			if (geometry == null) {
				continue;
			}

			g2.setColor(
					this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			g2.setStroke(new BasicStroke(this.isLinkSelected(linkModel.getId()) ? 2.5f : 1.2f));

			for (int i = 0; i < geometry.points().size() - 1; i++) {
				g2.draw(new Line2D.Double(geometry.points().get(i), geometry.points().get(i + 1)));
			}

			if (this.panelType != PanelType.CONCEPTUAL) {
				this.drawArrowHead(g2, geometry.points().get(geometry.points().size() - 2), geometry.toPoint());
			}

			g2.setStroke(new BasicStroke(1.0f));

			if (linkModel.getName() != null && !linkModel.getName().isBlank()) {
				g2.drawString(linkModel.getName(), (float) geometry.labelPoint().getX() + 4,
						(float) geometry.labelPoint().getY() - 4);
			}

			if (this.panelType == PanelType.CONCEPTUAL) {
				if (linkModel.getCardinalityFrom() != null) {
					g2.drawString(linkModel.getCardinalityFrom().getDisplayValue(),
							(float) geometry.fromPoint().getX() + 4, (float) geometry.fromPoint().getY() - 4);
				}
				if (linkModel.getCardinalityTo() != null) {
					g2.drawString(linkModel.getCardinalityTo().getDisplayValue(), (float) geometry.toPoint().getX() + 4,
							(float) geometry.toPoint().getY() - 4);
				}
			}
		}
	}

	private void drawArrowHead(final Graphics2D g2, final Point2D from, final Point2D to) {
		final double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
		final double arrowLength = 12.0;
		final double wingAngle = Math.PI / 7.0;

		final Point2D left = new Point2D.Double(to.getX() - arrowLength * Math.cos(angle - wingAngle),
				to.getY() - arrowLength * Math.sin(angle - wingAngle));
		final Point2D right = new Point2D.Double(to.getX() - arrowLength * Math.cos(angle + wingAngle),
				to.getY() - arrowLength * Math.sin(angle + wingAngle));

		g2.draw(new Line2D.Double(to, left));
		g2.draw(new Line2D.Double(to, right));
	}

	private void drawLinkPreview(final Graphics2D g2) {
		if (this.linkCreationState == null || this.currentLinkPreviewPoint == null) {
			return;
		}

		final Point2D fromAnchor;
		if (this.panelType == PanelType.CONCEPTUAL) {
			fromAnchor = this.resolveFieldAnchor(g2, this.linkCreationState.classId(), null, true);
		} else {
			fromAnchor = this.resolveFieldAnchor(g2, this.linkCreationState.classId(), this.linkCreationState.fieldId(),
					true);
		}

		if (fromAnchor == null) {
			return;
		}

		g2.setColor(DiagramCanvas.SELECTION_COLOR);
		g2.setStroke(new BasicStroke(1.5f));
		g2.draw(new Line2D.Double(fromAnchor.getX(), fromAnchor.getY(), this.currentLinkPreviewPoint.getX(),
				this.currentLinkPreviewPoint.getY()));
		g2.setStroke(new BasicStroke(1.0f));
	}

	private Point2D findBoundTargetAnchor(final Graphics2D g2, final CommentModel commentModel) {
		if (commentModel.getBinding() == null) {
			return null;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			if (classModel == null || !this.isVisible(classModel)) {
				return null;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
		final LinkGeometry geometry = linkModel == null ? null : this.resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.labelPoint();
	}

	private void finishLinkCreation(final Point2 worldPoint) {
		final HitResult hitResult = this.findTopmostHit(worldPoint);
		if (hitResult == null || this.linkCreationState == null) {
			return;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			if (hitResult.selection().type() != SelectedType.CLASS) {
				return;
			}

			if (this.linkCreationState.classId().equals(hitResult.selection().classId())) {
				return;
			}

			final LinkModel linkModel = new LinkModel();
			linkModel.setFrom(new LinkEnd(this.linkCreationState.classId(), null));
			linkModel.setTo(new LinkEnd(hitResult.selection().classId(), null));
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);

			this.document.getModel().getLinks().add(linkModel);
			this.findOrCreateLinkLayout(linkModel.getId());
			this.select(SelectedElement.forLink(linkModel.getId()));
			return;
		}

		if (hitResult.selection().type() != SelectedType.FIELD) {
			return;
		}

		if (this.linkCreationState.classId().equals(hitResult.selection().classId())
				&& Objects.equals(this.linkCreationState.fieldId(), hitResult.selection().fieldId())) {
			return;
		}

		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(this.linkCreationState.classId(), this.linkCreationState.fieldId()));
		linkModel.setTo(new LinkEnd(hitResult.selection().classId(), hitResult.selection().fieldId()));
		linkModel.setCardinalityFrom(null);
		linkModel.setCardinalityTo(null);

		this.document.getModel().getLinks().add(linkModel);
		this.findOrCreateLinkLayout(linkModel.getId());
		this.select(SelectedElement.forLink(linkModel.getId()));
	}

	private HitResult findTopmostHit(final Point2 worldPoint) {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		try {
			for (int i = this.document.getModel().getComments().size() - 1; i >= 0; i--) {
				final CommentModel commentModel = this.document.getModel().getComments().get(i);
				final String text = this.resolveCommentText(commentModel);

				if (text == null || text.isBlank() || !this.isCommentVisible(commentModel)) {
					continue;
				}

				final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
				final Rectangle2D bounds = this.computeCommentBounds(g2, text, layout);

				if (bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					return new HitResult(layout, bounds, SelectedElement.forComment(commentModel.getId()));
				}
			}

			for (int i = this.document.getModel().getClasses().size() - 1; i >= 0; i--) {
				final ClassModel classModel = this.document.getModel().getClasses().get(i);
				if (!this.isVisible(classModel)) {
					continue;
				}

				final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
				final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);

				if (!bounds.contains(worldPoint.getX(), worldPoint.getY())) {
					continue;
				}

				final FieldHitResult fieldHitResult = this.findFieldHit(classModel, bounds, worldPoint);
				if (fieldHitResult != null) {
					return new HitResult(layout, fieldHitResult.bounds(),
							SelectedElement.forField(classModel.getId(), fieldHitResult.field().getId()));
				}

				return new HitResult(layout, bounds, SelectedElement.forClass(classModel.getId()));
			}

			for (int i = this.document.getModel().getLinks().size() - 1; i >= 0; i--) {
				final LinkModel linkModel = this.document.getModel().getLinks().get(i);
				final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);

				if (geometry != null && this.isPointNearGeometry(worldPoint, geometry)) {
					return new HitResult(null, new Rectangle2D.Double(worldPoint.getX(), worldPoint.getY(), 1, 1),
							SelectedElement.forLink(linkModel.getId()));
				}
			}
		} finally {
			g2.dispose();
		}

		return null;
	}

	private FieldHitResult findFieldHit(final ClassModel classModel, final Rectangle2D classBounds,
			final Point2 worldPoint) {
		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);

		for (int i = 0; i < visibleFields.size(); i++) {
			final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
					classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
					classBounds.getWidth(), DiagramCanvas.ROW_HEIGHT);

			if (fieldBounds.contains(worldPoint.getX(), worldPoint.getY())) {
				return new FieldHitResult(visibleFields.get(i), fieldBounds);
			}
		}

		return null;
	}

	private boolean isPointNearGeometry(final Point2 worldPoint, final LinkGeometry geometry) {
		for (int i = 0; i < geometry.points().size() - 1; i++) {
			final Point2D first = geometry.points().get(i);
			final Point2D second = geometry.points().get(i + 1);

			if (Line2D.ptSegDist(first.getX(), first.getY(), second.getX(), second.getY(), worldPoint.getX(),
					worldPoint.getY()) <= DiagramCanvas.LINK_HIT_DISTANCE) {
				return true;
			}
		}

		return false;
	}

	private Rectangle2D computeClassBounds(final Graphics2D g2, final ClassModel classModel, final NodeLayout layout) {
		g2.setFont(DiagramCanvas.TITLE_FONT);
		final FontMetrics titleMetrics = g2.getFontMetrics();

		g2.setFont(DiagramCanvas.BODY_FONT);
		final FontMetrics bodyMetrics = g2.getFontMetrics();

		int width = Math.max(DiagramCanvas.CLASS_MIN_WIDTH,
				titleMetrics.stringWidth(this.resolveClassTitle(classModel)) + DiagramCanvas.PADDING * 2);
		for (final FieldModel fieldModel : this.getVisibleFields(classModel)) {
			width = Math.max(width,
					bodyMetrics.stringWidth(this.resolveFieldName(fieldModel)) + DiagramCanvas.PADDING * 2);
		}

		final int visibleFieldCount = this.getVisibleFields(classModel).size();
		final int height = DiagramCanvas.HEADER_HEIGHT + visibleFieldCount * DiagramCanvas.ROW_HEIGHT;

		if (layout.getSize().getWidth() <= 0.0) {
			layout.getSize().setWidth(width);
		}
		layout.getSize().setHeight(height);

		return new Rectangle2D.Double(layout.getPosition().getX(), layout.getPosition().getY(),
				Math.max(width, layout.getSize().getWidth()), height);
	}

	private Rectangle2D computeCommentBounds(final Graphics2D g2, final String text, final NodeLayout layout) {
		g2.setFont(DiagramCanvas.BODY_FONT);
		final FontMetrics metrics = g2.getFontMetrics();

		final double width = layout.getSize().getWidth() > 0.0 ? layout.getSize().getWidth()
				: DiagramCanvas.COMMENT_MIN_WIDTH;
		final List<String> wrappedLines = this.wrapText(text, metrics,
				(int) Math.max(40, width - DiagramCanvas.PADDING * 2));
		final int contentHeight = wrappedLines.size() * (metrics.getHeight() + 2) + DiagramCanvas.PADDING * 2;
		final double height = layout.getSize().getHeight() > 0.0 ? layout.getSize().getHeight()
				: Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, contentHeight);

		if (layout.getSize().getWidth() <= 0.0) {
			layout.getSize().setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH, width));
		}
		if (layout.getSize().getHeight() <= 0.0) {
			layout.getSize().setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, contentHeight));
		}

		return new Rectangle2D.Double(layout.getPosition().getX(), layout.getPosition().getY(),
				Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE, layout.getSize().getWidth()),
				Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, layout.getSize().getHeight()));
	}

	private List<String> wrapText(final String text, final FontMetrics metrics, final int maxWidth) {
		final List<String> lines = new ArrayList<>();
		for (final String paragraph : text.split("\\R", -1)) {
			if (paragraph.isEmpty()) {
				lines.add("");
				continue;
			}

			final String[] words = paragraph.split(" ");
			final StringBuilder current = new StringBuilder();

			for (final String word : words) {
				if (current.isEmpty()) {
					current.append(word);
					continue;
				}

				final String candidate = current + " " + word;
				if (metrics.stringWidth(candidate) <= maxWidth) {
					current.append(" ").append(word);
				} else {
					lines.add(current.toString());
					current.setLength(0);
					current.append(word);
				}
			}

			lines.add(current.toString());
		}
		return lines;
	}

	private void drawMultilineText(final Graphics2D g2, final String text, final Rectangle2D bounds,
			final int padding) {
		final FontMetrics metrics = g2.getFontMetrics();
		final List<String> wrappedLines = this.wrapText(text, metrics,
				(int) Math.max(40, bounds.getWidth() - padding * 2));

		float y = (float) bounds.getY() + padding + metrics.getAscent();
		for (final String line : wrappedLines) {
			g2.drawString(line, (float) bounds.getX() + padding, y);
			y += metrics.getHeight() + 2;
		}
	}

	private void ensureLayouts() {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (this.isVisible(classModel)) {
				this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			}
		}

		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String text = this.resolveCommentText(commentModel);
			if (this.isCommentVisible(commentModel) && text != null && !text.isBlank()) {
				this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			}
		}
	}

	private NodeLayout findOrCreateNodeLayout(final LayoutObjectType objectType, final String objectId) {
		for (final NodeLayout layout : this.getPanelState().getNodeLayouts()) {
			if (layout.getObjectType() == objectType && layout.getObjectId().equals(objectId)) {
				return layout;
			}
		}

		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(objectType);
		layout.setObjectId(objectId);
		layout.setPosition(new Point2(80 + this.getPanelState().getNodeLayouts().size() * 30,
				80 + this.getPanelState().getNodeLayouts().size() * 30));
		layout.setSize(new Size2(0, 0));
		this.getPanelState().getNodeLayouts().add(layout);
		return layout;
	}

	private LinkLayout findOrCreateLinkLayout(final String linkId) {
		for (final LinkLayout linkLayout : this.getPanelState().getLinkLayouts()) {
			if (linkLayout.getLinkId().equals(linkId)) {
				return linkLayout;
			}
		}

		final LinkLayout linkLayout = new LinkLayout();
		linkLayout.setLinkId(linkId);
		this.getPanelState().getLinkLayouts().add(linkLayout);
		return linkLayout;
	}

	private List<FieldModel> getVisibleFields(final ClassModel classModel) {
		final List<FieldModel> visibleFields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (this.panelType == PanelType.CONCEPTUAL && fieldModel.isNotConceptual()) {
				continue;
			}
			visibleFields.add(fieldModel);
		}

		return visibleFields;
	}

	private String resolveClassTitle(final ClassModel classModel) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			return this.blankToFallback(classModel.getNames().getConceptualName(),
					classModel.getNames().getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getNames().getTechnicalName(), classModel.getNames().getConceptualName(),
				"Unnamed class");
	}

	private String resolveFieldName(final FieldModel fieldModel) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			return this.blankToFallback(fieldModel.getNames().getName(), fieldModel.getNames().getTechnicalName(),
					"Unnamed field");
		}
		return this.blankToFallback(fieldModel.getNames().getTechnicalName(), fieldModel.getNames().getName(),
				"Unnamed field");
	}

	private String resolveCommentText(final CommentModel commentModel) {
		if (commentModel.getKind() == CommentKind.STANDALONE) {
			return commentModel.getText();
		}

		if (commentModel.getBinding() == null) {
			return "";
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			return classModel == null ? "" : classModel.getComment();
		}

		final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
		return linkModel == null ? "" : linkModel.getComment();
	}

	private boolean isCommentVisible(final CommentModel commentModel) {
		if (commentModel.getKind() == CommentKind.STANDALONE) {
			return true;
		}

		if (commentModel.getBinding() == null) {
			return false;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			return classModel != null && this.isVisible(classModel);
		}

		final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
		if (linkModel == null) {
			return false;
		}

		final Graphics2D g2 = this.createGraphicsContext();
		try {
			return this.resolveLinkGeometry(g2, linkModel) != null;
		} finally {
			g2.dispose();
		}
	}

	private boolean isVisible(final ClassModel classModel) {
		return switch (this.panelType) {
		case CONCEPTUAL -> classModel.getVisibility().isConceptual();
		case LOGICAL -> classModel.getVisibility().isLogical();
		case PHYSICAL -> classModel.getVisibility().isPhysical();
		};
	}

	private PanelState getPanelState() {
		return this.document.getWorkspace().getPanels().get(this.panelType);
	}

	private ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	private FieldModel findFieldById(final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return null;
		}

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.getId().equals(fieldId)) {
				return fieldModel;
			}
		}

		return null;
	}

	private LinkModel findLinkById(final String id) {
		for (final LinkModel linkModel : this.document.getModel().getLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		return null;
	}

	private CommentModel findCommentById(final String commentId) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			if (commentModel.getId().equals(commentId)) {
				return commentModel;
			}
		}
		return null;
	}

	private String getEditableClassName(final ClassModel classModel) {
		return this.panelType == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
				: classModel.getNames().getTechnicalName();
	}

	private void setEditableClassName(final ClassModel classModel, final String value) {
		if (classModel == null) {
			return;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			classModel.getNames().setConceptualName(value);
		} else {
			classModel.getNames().setTechnicalName(value);
		}
	}

	private String getEditableFieldName(final FieldModel fieldModel) {
		return this.panelType == PanelType.CONCEPTUAL ? fieldModel.getNames().getName()
				: fieldModel.getNames().getTechnicalName();
	}

	private void setEditableFieldName(final FieldModel fieldModel, final String value) {
		if (fieldModel == null) {
			return;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			fieldModel.getNames().setName(value);
		} else {
			fieldModel.getNames().setTechnicalName(value);
		}
	}

	private String getEditableCommentText(final String commentId) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null) {
			return "";
		}

		if (commentModel.getKind() == CommentKind.STANDALONE) {
			return commentModel.getText();
		}

		if (commentModel.getBinding() != null && commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			return classModel == null ? "" : classModel.getComment();
		}

		if (commentModel.getBinding() != null) {
			final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
			return linkModel == null ? "" : linkModel.getComment();
		}

		return "";
	}

	private void setEditableCommentText(final String commentId, final String value) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		if (commentModel.getKind() == CommentKind.STANDALONE) {
			commentModel.setText(value);
			return;
		}

		if (commentModel.getBinding() != null && commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			if (classModel != null) {
				classModel.setComment(value);
			}
			return;
		}

		if (commentModel.getBinding() != null) {
			final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
			if (linkModel != null) {
				linkModel.setComment(value);
			}
		}
	}

	private Point2 screenToWorld(final Point point) {
		final PanelState state = this.getPanelState();
		return new Point2((point.getX() - state.getPanX()) / state.getZoom(),
				(point.getY() - state.getPanY()) / state.getZoom());
	}

	private double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(max, value));
	}

	private String blankToFallback(final String primary, final String secondary, final String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary;
		}
		if (secondary != null && !secondary.isBlank()) {
			return secondary;
		}
		return fallback;
	}

	private LinkGeometry resolveLinkGeometry(final Graphics2D g2, final LinkModel linkModel) {
		final Point2D fromPoint;
		final Point2D toPoint;

		if (this.panelType == PanelType.CONCEPTUAL) {
			fromPoint = this.resolveFieldAnchor(g2, linkModel.getFrom().getClassId(), null, true);
			toPoint = this.resolveFieldAnchor(g2, linkModel.getTo().getClassId(), null, false);
		} else {
			fromPoint = this.resolveFieldAnchor(g2, linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId(),
					true);
			toPoint = this.resolveFieldAnchor(g2, linkModel.getTo().getClassId(), linkModel.getTo().getFieldId(),
					false);
		}

		if (fromPoint == null || toPoint == null) {
			return null;
		}

		final List<Point2D> points = new ArrayList<>();
		points.add(fromPoint);

		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkModel.getId());
		for (final Point2 bendPoint : linkLayout.getBendPoints()) {
			points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
		}

		points.add(toPoint);

		final Point2D labelPoint;
		if (linkLayout.getNameLabelPosition() != null) {
			labelPoint = new Point2D.Double(linkLayout.getNameLabelPosition().getX(),
					linkLayout.getNameLabelPosition().getY());
		} else {
			labelPoint = new Point2D.Double((fromPoint.getX() + toPoint.getX()) / 2.0,
					(fromPoint.getY() + toPoint.getY()) / 2.0);
		}

		return new LinkGeometry(fromPoint, toPoint, labelPoint, points);
	}

	private Point2D resolveFieldAnchor(final Graphics2D g2, final String classId, final String fieldId,
			final boolean fromSide) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
		final double x = fromSide ? bounds.getMaxX() : bounds.getX();

		if (fieldId == null) {
			return new Point2D.Double(x, bounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final double y = bounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT
						+ DiagramCanvas.ROW_HEIGHT / 2.0;
				return new Point2D.Double(x, y);
			}
		}

		return new Point2D.Double(x, bounds.getCenterY());
	}

	private boolean isClassSelected(final String classId) {
		return this.selectedElement != null && this.selectedElement.type() == SelectedType.CLASS
				&& classId.equals(this.selectedElement.classId());
	}

	private boolean isFieldSelected(final String classId, final String fieldId) {
		return this.selectedElement != null && this.selectedElement.type() == SelectedType.FIELD
				&& classId.equals(this.selectedElement.classId()) && fieldId.equals(this.selectedElement.fieldId());
	}

	private boolean isCommentSelected(final String commentId) {
		return this.selectedElement != null && this.selectedElement.type() == SelectedType.COMMENT
				&& commentId.equals(this.selectedElement.commentId());
	}

	private boolean isLinkSelected(final String linkId) {
		return this.selectedElement != null && this.selectedElement.type() == SelectedType.LINK
				&& linkId.equals(this.selectedElement.linkId());
	}

	private void select(final SelectedElement element) {
		this.selectedElement = element;
		this.notifySelectionChanged();
		this.repaint();
	}

	private void clearSelection() {
		this.selectedElement = null;
		this.notifySelectionChanged();
		this.repaint();
	}

	private void notifySelectionChanged() {
		if (this.statusListener != null) {
			this.statusListener.onSelectionChanged(this.getSelectionInfo());
		}
	}

	private String buildSelectionPath() {
		if (this.selectedElement == null) {
			return "";
		}

		switch (this.selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = this.findClassById(this.selectedElement.classId());
			return classModel == null ? "" : this.resolveClassTitle(classModel);
		}
		case FIELD -> {
			final ClassModel classModel = this.findClassById(this.selectedElement.classId());
			final FieldModel fieldModel = this.findFieldById(this.selectedElement.classId(),
					this.selectedElement.fieldId());
			if (classModel == null || fieldModel == null) {
				return "";
			}
			return this.resolveClassTitle(classModel) + " > " + this.resolveFieldName(fieldModel);
		}
		case COMMENT -> {
			final CommentModel commentModel = this.findCommentById(this.selectedElement.commentId());
			if (commentModel == null) {
				return "";
			}

			if (commentModel.getKind() == CommentKind.STANDALONE) {
				return "Comment";
			}

			if (commentModel.getBinding() != null
					&& commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
				final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
				return classModel == null ? "Comment" : this.resolveClassTitle(classModel) + " > comment";
			}

			final LinkModel linkModel = commentModel.getBinding() == null ? null
					: this.findLinkById(commentModel.getBinding().getTargetId());
			return linkModel == null ? "Comment" : this.buildLinkPath(linkModel) + " > comment";
		}
		case LINK -> {
			final LinkModel linkModel = this.findLinkById(this.selectedElement.linkId());
			return linkModel == null ? "" : this.buildLinkPath(linkModel);
		}
		default -> {
			return "";
		}
		}
	}

	private String buildLinkPath(final LinkModel linkModel) {
		final ClassModel fromClass = this.findClassById(linkModel.getFrom().getClassId());
		final ClassModel toClass = this.findClassById(linkModel.getTo().getClassId());

		final String fromName = fromClass == null ? "?" : this.resolveClassTitle(fromClass);
		final String toName = toClass == null ? "?" : this.resolveClassTitle(toClass);

		if (this.panelType == PanelType.CONCEPTUAL) {
			String middle = linkModel.getName() == null || linkModel.getName().isBlank() ? "link" : linkModel.getName();

			if (linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank()) {
				final ClassModel associationClass = this.findClassById(linkModel.getAssociationClassId());
				middle += "[" + (associationClass == null ? linkModel.getAssociationClassId()
						: this.resolveClassTitle(associationClass)) + "]";
			}

			return fromName + " > " + middle + " < " + toName;
		}

		final FieldModel fromField = this.findFieldById(linkModel.getFrom().getClassId(),
				linkModel.getFrom().getFieldId());
		final FieldModel toField = this.findFieldById(linkModel.getTo().getClassId(), linkModel.getTo().getFieldId());

		final String fromFieldName = fromField == null ? "?" : this.resolveFieldName(fromField);
		final String toFieldName = toField == null ? "?" : this.resolveFieldName(toField);

		return fromName + " > " + fromFieldName + " -> " + toFieldName + " < " + toName;
	}

	private Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
	}

	private Color withAlpha(final Color color, final int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private record DraggedNode(NodeLayout layout, double offsetX, double offsetY) {
	}

	private record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
	}

	private record FieldHitResult(FieldModel field, Rectangle2D bounds) {
	}

	private record LinkCreationState(String classId, String fieldId) {
	}

	private record LinkGeometry(Point2D fromPoint, Point2D toPoint, Point2D labelPoint, List<Point2D> points) {
	}

	private record ResizingComment(NodeLayout layout, double initialWidth, double initialHeight, double startWorldX,
			double startWorldY) {
	}

	private enum SelectedType {
		NONE, CLASS, FIELD, COMMENT, LINK
	}

	private record SelectedElement(SelectedType type, String classId, String fieldId, String commentId, String linkId) {
		private static SelectedElement forClass(final String classId) {
			return new SelectedElement(SelectedType.CLASS, classId, null, null, null);
		}

		private static SelectedElement forField(final String classId, final String fieldId) {
			return new SelectedElement(SelectedType.FIELD, classId, fieldId, null, null);
		}

		private static SelectedElement forComment(final String commentId) {
			return new SelectedElement(SelectedType.COMMENT, null, null, commentId, null);
		}

		private static SelectedElement forLink(final String linkId) {
			return new SelectedElement(SelectedType.LINK, null, null, null, linkId);
		}
	}
}