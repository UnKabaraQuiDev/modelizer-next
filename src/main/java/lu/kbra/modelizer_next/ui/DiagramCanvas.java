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

import lu.kbra.modelizer_next.common.Size2;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
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
	private static final Color SELECTION_COLOR = new Color(0x2F7DFF);

	private static final double LINK_HIT_DISTANCE = 6.0;

	private final ModelDocument document;
	private final PanelType panelType;
	private final CanvasStatusListener statusListener;

	private DraggedNode draggedNode;
	private Point lastScreenPoint;
	private boolean panning;

	private LinkCreationState linkCreationState;
	private Point2D.Double currentLinkPreviewPoint;

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

		this.drawComments(g2);
		this.drawClasses(g2);
		this.drawLinks(g2);
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

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK),
				"addTable");
		this.getActionMap().put("addTable", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addTable();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
				"addField");
		this.getActionMap().put("addField", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addField();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
				"addComment");
		this.getActionMap().put("addComment", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addComment();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelection");
		this.getActionMap().put("deleteSelection", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.deleteSelection();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
				"duplicateSelection");
		this.getActionMap().put("duplicateSelection", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.duplicateSelection();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection");
		this.getActionMap().put("clearSelection", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.clearSelection();
			}
		});
	}

	private void duplicateSelection() {
		if (this.selectedElement == null) {
			return;
		}

		switch (this.selectedElement.type()) {
		case CLASS -> this.duplicateClass(this.selectedElement.classId());
		case FIELD -> this.duplicateField(this.selectedElement.classId(), this.selectedElement.fieldId());
		case COMMENT -> this.duplicateComment(this.selectedElement.commentId());
		case LINK -> this.duplicateLink(this.selectedElement.linkId());
		default -> {
			return;
		}
		}

		this.notifySelectionChanged();
		this.repaint();
	}

	private void duplicateClass(final String classId) {
		final ClassModel source = this.findClassById(classId);
		if (source == null) {
			return;
		}

		final ClassModel copy = new ClassModel();
		copy.getNames().setConceptualName(source.getNames().getConceptualName() + " Copy");
		copy.getNames().setTechnicalName(source.getNames().getTechnicalName() + "_COPY");
		copy.setComment(source.getComment());
		copy.setGroup(source.getGroup());
		copy.getVisibility().setConceptual(source.getVisibility().isConceptual());
		copy.getVisibility().setLogical(source.getVisibility().isLogical());
		copy.getVisibility().setPhysical(source.getVisibility().isPhysical());
		copy.getStyle().setTextColor(source.getStyle().getTextColor());
		copy.getStyle().setBackgroundColor(source.getStyle().getBackgroundColor());
		copy.getStyle().setBorderColor(source.getStyle().getBorderColor());

		for (final FieldModel sourceField : source.getFields()) {
			final FieldModel fieldCopy = new FieldModel();
			fieldCopy.getNames().setName(sourceField.getNames().getName());
			fieldCopy.getNames().setTechnicalName(sourceField.getNames().getTechnicalName());
			fieldCopy.setNotConceptual(sourceField.isNotConceptual());
			fieldCopy.setComment(sourceField.getComment());
			fieldCopy.setPrimaryKey(sourceField.isPrimaryKey());
			fieldCopy.setUnique(sourceField.isUnique());
			fieldCopy.setNotNull(sourceField.isNotNull());
			fieldCopy.getStyle().setTextColor(sourceField.getStyle().getTextColor());
			fieldCopy.getStyle().setBackgroundColor(sourceField.getStyle().getBackgroundColor());
			copy.getFields().add(fieldCopy);
		}

		this.document.getModel().getClasses().add(copy);

		final NodeLayout sourceLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, source.getId());
		final NodeLayout copyLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, copy.getId());
		copyLayout.setPosition(
				new Point2D.Double(sourceLayout.getPosition().getX() + 30, sourceLayout.getPosition().getY() + 30));
		copyLayout.setSize(new Size2(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

		this.select(SelectedElement.forClass(copy.getId()));
	}

	private void duplicateField(final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		final FieldModel source = this.findFieldById(classId, fieldId);
		if (classModel == null || source == null) {
			return;
		}

		final FieldModel copy = new FieldModel();
		copy.getNames().setName(source.getNames().getName() + " Copy");
		copy.getNames().setTechnicalName(source.getNames().getTechnicalName() + "_COPY");
		copy.setNotConceptual(source.isNotConceptual());
		copy.setComment(source.getComment());
		copy.setPrimaryKey(source.isPrimaryKey());
		copy.setUnique(source.isUnique());
		copy.setNotNull(source.isNotNull());
		copy.getStyle().setTextColor(source.getStyle().getTextColor());
		copy.getStyle().setBackgroundColor(source.getStyle().getBackgroundColor());

		int insertIndex = classModel.getFields().indexOf(source);
		if (insertIndex < 0) {
			classModel.getFields().add(copy);
		} else {
			classModel.getFields().add(insertIndex + 1, copy);
		}

		this.select(SelectedElement.forField(classId, copy.getId()));
	}

	private void duplicateComment(final String commentId) {
		final CommentModel source = this.findCommentById(commentId);
		if (source == null) {
			return;
		}

		final CommentModel copy = new CommentModel();
		copy.setKind(source.getKind());
		copy.setText(source.getText());
		copy.setBinding(source.getBinding() == null ? null
				: new CommentBinding(source.getBinding().getTargetType(), source.getBinding().getTargetId()));
		copy.setTextColor(source.getTextColor());
		copy.setBackgroundColor(source.getBackgroundColor());
		copy.setBorderColor(source.getBorderColor());

		this.document.getModel().getComments().add(copy);

		final NodeLayout sourceLayout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, source.getId());
		final NodeLayout copyLayout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, copy.getId());
		copyLayout.setPosition(
				new Point2D.Double(sourceLayout.getPosition().getX() + 30, sourceLayout.getPosition().getY() + 30));
		copyLayout.setSize(new Size2(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

		this.select(SelectedElement.forComment(copy.getId()));
	}

	private void duplicateLink(final String linkId) {
		final LinkModel source = this.findLinkById(linkId);
		if (source == null) {
			return;
		}

		final LinkModel copy = new LinkModel();
		copy.setName(source.getName());
		copy.setComment(source.getComment());
		copy.setLineColor(source.getLineColor());
		copy.setAssociationClassId(source.getAssociationClassId());
		copy.setFrom(new LinkEnd(source.getFrom().getClassId(), source.getFrom().getFieldId()));
		copy.setTo(new LinkEnd(source.getTo().getClassId(), source.getTo().getFieldId()));
		copy.setCardinalityFrom(source.getCardinalityFrom());
		copy.setCardinalityTo(source.getCardinalityTo());

		if (panelType == PanelType.CONCEPTUAL) {
			document.getModel().getConceptualLinks().add(copy);
		} else {
			document.getModel().getTechnicalLinks().add(copy);
		}

		final LinkLayout sourceLayout = this.findOrCreateLinkLayout(source.getId());
		final LinkLayout copyLayout = this.findOrCreateLinkLayout(copy.getId());
		copyLayout.getBendPoints().clear();
		for (final Point2D.Double bendPoint : sourceLayout.getBendPoints()) {
			copyLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + 20, bendPoint.getY() + 20));
		}
		if (sourceLayout.getNameLabelPosition() != null) {
			copyLayout.setNameLabelPosition(new Point2D.Double(sourceLayout.getNameLabelPosition().getX() + 20,
					sourceLayout.getNameLabelPosition().getY() + 20));
		}

		this.select(SelectedElement.forLink(copy.getId()));
	}

	private void deleteSelection() {
		if (this.selectedElement == null) {
			return;
		}

		switch (this.selectedElement.type()) {
		case CLASS -> this.deleteClass(this.selectedElement.classId());
		case FIELD -> this.deleteField(this.selectedElement.classId(), this.selectedElement.fieldId());
		case COMMENT -> this.deleteComment(this.selectedElement.commentId());
		case LINK -> this.deleteLink(this.selectedElement.linkId());
		default -> {
			return;
		}
		}

		this.clearSelection();
		this.repaint();
	}

	private void deleteClass(final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return;
		}

		this.document.getModel().getClasses().remove(classModel);
		this.getPanelState().getNodeLayouts().removeIf(
				layout -> layout.getObjectType() == LayoutObjectType.CLASS && layout.getObjectId().equals(classId));

		this.getActiveLinks().removeIf(link -> classId.equals(link.getFrom().getClassId())
				|| classId.equals(link.getTo().getClassId()) || classId.equals(link.getAssociationClassId()));

		this.document.getModel().getConceptualLinks().removeIf(link -> classId.equals(link.getFrom().getClassId())
				|| classId.equals(link.getTo().getClassId()) || classId.equals(link.getAssociationClassId()));
		this.document.getModel().getTechnicalLinks().removeIf(link -> classId.equals(link.getFrom().getClassId())
				|| classId.equals(link.getTo().getClassId()) || classId.equals(link.getAssociationClassId()));

		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> this.findLinkById(linkLayout.getLinkId()) == null);
	}

	private void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return;
		}

		classModel.getFields().removeIf(field -> field.getId().equals(fieldId));
		this.document.getModel().getTechnicalLinks().removeIf(
				link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()));
		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> this.findLinkById(linkLayout.getLinkId()) == null);
	}

	private void deleteComment(final String commentId) {
		this.document.getModel().getComments().removeIf(comment -> comment.getId().equals(commentId));
		this.getPanelState().getNodeLayouts().removeIf(
				layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	private void deleteLink(final String linkId) {
		this.getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getConceptualLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getTechnicalLinks().removeIf(link -> link.getId().equals(linkId));
		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

	private void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		classModel.getNames().setTechnicalName("NEW_TABLE");

		this.document.getModel().getClasses().add(classModel);

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
		final Point2D.Double center = this.viewportCenterWorld();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2(180, 0));

		this.select(SelectedElement.forClass(classModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addField() {
		final ClassModel targetClass;

		if (this.selectedElement != null && this.selectedElement.type() == SelectedType.CLASS) {
			targetClass = this.findClassById(this.selectedElement.classId());
		} else if (this.selectedElement != null && this.selectedElement.type() == SelectedType.FIELD) {
			targetClass = this.findClassById(this.selectedElement.classId());
		} else {
			return;
		}

		if (targetClass == null) {
			return;
		}

		final FieldModel fieldModel = new FieldModel();
		fieldModel.getNames().setName("New field");
		fieldModel.getNames().setTechnicalName("NEW_FIELD");
		targetClass.getFields().add(fieldModel);

		this.select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");

		this.document.getModel().getComments().add(commentModel);

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
		final Point2D.Double center = this.viewportCenterWorld();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2(220, 80));

		this.select(SelectedElement.forComment(commentModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private Point2D.Double viewportCenterWorld() {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((this.getWidth() / 2.0 - state.getPanX()) / state.getZoom(),
				(this.getHeight() / 2.0 - state.getPanY()) / state.getZoom());
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

		final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
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

	private boolean isInCommentResizeHandle(final Rectangle2D bounds, final Point2D.Double worldPoint) {
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

		final FieldEditorDialog.Result result = FieldEditorDialog.showDialog(this, fieldModel, moveDelta -> {
			this.moveSelectedFieldInList(moveDelta);
			this.notifySelectionChanged();
			this.repaint();
		});
		if (result == null) {
			return;
		}

		fieldModel.getNames().setName(result.name());
		fieldModel.getNames().setTechnicalName(result.technicalName());
		fieldModel.setPrimaryKey(result.primaryKey());
		fieldModel.setUnique(result.unique());
		fieldModel.setNotNull(result.notNull());
		fieldModel.getStyle().setTextColor(result.textColor());
		fieldModel.getStyle().setBackgroundColor(result.backgroundColor());

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
			final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
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

		final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
		this.draggedNode.layout().getPosition().setLocation(worldPoint.getX() - this.draggedNode.offsetX(),
				worldPoint.getY() - this.draggedNode.offsetY());
		this.repaint();
	}

	private void handleMouseWheelMoved(final MouseWheelEvent event) {
		final PanelState state = this.getPanelState();
		final Point2D.Double worldBefore = this.screenToWorld(event.getPoint());

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
			if (panelType != PanelType.CONCEPTUAL) {
				return;
			}
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

//			if (draggedNode == null || !commentModel.getId().equals(draggedNode.layout().getObjectId())) {
			g2.setFont(DiagramCanvas.BODY_FONT);
			g2.setColor(commentModel.getTextColor());
			this.drawMultilineText(g2, commentText, bounds, DiagramCanvas.PADDING);
//			}

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

		for (final LinkModel linkModel : this.getActiveLinks()) {
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

			if (this.panelType == PanelType.CONCEPTUAL && linkModel.getName() != null
					&& !linkModel.getName().isBlank()) {
				this.drawAlignedLinkLabel(g2, linkModel.getName(), geometry.labelPoint(), geometry.labelAngle());
			}

			if (this.panelType == PanelType.CONCEPTUAL) {
				if (linkModel.getCardinalityFrom() != null) {
					this.drawCardinalityLabel(g2, linkModel.getCardinalityFrom().getDisplayValue(),
							geometry.fromPoint(), geometry.points().get(1), geometry.labelAngle());
				}
				if (linkModel.getCardinalityTo() != null) {
					this.drawCardinalityLabel(g2, linkModel.getCardinalityTo().getDisplayValue(), geometry.toPoint(),
							geometry.points().get(geometry.points().size() - 2), geometry.labelAngle());
				}
			}
		}
	}

	private void drawCardinalityLabel(final Graphics2D g2, final String text, final Point2D anchor,
			final Point2D adjacentPoint, final double angle) {
		final double dx = adjacentPoint.getX() - anchor.getX();
		final double dy = adjacentPoint.getY() - anchor.getY();

		double ux = 0.0;
		double uy = 0.0;
		final double length = Math.hypot(dx, dy);
		if (length > 0.0) {
			ux = dx / length;
			uy = dy / length;
		}

		final double alongOffset = 16.0;

		final Point2D center = new Point2D.Double(anchor.getX() + ux * alongOffset, anchor.getY() + uy * alongOffset);

		this.drawAlignedLinkLabel(g2, text, center, angle);
	}

	private void drawAlignedLinkLabel(final Graphics2D g2, final String text, final Point2D center,
			final double angle) {
		final Graphics2D labelGraphics = (Graphics2D) g2.create();
		try {
			final FontMetrics metrics = labelGraphics.getFontMetrics();
			final Rectangle2D textBounds = metrics.getStringBounds(text, labelGraphics);

			final double normalX = -Math.sin(angle);
			final double normalY = Math.cos(angle);
			final double offset = -8;

			labelGraphics.translate(center.getX() + normalX * offset, center.getY() + normalY * offset);
			labelGraphics.rotate(angle);

			labelGraphics.setColor(g2.getColor());
			labelGraphics.drawString(text, (float) (-textBounds.getWidth() / 2.0),
					(float) (metrics.getAscent() - textBounds.getHeight() / 2.0));
		} finally {
			labelGraphics.dispose();
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
			final ClassModel classModel = this.findClassById(this.linkCreationState.classId());
			if (classModel == null || !this.isVisible(classModel)) {
				return;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);

			AnchorCandidate bestCandidate = null;
			double bestDistance = Double.POSITIVE_INFINITY;

			for (final AnchorCandidate candidate : this.computeConceptualCandidates(g2, new LinkModel(),
					classModel.getId(), bounds)) {
				final double distance = candidate.point().distance(this.currentLinkPreviewPoint);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestCandidate = candidate;
				}
			}

			if (bestCandidate == null) {
				return;
			}

			fromAnchor = bestCandidate.point();
		} else {
			fromAnchor = this.resolveTechnicalFieldAnchor(g2, this.linkCreationState.classId(),
					this.linkCreationState.fieldId(), null, null);
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
		return geometry == null ? null : geometry.middlePoint();
	}

	private void finishLinkCreation(final Point2D.Double worldPoint) {
		final HitResult hitResult = this.findTopmostHit(worldPoint);
		if (hitResult == null || this.linkCreationState == null) {
			return;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			if (hitResult.selection().type() != SelectedType.FIELD) {
				return;
			}

			if (this.linkCreationState.classId().equals(hitResult.selection().classId())
					&& Objects.equals(this.linkCreationState.fieldId(), hitResult.selection().fieldId())) {
				return;
			}

			final FieldModel sourceField = this.findFieldById(this.linkCreationState.classId(),
					this.linkCreationState.fieldId());
			final FieldModel targetField = this.findFieldById(hitResult.selection().classId(),
					hitResult.selection().fieldId());

			if (sourceField == null || targetField == null) {
				return;
			}

			if (!targetField.isPrimaryKey()) {
				return;
			}

			if (this.hasOutgoingTechnicalLink(this.linkCreationState.classId(), this.linkCreationState.fieldId())) {
				return;
			}

			final LinkModel linkModel = new LinkModel();
			linkModel.setFrom(new LinkEnd(this.linkCreationState.classId(), this.linkCreationState.fieldId()));
			linkModel.setTo(new LinkEnd(hitResult.selection().classId(), hitResult.selection().fieldId()));
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);

			this.document.getModel().getConceptualLinks().add(linkModel);
			this.findOrCreateLinkLayout(linkModel.getId());
			this.select(SelectedElement.forLink(linkModel.getId()));
		} else if (panelType != PanelType.CONCEPTUAL) {

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

			this.document.getModel().getTechnicalLinks().add(linkModel);
			this.findOrCreateLinkLayout(linkModel.getId());
			this.select(SelectedElement.forLink(linkModel.getId()));
		}
	}

	private boolean hasOutgoingTechnicalLink(final String classId, final String fieldId) {
		for (final LinkModel linkModel : this.document.getModel().getTechnicalLinks()) {
			if (linkModel.getFrom() == null || linkModel.getTo() == null) {
				continue;
			}

			if (linkModel.getFrom().getFieldId() == null || linkModel.getTo().getFieldId() == null) {
				continue;
			}

			if (Objects.equals(linkModel.getFrom().getClassId(), classId)
					&& Objects.equals(linkModel.getFrom().getFieldId(), fieldId)) {
				return true;
			}
		}
		return false;
	}

	private HitResult findTopmostHit(final Point2D.Double worldPoint) {
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

			for (int i = this.getActiveLinks().size() - 1; i >= 0; i--) {
				final LinkModel linkModel = this.getActiveLinks().get(i);
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
			final Point2D.Double worldPoint) {
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

	private boolean isPointNearGeometry(final Point2D.Double worldPoint, final LinkGeometry geometry) {
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
		layout.setPosition(new Point2D.Double(80 + this.getPanelState().getNodeLayouts().size() * 30,
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
		final String baseName;
		if (this.panelType == PanelType.CONCEPTUAL) {
			baseName = blankToFallback(fieldModel.getNames().getName(), fieldModel.getNames().getTechnicalName(),
					"Unnamed field");
		} else {
			baseName = blankToFallback(fieldModel.getNames().getTechnicalName(), fieldModel.getNames().getName(),
					"Unnamed field");
		}

		if (this.panelType != PanelType.PHYSICAL) {
			return baseName;
		}

		final List<String> flags = new ArrayList<>();
		if (fieldModel.isPrimaryKey()) {
			flags.add("PK");
		}
		if (fieldModel.isUnique()) {
			flags.add("UQ");
		}
		if (fieldModel.isNotNull()) {
			flags.add("NN");
		}

		if (flags.isEmpty()) {
			return baseName;
		}

		return baseName + " [" + String.join(", ", flags) + "]";
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
		for (final LinkModel linkModel : this.document.getModel().getConceptualLinks()) {
			if (linkModel.getId().equals(id)) {
				return linkModel;
			}
		}
		for (final LinkModel linkModel : this.document.getModel().getTechnicalLinks()) {
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

	private Point2D.Double screenToWorld(final Point point) {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((point.getX() - state.getPanX()) / state.getZoom(),
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
			final AnchorPair anchorPair = this.resolveConceptualAnchorPair(g2, linkModel);
			if (anchorPair == null) {
				return null;
			}
			fromPoint = anchorPair.from();
			toPoint = anchorPair.to();
		} else {
			fromPoint = this.resolveTechnicalFieldAnchor(g2, linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId(), linkModel.getTo().getClassId(), linkModel.getTo().getFieldId());
			toPoint = this.resolveTechnicalFieldAnchor(g2, linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId(), linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId());
		}

		if (fromPoint == null || toPoint == null) {
			return null;
		}

		final List<Point2D> points = new ArrayList<>();
		points.add(fromPoint);

		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkModel.getId());
		for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
			points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
		}

		points.add(toPoint);

		final Point2D middlePoint = this.computePolylineMiddlePoint(points);
		final double labelAngle = this.computeUprightAngleAtMiddle(points);

		final Point2D labelPoint;
		if (linkLayout.getNameLabelPosition() != null) {
			labelPoint = new Point2D.Double(linkLayout.getNameLabelPosition().getX(),
					linkLayout.getNameLabelPosition().getY());
		} else {
			labelPoint = middlePoint;
		}

		return new LinkGeometry(fromPoint, toPoint, labelPoint, middlePoint, labelAngle, points);
	}

	private Point2D computePolylineMiddlePoint(final List<Point2D> points) {
		if (points == null || points.size() < 2) {
			return null;
		}

		double totalLength = 0.0;
		for (int i = 0; i < points.size() - 1; i++) {
			totalLength += points.get(i).distance(points.get(i + 1));
		}

		if (totalLength <= 0.0) {
			return new Point2D.Double(points.get(0).getX(), points.get(0).getY());
		}

		final double halfLength = totalLength / 2.0;
		double walked = 0.0;

		for (int i = 0; i < points.size() - 1; i++) {
			final Point2D a = points.get(i);
			final Point2D b = points.get(i + 1);
			final double segmentLength = a.distance(b);

			if (walked + segmentLength >= halfLength) {
				final double remaining = halfLength - walked;
				final double t = segmentLength == 0.0 ? 0.0 : remaining / segmentLength;
				return new Point2D.Double(a.getX() + (b.getX() - a.getX()) * t, a.getY() + (b.getY() - a.getY()) * t);
			}

			walked += segmentLength;
		}

		final Point2D last = points.get(points.size() - 1);
		return new Point2D.Double(last.getX(), last.getY());
	}

	private double computeUprightAngleAtMiddle(final List<Point2D> points) {
		if (points == null || points.size() < 2) {
			return 0.0;
		}

		double totalLength = 0.0;
		for (int i = 0; i < points.size() - 1; i++) {
			totalLength += points.get(i).distance(points.get(i + 1));
		}

		if (totalLength <= 0.0) {
			return 0.0;
		}

		final double halfLength = totalLength / 2.0;
		double walked = 0.0;

		for (int i = 0; i < points.size() - 1; i++) {
			final Point2D a = points.get(i);
			final Point2D b = points.get(i + 1);
			final double segmentLength = a.distance(b);

			if (walked + segmentLength >= halfLength) {
				double angle = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

				if (angle > Math.PI / 2.0) {
					angle -= Math.PI;
				} else if (angle <= -Math.PI / 2.0) {
					angle += Math.PI;
				}

				return angle;
			}

			walked += segmentLength;
		}

		final Point2D a = points.get(points.size() - 2);
		final Point2D b = points.get(points.size() - 1);
		double angle = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

		if (angle > Math.PI / 2.0) {
			angle -= Math.PI;
		} else if (angle <= -Math.PI / 2.0) {
			angle += Math.PI;
		}

		return angle;
	}

	private Point2D resolveTechnicalFieldAnchor(final Graphics2D g2, final String classId, final String fieldId,
			final String oppositeClassId, final String oppositeFieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);

		final Point2D oppositeReference = this.resolveOppositeReferencePoint(g2, oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return null;
		}

		if (fieldId == null) {
			final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
			final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
			return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + HEADER_HEIGHT + i * ROW_HEIGHT, classBounds.getWidth(), ROW_HEIGHT);

				final Point2D left = new Point2D.Double(fieldBounds.getX(), fieldBounds.getCenterY());
				final Point2D right = new Point2D.Double(fieldBounds.getMaxX(), fieldBounds.getCenterY());

				return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
			}
		}

		final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
		final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
		return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
	}

	private Point2D resolveOppositeReferencePoint(final Graphics2D g2, final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);

		if (this.panelType == PanelType.CONCEPTUAL || fieldId == null) {
			return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final double y = classBounds.getY() + HEADER_HEIGHT + i * ROW_HEIGHT + ROW_HEIGHT / 2.0;
				return new Point2D.Double(classBounds.getCenterX(), y);
			}
		}

		return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
	}

	private Point2D resolveTechnicalFieldAnchor(final Graphics2D g2, final String classId, final String fieldId,
			final Point2D oppositeReference) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);

		if (fieldId == null) {
			final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX()
					: classBounds.getMaxX();
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + HEADER_HEIGHT + i * ROW_HEIGHT, classBounds.getWidth(), ROW_HEIGHT);
				final double x = oppositeReference.getX() < fieldBounds.getCenterX() ? fieldBounds.getX()
						: fieldBounds.getMaxX();
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX()
				: classBounds.getMaxX();
		return new Point2D.Double(x, classBounds.getCenterY());
	}

	private AnchorPair resolveConceptualAnchorPair(final Graphics2D g2, final LinkModel targetLink) {
		final ClassModel fromClass = this.findClassById(targetLink.getFrom().getClassId());
		final ClassModel toClass = this.findClassById(targetLink.getTo().getClassId());

		if (fromClass == null || toClass == null || !this.isVisible(fromClass) || !this.isVisible(toClass)) {
			return null;
		}

		final NodeLayout fromLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, fromClass.getId());
		final NodeLayout toLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, toClass.getId());

		final Rectangle2D fromBounds = this.computeClassBounds(g2, fromClass, fromLayout);
		final Rectangle2D toBounds = this.computeClassBounds(g2, toClass, toLayout);

		AnchorCandidate bestFrom = null;
		AnchorCandidate bestTo = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (final AnchorCandidate fromCandidate : this.computeConceptualCandidates(g2, targetLink, fromClass.getId(),
				fromBounds)) {
			for (final AnchorCandidate toCandidate : this.computeConceptualCandidates(g2, targetLink, toClass.getId(),
					toBounds)) {
				final double distance = fromCandidate.point().distance(toCandidate.point());
				if (distance < bestDistance) {
					bestDistance = distance;
					bestFrom = fromCandidate;
					bestTo = toCandidate;
				}
			}
		}

		if (bestFrom == null || bestTo == null) {
			return null;
		}

		return new AnchorPair(bestFrom.point(), bestTo.point());
	}

	private List<AnchorCandidate> computeConceptualCandidates(final Graphics2D g2, final LinkModel targetLink,
			final String classId, final Rectangle2D bounds) {
		final List<AnchorCandidate> candidates = new ArrayList<>();

		for (final AnchorSide side : AnchorSide.values()) {
			final double offset = this.computeConceptualSideOffset(g2, targetLink, classId, side);
			final Point2D point = switch (side) {
			case TOP -> new Point2D.Double(
					clamp(bounds.getCenterX() + offset, bounds.getX() + 12, bounds.getMaxX() - 12), bounds.getY());
			case BOTTOM -> new Point2D.Double(
					clamp(bounds.getCenterX() + offset, bounds.getX() + 12, bounds.getMaxX() - 12), bounds.getMaxY());
			case LEFT -> new Point2D.Double(bounds.getX(),
					clamp(bounds.getCenterY() + offset, bounds.getY() + 12, bounds.getMaxY() - 12));
			case RIGHT -> new Point2D.Double(bounds.getMaxX(),
					clamp(bounds.getCenterY() + offset, bounds.getY() + 12, bounds.getMaxY() - 12));
			};

			candidates.add(new AnchorCandidate(side, point));
		}

		return candidates;
	}

	private double computeConceptualSideOffset(final Graphics2D g2, final LinkModel targetLink, final String classId,
			final AnchorSide side) {
		final List<LinkSlot> slots = new ArrayList<>();

		for (final LinkModel linkModel : this.getActiveLinks()) {
			if (this.panelType != PanelType.CONCEPTUAL) {
				continue;
			}

			final boolean matchesFrom = classId.equals(linkModel.getFrom().getClassId());
			final boolean matchesTo = classId.equals(linkModel.getTo().getClassId());
			if (!matchesFrom && !matchesTo) {
				continue;
			}

			final String otherClassId = matchesFrom ? linkModel.getTo().getClassId() : linkModel.getFrom().getClassId();
			final ClassModel classModel = this.findClassById(classId);
			final ClassModel otherClassModel = this.findClassById(otherClassId);
			if (classModel == null || otherClassModel == null || !this.isVisible(classModel)
					|| !this.isVisible(otherClassModel)) {
				continue;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
			final NodeLayout otherLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, otherClassId);

			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			final Rectangle2D otherBounds = this.computeClassBounds(g2, otherClassModel, otherLayout);

			final AnchorSide preferredSide = this.findClosestSideFromCenter(bounds, otherBounds.getCenterX(),
					otherBounds.getCenterY());

			if (preferredSide != side) {
				continue;
			}

			final double sortValue = switch (side) {
			case TOP, BOTTOM -> otherBounds.getCenterX();
			case LEFT, RIGHT -> otherBounds.getCenterY();
			};

			slots.add(new LinkSlot(linkModel.getId(), sortValue));
		}

		slots.sort((a, b) -> Double.compare(a.sortValue(), b.sortValue()));

		int index = 0;
		for (int i = 0; i < slots.size(); i++) {
			if (slots.get(i).linkId().equals(targetLink.getId())) {
				index = i;
				break;
			}
		}

		final double spacing = 14.0;
		final double centerIndex = (slots.size() - 1) / 2.0;
		return (index - centerIndex) * spacing;
	}

	private AnchorSide findClosestSideFromCenter(final Rectangle2D bounds, final double x, final double y) {
		final double topDistance = Line2D.ptSegDist(bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getY(), x,
				y);
		final double bottomDistance = Line2D.ptSegDist(bounds.getX(), bounds.getMaxY(), bounds.getMaxX(),
				bounds.getMaxY(), x, y);
		final double leftDistance = Line2D.ptSegDist(bounds.getX(), bounds.getY(), bounds.getX(), bounds.getMaxY(), x,
				y);
		final double rightDistance = Line2D.ptSegDist(bounds.getMaxX(), bounds.getY(), bounds.getMaxX(),
				bounds.getMaxY(), x, y);

		AnchorSide bestSide = AnchorSide.TOP;
		double bestDistance = topDistance;

		if (bottomDistance < bestDistance) {
			bestDistance = bottomDistance;
			bestSide = AnchorSide.BOTTOM;
		}
		if (leftDistance < bestDistance) {
			bestDistance = leftDistance;
			bestSide = AnchorSide.LEFT;
		}
		if (rightDistance < bestDistance) {
			bestSide = AnchorSide.RIGHT;
		}

		return bestSide;
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

	private List<LinkModel> getActiveLinks() {
		return this.panelType == PanelType.CONCEPTUAL ? this.document.getModel().getConceptualLinks()
				: this.document.getModel().getTechnicalLinks();
	}

	private record DraggedNode(NodeLayout layout, double offsetX, double offsetY) {
	}

	private record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
	}

	private record FieldHitResult(FieldModel field, Rectangle2D bounds) {
	}

	private record LinkCreationState(String classId, String fieldId) {
	}

	private record LinkGeometry(Point2D fromPoint, Point2D toPoint, Point2D labelPoint, Point2D middlePoint,
			double labelAngle, List<Point2D> points) {
	}

	private record ResizingComment(NodeLayout layout, double initialWidth, double initialHeight, double startWorldX,
			double startWorldY) {
	}

	private enum SelectedType {
		NONE, CLASS, FIELD, COMMENT, LINK
	}

	private enum AnchorSide {
		TOP, BOTTOM, LEFT, RIGHT
	}

	private record LinkSlot(String linkId, double sortValue) {
	}

	private record AnchorPair(Point2D from, Point2D to) {
	}

	private record AnchorCandidate(AnchorSide side, Point2D point) {
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