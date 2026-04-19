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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.common.Size2D;
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
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.dialogs.ClassEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.CommentEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.FieldEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.RenameDialog;

public class DiagramCanvas extends JPanel {

	private enum AnchorSide {
		TOP,
		BOTTOM,
		LEFT,
		RIGHT
	}

	private enum RenderPass {
		FULL,
		STATIC,
		MOVING,
		TRANSITION
	}

	private record DraggedLayout(NodeLayout layout, double startX, double startY) {
	}

	private record DraggedSelection(List<DraggedLayout> layouts, double offsetX, double offsetY, double anchorStartX, double anchorStartY) {
	}

	private record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
	}

	private record FieldHitResult(FieldModel field, Rectangle2D bounds) {
	}

	private record LinkCreationState(String classId, String fieldId) {
	}

	private record LinkGeometry(Point2D fromPoint, Point2D toPoint, Point2D labelPoint, Point2D middlePoint, double labelAngle,
			List<Point2D> points) {
	}

	private record ResizingComment(NodeLayout layout, double initialWidth, double initialHeight, double startWorldX, double startWorldY) {
	}

	private enum SelectedType {
		NONE,
		CLASS,
		FIELD,
		COMMENT,
		LINK
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

		public String getActualId() {
			return switch (this.type) {
			case CLASS -> this.classId;
			case FIELD -> this.fieldId;
			case COMMENT -> this.commentId;
			case LINK -> this.linkId;
			default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
			};
		}

		@Override
		public final int hashCode() {
			return Objects.hash(this.type, this.getActualId());
		}

	}

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
	private DraggedSelection draggedSelection;
	private Point lastScreenPoint;

	private boolean panning;
	private LinkCreationState linkCreationState;
	private SelectedElement linkPreviewTarget;
	private Point2D.Double linkPreviewMousePoint;

	private SelectedElement selectedElement;
	private final LinkedHashSet<SelectedElement> selectedElements = new LinkedHashSet<>();
	private ResizingComment resizingComment;

	private StylePalette defaultPalette;

	private SelectedElement pendingClickSelection;
	private boolean pendingModifierSelection;
	private boolean dragOccurred;

	private BufferedImage staticDragLayer;
	private BufferedImage movingDragLayer;
	private Point2D.Double currentDragOffset = new Point2D.Double();
	private final Set<String> movingClassIds = new HashSet<>();
	private final Set<String> movingCommentIds = new HashSet<>();
	private final Set<String> movingLinkIds = new HashSet<>();

	private final Comparator<ClassModel> comparator = (a, b) -> {
		if (this.selectedElement == null || this.selectedElement.type() != SelectedType.CLASS) {
			return 0;
		}

		final String selectedId = this.selectedElement.classId();

		final boolean aSelected = selectedId.equals(a.getId());
		final boolean bSelected = selectedId.equals(b.getId());

		if (aSelected == bSelected) {
			return 0;
		}
		return aSelected ? -1 : 1;
	};

	public DiagramCanvas(final ModelDocument document, final PanelType panelType, final CanvasStatusListener statusListener) {
		this.document = document;
		this.panelType = panelType;
		this.statusListener = statusListener;

		this.setBackground(new Color(0xF2F2F2));
		this.setOpaque(true);
		this.setFocusable(true);

		this.installKeyBindings();

		final MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				DiagramCanvas.this.handleMouseDragged(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				DiagramCanvas.this.handleMousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				DiagramCanvas.this.handleMouseReleased(e);
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

	private void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");
		commentModel.setVisibility(PanelType.CONCEPTUAL, PanelType.LOGICAL, PanelType.LOGICAL);
		this.applyDefaultPaletteToComment(commentModel);

		this.document.getModel().getComments().add(commentModel);

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
		final Point2D.Double center = this.viewportCenterWorld();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2D(220, 80));

		this.select(SelectedElement.forComment(commentModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addDraggedLayout(
			final List<DraggedLayout> layouts,
			final Set<String> seen,
			final SelectedElement element,
			final NodeLayout fallbackLayout) {
		final NodeLayout layout = this.resolveNodeLayoutForSelection(element, fallbackLayout);
		if (layout == null) {
			return;
		}

		final String key = layout.getObjectType() + ":" + layout.getObjectId();
		if (!seen.add(key)) {
			return;
		}

		layouts.add(new DraggedLayout(layout, layout.getPosition().getX(), layout.getPosition().getY()));
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
		this.applyDefaultPaletteToField(fieldModel);
		targetClass.getFields().add(fieldModel);

		this.select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addLink() {
		final LinkModel linkModel = new LinkModel();
		if (this.selectedElement != null && this.selectedElement.type == SelectedType.CLASS) {
			linkModel.setFrom(new LinkEnd(this.selectedElement.classId, null));
		} else {
			linkModel.setFrom(new LinkEnd(null, null));
		}
		linkModel.setTo(new LinkEnd(null, null));

		if (this.panelType == PanelType.CONCEPTUAL) {
			linkModel.setName("new relation");
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		} else {
			linkModel.setName("NEW_LINK");
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		final LinkEditorDialog.Result result = LinkEditorDialog.showDialog(this, this.document, linkModel, this.panelType);
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		final LinkModel createdLink = new LinkModel();
		createdLink.setName(result.name());
		createdLink.setComment(result.comment());
		createdLink.setLineColor(result.lineColor());
		createdLink.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		createdLink.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));

		if (this.panelType == PanelType.CONCEPTUAL) {
			createdLink.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			createdLink.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
			this.document.getModel().getConceptualLinks().add(createdLink);
		} else {
			createdLink.setCardinalityFrom(null);
			createdLink.setCardinalityTo(null);
			this.document.getModel().getTechnicalLinks().add(createdLink);
		}
		this.applyDefaultPaletteToLink(linkModel);

		this.findOrCreateLinkLayout(createdLink.getId());
		this.select(SelectedElement.forLink(createdLink.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		this.applyDefaultPaletteToClass(classModel);

		this.document.getModel().getClasses().add(classModel);

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
		final Point2D.Double center = this.viewportCenterWorld();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, 0));

		this.select(SelectedElement.forClass(classModel.getId()));
		this.notifySelectionChanged();
		this.repaint();
	}

	private void addToSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.document.getModel().getClasses().sort(this.comparator);
		this.selectedElements.add(element);
		this.selectedElement = element;
		this.notifySelectionChanged();
		this.repaint();
	}

	private Point2D applyCurrentDragOffsetIfNeeded(final Point2D point, final String classId) {
		if (point == null || classId == null || !this.isDragRenderingActive() || !this.movingClassIds.contains(classId)) {
			return point;
		}

		final double zoom = this.getPanelState().getZoom();
		final double dx = this.currentDragOffset.getX() / zoom;
		final double dy = this.currentDragOffset.getY() / zoom;

		return new Point2D.Double(point.getX() + dx, point.getY() + dy);
	}

	private void applyDefaultPaletteToClass(final ClassModel classModel) {
		if (this.defaultPalette == null || classModel == null) {
			return;
		}

		classModel.getStyle().setTextColor(this.defaultPalette.getClassTextColor());
		classModel.getStyle().setBackgroundColor(this.defaultPalette.getClassBackgroundColor());
		classModel.getStyle().setBorderColor(this.defaultPalette.getClassBorderColor());
	}

	private void applyDefaultPaletteToComment(final CommentModel commentModel) {
		if (this.defaultPalette == null || commentModel == null) {
			return;
		}

		commentModel.setTextColor(this.defaultPalette.getCommentTextColor());
		commentModel.setBackgroundColor(this.defaultPalette.getCommentBackgroundColor());
		commentModel.setBorderColor(this.defaultPalette.getCommentBorderColor());
	}

	private void applyDefaultPaletteToField(final FieldModel fieldModel) {
		if (this.defaultPalette == null || fieldModel == null) {
			return;
		}

		fieldModel.getStyle().setTextColor(this.defaultPalette.getFieldTextColor());
		fieldModel.getStyle().setBackgroundColor(this.defaultPalette.getFieldBackgroundColor());
	}

	private void applyDefaultPaletteToLink(final LinkModel linkModel) {
		if (this.defaultPalette == null || linkModel == null) {
			return;
		}

		linkModel.setLineColor(this.defaultPalette.getLinkColor());
	}

	public void applyPalette(final StylePalette palette) {
		if (palette == null || this.selectedElements.isEmpty()) {
			return;
		}

		for (final SelectedElement element : this.selectedElements) {
			switch (element.type()) {
			case CLASS -> {
				final ClassModel classModel = this.findClassById(element.classId());
				if (classModel != null) {
					classModel.getStyle().setTextColor(palette.getClassTextColor());
					classModel.getStyle().setBackgroundColor(palette.getClassBackgroundColor());
					classModel.getStyle().setBorderColor(palette.getClassBorderColor());
				}
			}
			case FIELD -> {
				final FieldModel fieldModel = this.findFieldById(element.classId(), element.fieldId());
				if (fieldModel != null) {
					fieldModel.getStyle().setTextColor(palette.getFieldTextColor());
					fieldModel.getStyle().setBackgroundColor(palette.getFieldBackgroundColor());
				}
			}
			case COMMENT -> {
				final CommentModel commentModel = this.findCommentById(element.commentId());
				if (commentModel != null) {
					commentModel.setTextColor(palette.getCommentTextColor());
					commentModel.setBackgroundColor(palette.getCommentBackgroundColor());
					commentModel.setBorderColor(palette.getCommentBorderColor());
				}
			}
			case LINK -> {
				final LinkModel linkModel = this.findLinkById(element.linkId());
				if (linkModel != null) {
					linkModel.setLineColor(palette.getLinkColor());
				}
			}
			default -> {
			}
			}
		}

		this.repaint();
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

	private void buildDragRenderLayers(final DraggedSelection selection) {
		this.movingClassIds.clear();
		this.movingCommentIds.clear();
		this.movingLinkIds.clear();
		this.currentDragOffset = new Point2D.Double();

		if (selection == null || this.getWidth() <= 0 || this.getHeight() <= 0) {
			this.staticDragLayer = null;
			this.movingDragLayer = null;
			return;
		}

		for (final DraggedLayout draggedLayout : selection.layouts()) {
			if (draggedLayout.layout().getObjectType() == LayoutObjectType.CLASS) {
				this.movingClassIds.add(draggedLayout.layout().getObjectId());
			} else if (draggedLayout.layout().getObjectType() == LayoutObjectType.COMMENT) {
				this.movingCommentIds.add(draggedLayout.layout().getObjectId());
			}
		}

		for (final LinkModel linkModel : this.getActiveLinks()) {
			if (this.isLinkBetweenMovingObjects(linkModel)) {
				this.movingLinkIds.add(linkModel.getId());
			}
		}

		this.staticDragLayer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.movingDragLayer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);

		final Graphics2D staticGraphics = this.staticDragLayer.createGraphics();
		final Graphics2D movingGraphics = this.movingDragLayer.createGraphics();

		try {
			this.configureGraphics(staticGraphics);
			this.configureGraphics(movingGraphics);

			this.drawGrid(staticGraphics);

			final PanelState state = this.getPanelState();

			staticGraphics.translate(state.getPanX(), state.getPanY());
			staticGraphics.scale(state.getZoom(), state.getZoom());

			movingGraphics.translate(state.getPanX(), state.getPanY());
			movingGraphics.scale(state.getZoom(), state.getZoom());

			this.drawComments(staticGraphics, RenderPass.STATIC);
			this.drawClasses(staticGraphics, RenderPass.STATIC);
			this.drawLinks(staticGraphics, RenderPass.STATIC);

			this.drawComments(movingGraphics, RenderPass.MOVING);
			this.drawClasses(movingGraphics, RenderPass.MOVING);
			this.drawLinks(movingGraphics, RenderPass.MOVING);
		} finally {
			staticGraphics.dispose();
			movingGraphics.dispose();
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
				middle += "[" + (associationClass == null ? linkModel.getAssociationClassId() : this.resolveClassTitle(associationClass))
						+ "]";
			}

			return fromName + " > " + middle + " < " + toName;
		}

		final FieldModel fromField = this.findFieldById(linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId());
		final FieldModel toField = this.findFieldById(linkModel.getTo().getClassId(), linkModel.getTo().getFieldId());

		final String fromFieldName = fromField == null ? "?" : this.resolveFieldName(fromField);
		final String toFieldName = toField == null ? "?" : this.resolveFieldName(toField);

		return fromName + " > " + fromFieldName + " -> " + toFieldName + " < " + toName;
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
			final FieldModel fieldModel = this.findFieldById(this.selectedElement.classId(), this.selectedElement.fieldId());
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

			if (commentModel.getBinding() != null && commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
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

	private List<Point2D> buildSelfLinkPoints(
			final Graphics2D g2,
			final LinkModel linkModel,
			final Point2D fromPoint,
			final Point2D toPoint) {
		final List<Point2D> points = new ArrayList<>();
		points.add(fromPoint);

		final ClassModel classModel = this.findClassById(linkModel.getFrom().getClassId());
		if (classModel == null) {
			points.add(toPoint);
			return points;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
		final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);

		final boolean useRightSide = fromPoint.getX() >= bounds.getCenterX() || toPoint.getX() >= bounds.getCenterX();
		final double horizontalOffset = 40.0;

		final double outsideX = useRightSide ? bounds.getMaxX() + horizontalOffset : bounds.getX() - horizontalOffset;

		points.add(new Point2D.Double(outsideX, fromPoint.getY()));
		points.add(new Point2D.Double(outsideX, toPoint.getY()));
		points.add(toPoint);

		return points;
	}

	private double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(max, value));
	}

	private void clearSelection() {
		this.selectedElements.clear();
		this.selectedElement = null;
		this.notifySelectionChanged();
		this.repaint();
	}

	private Rectangle2D computeClassBounds(final Graphics2D g2, final ClassModel classModel, final NodeLayout layout) {
		g2.setFont(DiagramCanvas.TITLE_FONT);
		final FontMetrics titleMetrics = g2.getFontMetrics();

		g2.setFont(DiagramCanvas.BODY_FONT);
		final FontMetrics bodyMetrics = g2.getFontMetrics();

		int width = Math.max(DiagramCanvas.CLASS_MIN_WIDTH,
				titleMetrics.stringWidth(this.resolveClassTitle(classModel)) + DiagramCanvas.PADDING * 2);
		for (final FieldModel fieldModel : this.getVisibleFields(classModel)) {
			width = Math.max(width, bodyMetrics.stringWidth(this.resolveFieldName(fieldModel)) + DiagramCanvas.PADDING * 2);
		}

		final int visibleFieldCount = this.getVisibleFields(classModel).size();
		final int height = DiagramCanvas.HEADER_HEIGHT + visibleFieldCount * DiagramCanvas.ROW_HEIGHT;

		if (layout.getSize().getX() <= 0.0) {
			layout.getSize().setWidth(width);
		}
		layout.getSize().setHeight(height);

		return new Rectangle2D.Double(layout.getPosition().getX(),
				layout.getPosition().getY(),
				Math.max(width, layout.getSize().getWidth()),
				height);
	}

	private Rectangle2D computeCommentBounds(final Graphics2D g2, final String text, final NodeLayout layout) {
		g2.setFont(DiagramCanvas.BODY_FONT);
		final FontMetrics metrics = g2.getFontMetrics();

		final double width = layout.getSize().getWidth() > 0.0 ? layout.getSize().getWidth() : DiagramCanvas.COMMENT_MIN_WIDTH;
		final List<String> wrappedLines = this.wrapText(text, metrics, (int) Math.max(40, width - DiagramCanvas.PADDING * 2));
		final int contentHeight = wrappedLines.size() * (metrics.getHeight() + 2) + DiagramCanvas.PADDING * 2;

		if (layout.getSize().getWidth() <= 0.0) {
			layout.getSize().setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH, width));
		}
		if (layout.getSize().getHeight() <= 0.0) {
			layout.getSize().setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, contentHeight));
		}

		return new Rectangle2D.Double(layout.getPosition().getX(),
				layout.getPosition().getY(),
				Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE, layout.getSize().getWidth()),
				Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT, layout.getSize().getHeight()));
	}

	private List<AnchorCandidate> computeConceptualCandidates(
			final Graphics2D g2,
			final LinkModel targetLink,
			final String classId,
			final Rectangle2D bounds) {
		final List<AnchorCandidate> candidates = new ArrayList<>();

		for (final AnchorSide side : AnchorSide.values()) {
			final double offset = this.computeConceptualSideOffset(g2, targetLink, classId, side);
			final Point2D point = switch (side) {
			case TOP ->
				new Point2D.Double(this.clamp(bounds.getCenterX() + offset, bounds.getX() + 12, bounds.getMaxX() - 12), bounds.getY());
			case BOTTOM ->
				new Point2D.Double(this.clamp(bounds.getCenterX() + offset, bounds.getX() + 12, bounds.getMaxX() - 12), bounds.getMaxY());
			case LEFT ->
				new Point2D.Double(bounds.getX(), this.clamp(bounds.getCenterY() + offset, bounds.getY() + 12, bounds.getMaxY() - 12));
			case RIGHT ->
				new Point2D.Double(bounds.getMaxX(), this.clamp(bounds.getCenterY() + offset, bounds.getY() + 12, bounds.getMaxY() - 12));
			};

			candidates.add(new AnchorCandidate(side, point));
		}

		return candidates;
	}

	private double computeConceptualSideOffset(
			final Graphics2D g2,
			final LinkModel targetLink,
			final String classId,
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
			if (classModel == null || otherClassModel == null || !this.isVisible(classModel) || !this.isVisible(otherClassModel)) {
				continue;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
			final NodeLayout otherLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, otherClassId);

			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			final Rectangle2D otherBounds = this.computeClassBounds(g2, otherClassModel, otherLayout);

			final AnchorSide preferredSide = this.findClosestSideFromCenter(bounds, otherBounds.getCenterX(), otherBounds.getCenterY());

			if (preferredSide != side) {
				continue;
			}

			final double sortValue = switch (side) {
			case TOP, BOTTOM -> otherBounds.getCenterX();
			case LEFT, RIGHT -> otherBounds.getCenterY();
			};

			slots.add(new LinkSlot(linkModel.getId(), sortValue));
		}

		slots.sort(Comparator.comparing(LinkSlot::sortValue));

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

	private void configureGraphics(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	private DraggedSelection createDraggedSelection(
			final SelectedElement hitSelection,
			final NodeLayout hitLayout,
			final Point2D.Double worldPoint,
			final Rectangle2D hitBounds) {
		final List<DraggedLayout> layouts = new ArrayList<>();
		final Set<String> seen = new HashSet<>();

		if (this.selectedElements.isEmpty() || !this.isElementSelected(hitSelection)) {
			this.addDraggedLayout(layouts, seen, hitSelection, hitLayout);
		} else {
			for (final SelectedElement element : this.selectedElements) {
				this.addDraggedLayout(layouts, seen, element, null);
			}

			if (layouts.isEmpty()) {
				this.addDraggedLayout(layouts, seen, hitSelection, hitLayout);
			}
		}

		final DraggedSelection selection = new DraggedSelection(layouts,
				worldPoint.getX() - hitBounds.getX(),
				worldPoint.getY() - hitBounds.getY(),
				hitLayout.getPosition().getX(),
				hitLayout.getPosition().getY());

		this.buildDragRenderLayers(selection);
		return selection;
	}

	private Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
	}

	private void deleteClass(final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return;
		}

		this.document.getModel().getClasses().remove(classModel);
		this.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.CLASS && layout.getObjectId().equals(classId));

		this.getActiveLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.document.getModel()
				.getConceptualLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));
		this.document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> this.findLinkById(linkLayout.getLinkId()) == null);
	}

	private void deleteComment(final String commentId) {
		this.document.getModel().getComments().removeIf(comment -> comment.getId().equals(commentId));
		this.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	private void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return;
		}

		classModel.getFields().removeIf(field -> field.getId().equals(fieldId));
		this.document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()));
		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> this.findLinkById(linkLayout.getLinkId()) == null);
	}

	private void deleteLink(final String linkId) {
		this.getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getConceptualLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getTechnicalLinks().removeIf(link -> link.getId().equals(linkId));
		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

	private void deleteSelection() {
		if (this.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.selectedElements);

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case LINK -> this.deleteLink(element.linkId());
			case COMMENT -> this.deleteComment(element.commentId());
			case FIELD -> this.deleteField(element.classId(), element.fieldId());
			case CLASS -> this.deleteClass(element.classId());
			default -> {
			}
			}
		}

		this.clearSelection();
		this.repaint();
	}

	private void drawAlignedLinkLabel(final Graphics2D g2, final String text, final Point2D center, final double angle) {
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
			labelGraphics
					.drawString(text, (float) (-textBounds.getWidth() / 2.0), (float) (metrics.getAscent() - textBounds.getHeight() / 2.0));
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

	private void drawCardinalityLabel(
			final Graphics2D g2,
			final String text,
			final Point2D anchor,
			final Point2D adjacentPoint,
			final double angle) {
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

	private void drawClasses(final Graphics2D g2, final RenderPass pass) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (!this.isVisible(classModel)) {
				continue;
			}
			final boolean moving = this.isMovingClass(classModel.getId());
			if ((moving ? pass == RenderPass.STATIC : pass == RenderPass.MOVING) || pass == RenderPass.TRANSITION) {
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
			g2.drawString(this.resolveClassTitle(classModel),
					(float) bounds.getX() + DiagramCanvas.PADDING,
					(float) bounds.getY() + DiagramCanvas.HEADER_HEIGHT - 9);

			g2.setFont(DiagramCanvas.BODY_FONT);
			double rowY = bounds.getY() + DiagramCanvas.HEADER_HEIGHT;
			final List<FieldModel> visibleFields = this.getVisibleFields(classModel);

			for (final FieldModel fieldModel : visibleFields) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(bounds.getX(), rowY, bounds.getWidth(), DiagramCanvas.ROW_HEIGHT);

				g2.setColor(fieldModel.getStyle().getBackgroundColor());
				g2.fill(fieldBounds);

				if (this.isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(this.withAlpha(DiagramCanvas.SELECTION_COLOR, 60));
					g2.fill(fieldBounds);
				}

				g2.setColor(classModel.getStyle().getBorderColor());
				g2.draw(new Line2D.Double(bounds.getX(), rowY, bounds.getMaxX(), rowY));

				g2.setColor(fieldModel.getStyle().getTextColor());
				g2.drawString(this.resolveFieldName(fieldModel), (float) bounds.getX() + DiagramCanvas.PADDING, (float) rowY + 15);

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
				g2.draw(new Line2D.Double(bounds.getX(),
						bounds.getY() + DiagramCanvas.HEADER_HEIGHT,
						bounds.getMaxX(),
						bounds.getY() + DiagramCanvas.HEADER_HEIGHT));
			}
		}
	}

	private void drawComments(final Graphics2D g2, final RenderPass pass) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String commentText = this.resolveCommentText(commentModel);
			if (commentText == null || commentText.isBlank() || !this.isCommentVisible(commentModel)) {
				continue;
			}
			final boolean moving = this.isMovingComment(commentModel.getId());
			if ((moving ? pass == RenderPass.STATIC : pass == RenderPass.MOVING) || pass == RenderPass.TRANSITION) {
				continue;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			final Rectangle2D bounds = this.computeCommentBounds(g2, commentText, layout);
			layout.getSize().setWidth(bounds.getWidth());
			layout.getSize().setHeight(bounds.getHeight());

			if (commentModel.getKind() == CommentKind.BOUND && commentModel.getBinding() != null) {
				final Point2D anchor = this.findBoundTargetAnchor(g2, commentModel);
				if (anchor != null) {
					g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR : new Color(0x777777));
					g2.setStroke(new BasicStroke(this.isCommentSelected(commentModel.getId()) ? 2.0f : 1.0f));
					g2.draw(new Line2D.Double(anchor.getX(), anchor.getY(), bounds.getCenterX(), bounds.getCenterY()));
					g2.setStroke(new BasicStroke(1.0f));
				}
			}

			g2.setColor(commentModel.getBackgroundColor());
			g2.fill(bounds);

			g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR : commentModel.getBorderColor());
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
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE));
			}
		}
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

	private void drawLinkPreview(final Graphics2D g2) {
		if (this.linkCreationState == null) {
			return;
		}

		final Point2D fromAnchor = this.resolvePreviewSourceAnchor(g2);
		if (fromAnchor == null) {
			return;
		}

		final SelectedElement target = this.linkPreviewTarget;
		final boolean valid = this.isValidPreviewTarget(target);

		final Point2D toAnchor;
		if (target != null) {
			toAnchor = this.resolvePreviewTargetAnchor(g2, target);
		} else {
			toAnchor = this.linkPreviewMousePoint;
		}

		if (toAnchor == null) {
			return;
		}

		final Graphics2D previewGraphics = (Graphics2D) g2.create();
		try {
			previewGraphics.setColor(valid ? DiagramCanvas.SELECTION_COLOR : Color.RED);
			previewGraphics.setStroke(
					new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 6.0f, 6.0f }, 0.0f));
			previewGraphics.draw(new Line2D.Double(fromAnchor, toAnchor));
		} finally {
			previewGraphics.dispose();
		}
	}

	private void drawLinks(final Graphics2D g2, final RenderPass pass) {
		g2.setFont(DiagramCanvas.BODY_FONT);

		for (final LinkModel linkModel : this.getActiveLinks()) {
			final boolean moving = this.isMovingLink(linkModel.getId());
			final boolean transition = this.isLinkAttachedToMovingObject(linkModel) && !moving;

			if (pass == RenderPass.STATIC && (moving || transition) || pass == RenderPass.MOVING && !moving
					|| pass == RenderPass.TRANSITION && !transition) {
				continue;
			}
			final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);
			if (geometry == null) {
				continue;
			}

			g2.setColor(this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			g2.setStroke(new BasicStroke(this.isLinkSelected(linkModel.getId()) ? 2.5f : 1.2f));

			for (int i = 0; i < geometry.points().size() - 1; i++) {
				g2.draw(new Line2D.Double(geometry.points().get(i), geometry.points().get(i + 1)));
			}

			if (this.panelType != PanelType.CONCEPTUAL) {
				this.drawArrowHead(g2, geometry.points().get(geometry.points().size() - 2), geometry.toPoint());
			}

			g2.setStroke(new BasicStroke(1.0f));

			if (this.panelType == PanelType.CONCEPTUAL && linkModel.getName() != null && !linkModel.getName().isBlank()) {
				this.drawAlignedLinkLabel(g2, linkModel.getName(), geometry.labelPoint(), geometry.labelAngle());
			}

			if (this.panelType == PanelType.CONCEPTUAL) {
				if (linkModel.getCardinalityFrom() != null) {
					this.drawCardinalityLabel(g2,
							linkModel.getCardinalityFrom().getDisplayValue(),
							geometry.fromPoint(),
							geometry.points().get(1),
							geometry.labelAngle());
				}
				if (linkModel.getCardinalityTo() != null) {
					this.drawCardinalityLabel(g2,
							linkModel.getCardinalityTo().getDisplayValue(),
							geometry.toPoint(),
							geometry.points().get(geometry.points().size() - 2),
							geometry.labelAngle());
				}
			}
		}
	}

	private void drawMultilineText(final Graphics2D g2, final String text, final Rectangle2D bounds, final int padding) {
		final FontMetrics metrics = g2.getFontMetrics();
		final List<String> wrappedLines = this.wrapText(text, metrics, (int) Math.max(40, bounds.getWidth() - padding * 2));

		float y = (float) bounds.getY() + padding + metrics.getAscent();
		for (final String line : wrappedLines) {
			g2.drawString(line, (float) bounds.getX() + padding, y);
			y += metrics.getHeight() + 2;
		}
	}

	private void duplicateSelection() {
		if (this.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.selectedElements);

		final Set<String> selectedClassIds = new HashSet<>();
		final Set<String> selectedFieldIds = new HashSet<>();
		final Set<String> selectedCommentIds = new HashSet<>();
		final Set<String> selectedLinkIds = new HashSet<>();

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case CLASS -> selectedClassIds.add(element.classId());
			case FIELD -> {
				if (!selectedClassIds.contains(element.classId())) {
					selectedFieldIds.add(element.fieldId());
				}
			}
			case COMMENT -> selectedCommentIds.add(element.commentId());
			case LINK -> selectedLinkIds.add(element.linkId());
			default -> {
			}
			}
		}

		final Map<String, String> duplicatedClassIds = new HashMap<>();
		final Map<String, String> duplicatedFieldIds = new HashMap<>();
		final Map<String, String> duplicatedCommentIds = new HashMap<>();
		final Map<String, String> duplicatedLinkIds = new HashMap<>();

		final LinkedHashSet<SelectedElement> newSelection = new LinkedHashSet<>();

		for (final String classId : selectedClassIds) {
			final ClassModel source = this.findClassById(classId);
			if (source == null) {
				continue;
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

				duplicatedFieldIds.put(sourceField.getId(), fieldCopy.getId());
			}

			this.document.getModel().getClasses().add(copy);
			duplicatedClassIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, source.getId());
			final NodeLayout copyLayout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, copy.getId());
			copyLayout.setPosition(new Point2D.Double(sourceLayout.getPosition().getX() + 30, sourceLayout.getPosition().getY() + 30));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forClass(copy.getId()));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.findOwnerClassOfField(fieldId);
			if (owner == null || duplicatedClassIds.containsKey(owner.getId())) {
				continue;
			}

			final FieldModel source = this.findFieldById(owner.getId(), fieldId);
			if (source == null) {
				continue;
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

			final int insertIndex = owner.getFields().indexOf(source);
			if (insertIndex < 0) {
				owner.getFields().add(copy);
			} else {
				owner.getFields().add(insertIndex + 1, copy);
			}

			duplicatedFieldIds.put(source.getId(), copy.getId());
			newSelection.add(SelectedElement.forField(owner.getId(), copy.getId()));
		}

		for (final String commentId : selectedCommentIds) {
			final CommentModel source = this.findCommentById(commentId);
			if (source == null) {
				continue;
			}

			final CommentModel copy = new CommentModel();
			copy.setKind(source.getKind());
			copy.setText(source.getText());
			copy.setTextColor(source.getTextColor());
			copy.setBackgroundColor(source.getBackgroundColor());
			copy.setBorderColor(source.getBorderColor());

			if (source.getBinding() != null) {
				String targetId = source.getBinding().getTargetId();

				if (source.getBinding().getTargetType() == BoundTargetType.CLASS && duplicatedClassIds.containsKey(targetId)) {
					targetId = duplicatedClassIds.get(targetId);
				} else if (source.getBinding().getTargetType() == BoundTargetType.LINK && duplicatedLinkIds.containsKey(targetId)) {
					targetId = duplicatedLinkIds.get(targetId);
				}

				copy.setBinding(new CommentBinding(source.getBinding().getTargetType(), targetId));
			}

			this.document.getModel().getComments().add(copy);
			duplicatedCommentIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, source.getId());
			final NodeLayout copyLayout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, copy.getId());
			copyLayout.setPosition(new Point2D.Double(sourceLayout.getPosition().getX() + 30, sourceLayout.getPosition().getY() + 30));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forComment(copy.getId()));
		}

		final Set<String> linksToDuplicate = new LinkedHashSet<>(selectedLinkIds);
		for (final LinkModel link : this.getActiveLinks()) {
			final boolean fromClassDuplicated = duplicatedClassIds.containsKey(link.getFrom().getClassId());
			final boolean toClassDuplicated = duplicatedClassIds.containsKey(link.getTo().getClassId());
			final boolean fromFieldDuplicated = link.getFrom().getFieldId() != null
					&& duplicatedFieldIds.containsKey(link.getFrom().getFieldId());
			final boolean toFieldDuplicated = link.getTo().getFieldId() != null
					&& duplicatedFieldIds.containsKey(link.getTo().getFieldId());

			if (fromClassDuplicated || toClassDuplicated || fromFieldDuplicated || toFieldDuplicated) {
				linksToDuplicate.add(link.getId());
			}
		}

		for (final String linkId : linksToDuplicate) {
			final LinkModel source = this.findLinkById(linkId);
			if (source == null) {
				continue;
			}

			final String newFromClassId = duplicatedClassIds.getOrDefault(source.getFrom().getClassId(), source.getFrom().getClassId());
			final String newToClassId = duplicatedClassIds.getOrDefault(source.getTo().getClassId(), source.getTo().getClassId());
			final String newFromFieldId = source.getFrom().getFieldId() == null ? null
					: duplicatedFieldIds.getOrDefault(source.getFrom().getFieldId(), source.getFrom().getFieldId());
			final String newToFieldId = source.getTo().getFieldId() == null ? null
					: duplicatedFieldIds.getOrDefault(source.getTo().getFieldId(), source.getTo().getFieldId());

			final LinkModel copy = new LinkModel();
			copy.setName(source.getName());
			copy.setComment(source.getComment());
			copy.setLineColor(source.getLineColor());
			copy.setAssociationClassId(source.getAssociationClassId() == null ? null
					: duplicatedClassIds.getOrDefault(source.getAssociationClassId(), source.getAssociationClassId()));
			copy.setFrom(new LinkEnd(newFromClassId, newFromFieldId));
			copy.setTo(new LinkEnd(newToClassId, newToFieldId));
			copy.setCardinalityFrom(source.getCardinalityFrom());
			copy.setCardinalityTo(source.getCardinalityTo());

			this.getActiveLinks().add(copy);
			duplicatedLinkIds.put(source.getId(), copy.getId());

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

			newSelection.add(SelectedElement.forLink(copy.getId()));
		}

		this.selectedElements.clear();
		this.selectedElements.addAll(newSelection);
		this.selectedElement = this.selectedElements.isEmpty() ? null : this.selectedElements.getLast();

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

	private void editComment(final String commentId) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		final CommentEditorDialog.Result result = CommentEditorDialog.showDialog(this, this.document, commentModel);
		if (result == null) {
			return;
		}

		commentModel.setText(result.text());
		commentModel.setTextColor(result.textColor());
		commentModel.setBackgroundColor(result.backgroundColor());
		commentModel.setBorderColor(result.borderColor());
		commentModel.setKind(result.kind());
		commentModel.setBinding(result.binding());
		commentModel.setVisibleInConceptual(result.visibleInConceptual());
		commentModel.setVisibleInLogical(result.visibleInLogical());
		commentModel.setVisibleInPhysical(result.visibleInPhysical());

		this.notifySelectionChanged();
		this.repaint();
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

	private void editLink(final String linkId) {
		final LinkModel linkModel = this.findLinkById(linkId);
		if (linkModel == null) {
			return;
		}

		final LinkEditorDialog.Result result = LinkEditorDialog.showDialog(this, this.document, linkModel, this.panelType);
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
			linkModel.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
		} else {
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		this.notifySelectionChanged();
		this.repaint();
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

	private ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	private AnchorSide findClosestSideFromCenter(final Rectangle2D bounds, final double x, final double y) {
		final double topDistance = Line2D.ptSegDist(bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getY(), x, y);
		final double bottomDistance = Line2D.ptSegDist(bounds.getX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY(), x, y);
		final double leftDistance = Line2D.ptSegDist(bounds.getX(), bounds.getY(), bounds.getX(), bounds.getMaxY(), x, y);
		final double rightDistance = Line2D.ptSegDist(bounds.getMaxX(), bounds.getY(), bounds.getMaxX(), bounds.getMaxY(), x, y);

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

	private CommentModel findCommentById(final String commentId) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			if (commentModel.getId().equals(commentId)) {
				return commentModel;
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

	private FieldHitResult findFieldHit(final ClassModel classModel, final Rectangle2D classBounds, final Point2D.Double worldPoint) {
		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);

		for (int i = 0; i < visibleFields.size(); i++) {
			final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
					classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
					classBounds.getWidth(),
					DiagramCanvas.ROW_HEIGHT);

			if (fieldBounds.contains(worldPoint.getX(), worldPoint.getY())) {
				return new FieldHitResult(visibleFields.get(i), fieldBounds);
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

	private NodeLayout findNodeLayout(final LayoutObjectType objectType, final String objectId) {
		if (objectId == null) {
			return null;
		}

		for (final NodeLayout layout : this.getPanelState().getNodeLayouts()) {
			if (layout.getObjectType() == objectType && objectId.equals(layout.getObjectId())) {
				return layout;
			}
		}

		return null;
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
		layout.setSize(new Size2D(0, 0));
		this.getPanelState().getNodeLayouts().add(layout);
		return layout;
	}

	private ClassModel findOwnerClassOfField(final String fieldId) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			for (final FieldModel fieldModel : classModel.getFields()) {
				if (fieldModel.getId().equals(fieldId)) {
					return classModel;
				}
			}
		}
		return null;
	}

	private HitResult findTopmostHit(final Point2D.Double worldPoint) {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		try {
			for (int i = this.getActiveLinks().size() - 1; i >= 0; i--) {
				final LinkModel linkModel = this.getActiveLinks().get(i);
				final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);

				if (geometry != null && this.isPointNearGeometry(worldPoint, geometry)) {
					return new HitResult(null,
							new Rectangle2D.Double(worldPoint.getX(), worldPoint.getY(), 1, 1),
							SelectedElement.forLink(linkModel.getId()));
				}
			}

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
					return new HitResult(layout,
							fieldHitResult.bounds(),
							SelectedElement.forField(classModel.getId(), fieldHitResult.field().getId()));
				}

				return new HitResult(layout, bounds, SelectedElement.forClass(classModel.getId()));
			}
		} finally {
			g2.dispose();
		}

		return null;
	}

	private void finishLinkCreation(final Point2D.Double worldPoint) {
		if (this.linkCreationState == null) {
			return;
		}

		SelectedElement target = this.linkPreviewTarget;
		if (target == null) {
			final HitResult hitResult = this.findTopmostHit(worldPoint);
			target = hitResult == null ? null : this.normalizeLinkEndpointSelection(hitResult.selection());
		}

		if (!this.isValidPreviewTarget(target)) {
			return;
		}

		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(this.linkCreationState.classId(), this.linkCreationState.fieldId()));
		linkModel.setTo(new LinkEnd(target.classId(), target.fieldId()));
		this.applyDefaultPaletteToLink(linkModel);

		if (this.panelType == PanelType.CONCEPTUAL) {
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
			this.document.getModel().getConceptualLinks().add(linkModel);
		} else {
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
			this.document.getModel().getTechnicalLinks().add(linkModel);
		}

		this.findOrCreateLinkLayout(linkModel.getId());
		this.select(SelectedElement.forLink(linkModel.getId()));
	}

	private List<LinkModel> getActiveLinks() {
		return this.panelType == PanelType.CONCEPTUAL ? this.document.getModel().getConceptualLinks()
				: this.document.getModel().getTechnicalLinks();
	}

	public Action getCanvasAction(final String actionKey) {
		return this.getActionMap().get(actionKey);
	}

	private String getEditableClassName(final ClassModel classModel) {
		return this.panelType == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
				: classModel.getNames().getTechnicalName();
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

	private String getEditableFieldName(final FieldModel fieldModel) {
		return this.panelType == PanelType.CONCEPTUAL ? fieldModel.getNames().getName() : fieldModel.getNames().getTechnicalName();
	}

	private PanelState getPanelState() {
		return this.document.getWorkspace().getPanels().get(this.panelType);
	}

	public PanelType getPanelType() {
		return this.panelType;
	}

	public SelectionInfo getSelectionInfo() {
		return new SelectionInfo(this.panelType, this.buildSelectionPath());
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
			final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
			this.linkPreviewMousePoint = worldPoint;

			final HitResult hitResult = this.findTopmostHit(worldPoint);
			this.linkPreviewTarget = hitResult == null ? null : this.normalizeLinkEndpointSelection(hitResult.selection());

			this.repaint();
			return;
		}

		if (this.resizingComment != null) {
			final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
			this.resizingComment.layout()
					.getSize()
					.setWidth(Math.max(DiagramCanvas.COMMENT_MIN_WIDTH_VALUE,
							this.resizingComment.initialWidth() + (worldPoint.getX() - this.resizingComment.startWorldX())));
			this.resizingComment.layout()
					.getSize()
					.setHeight(Math.max(DiagramCanvas.COMMENT_MIN_HEIGHT,
							this.resizingComment.initialHeight() + (worldPoint.getY() - this.resizingComment.startWorldY())));
			this.repaint();
			return;
		}

		if (this.draggedSelection == null) {
			return;
		}

		this.dragOccurred = true;

		final Point2D.Double worldPoint = this.screenToWorld(event.getPoint());
		final double anchorX = worldPoint.getX() - this.draggedSelection.offsetX();
		final double anchorY = worldPoint.getY() - this.draggedSelection.offsetY();

		final double deltaX = anchorX - this.draggedSelection.anchorStartX();
		final double deltaY = anchorY - this.draggedSelection.anchorStartY();

		final double zoom = this.getPanelState().getZoom();
		this.currentDragOffset = new Point2D.Double(deltaX * zoom, deltaY * zoom);

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

			final SelectedElement source = this.normalizeLinkEndpointSelection(hitResult.selection());
			if (source == null) {
				return;
			}

			if (!this.selectedElements.contains(source)) {
				this.select(source);
			} else {
				this.selectedElement = source;
				this.notifySelectionChanged();
			}

			this.linkCreationState = new LinkCreationState(source.classId(), source.fieldId());
			this.linkPreviewTarget = null;
			this.linkPreviewMousePoint = worldPoint;
			this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			this.repaint();
			return;
		}

		if (!SwingUtilities.isLeftMouseButton(event)) {
			return;
		}

		this.pendingClickSelection = hitResult == null ? null : hitResult.selection();
		this.pendingModifierSelection = event.isShiftDown() || event.isControlDown();
		this.dragOccurred = false;

		if (hitResult == null) {
			if (!this.pendingModifierSelection) {
				this.clearSelection();
			}
			return;
		}

		final SelectedElement clickedElement = hitResult.selection();

		if (!this.pendingModifierSelection) {
			this.select(clickedElement);
		}

		if (!this.pendingModifierSelection && event.getClickCount() == 2) {
			this.openEditDialogForSelection();
			return;
		}

		if (!this.pendingModifierSelection && hitResult.selection().type() == SelectedType.COMMENT && hitResult.bounds() != null
				&& this.isInCommentResizeHandle(hitResult.bounds(), worldPoint)) {
			this.resizingComment = new ResizingComment(hitResult
					.layout(), hitResult.bounds().getWidth(), hitResult.bounds().getHeight(), worldPoint.getX(), worldPoint.getY());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}

		if (hitResult.layout() != null) {
			if (this.pendingModifierSelection && !this.isElementSelected(clickedElement)) {
				return;
			}

			this.draggedSelection = this.createDraggedSelection(clickedElement, hitResult.layout(), worldPoint, hitResult.bounds());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private void handleMouseReleased(final MouseEvent event) {
		if (SwingUtilities.isRightMouseButton(event) && this.linkCreationState != null) {
			this.finishLinkCreation(this.screenToWorld(event.getPoint()));
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.draggedSelection != null && this.dragOccurred) {
			final double zoom = this.getPanelState().getZoom();
			final double deltaX = this.currentDragOffset.getX() / zoom;
			final double deltaY = this.currentDragOffset.getY() / zoom;

			for (final DraggedLayout draggedLayout : this.draggedSelection.layouts()) {
				draggedLayout.layout().getPosition().setLocation(draggedLayout.startX() + deltaX, draggedLayout.startY() + deltaY);
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.pendingModifierSelection && !this.dragOccurred) {
			this.updateSelectionFromMouse(this.pendingClickSelection, event);
		}

		this.draggedSelection = null;
		this.resizingComment = null;
		this.panning = false;
		this.lastScreenPoint = null;
		this.linkCreationState = null;
		this.linkPreviewTarget = null;
		this.linkPreviewMousePoint = null;

		this.pendingClickSelection = null;
		this.pendingModifierSelection = false;
		this.dragOccurred = false;

		this.staticDragLayer = null;
		this.movingDragLayer = null;
		this.currentDragOffset = new Point2D.Double();
		this.movingClassIds.clear();
		this.movingCommentIds.clear();
		this.movingLinkIds.clear();

		this.setCursor(Cursor.getDefaultCursor());
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

	private boolean hasOutgoingTechnicalLink(final String classId, final String fieldId) {
		for (final LinkModel linkModel : this.document.getModel().getTechnicalLinks()) {
			if (linkModel.getFrom() == null || linkModel.getTo() == null || linkModel.getFrom().getFieldId() == null
					|| linkModel.getTo().getFieldId() == null) {
				continue;
			}

			if (Objects.equals(linkModel.getFrom().getClassId(), classId) && Objects.equals(linkModel.getFrom().getFieldId(), fieldId)) {
				return true;
			}
		}
		return false;
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

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), "moveFieldUp");
		this.getActionMap().put("moveFieldUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveSelectedFieldInList(-1);
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), "moveFieldDown");
		this.getActionMap().put("moveFieldDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.moveSelectedFieldInList(1);
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "addTable");
		this.getActionMap().put("addTable", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addTable();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "addField");
		this.getActionMap().put("addField", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addField();
			}
		});

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "addComment");
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

		this.getInputMap(JComponent.WHEN_FOCUSED)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "duplicateSelection");
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

		this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "addLink");
		this.getActionMap().put("addLink", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramCanvas.this.addLink();
			}
		});
	}

	private boolean isClassSelected(final String classId) {
		return this.selectedElements.contains(SelectedElement.forClass(classId));
	}

	private boolean isCommentSelected(final String commentId) {
		return this.selectedElements.contains(SelectedElement.forComment(commentId));
	}

	private boolean isCommentVisible(final CommentModel commentModel) {
		final boolean visibleInPanel = switch (this.panelType) {
		case CONCEPTUAL -> commentModel.isVisibleInConceptual();
		case LOGICAL -> commentModel.isVisibleInLogical();
		case PHYSICAL -> commentModel.isVisibleInPhysical();
		};

		if (!visibleInPanel) {
			return false;
		}

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

	private boolean isDragRenderingActive() {
		return this.draggedSelection != null && this.staticDragLayer != null && this.movingDragLayer != null;
	}

	private boolean isElementSelected(final SelectedElement element) {
		return element != null && this.selectedElements.contains(element);
	}

	private boolean isFieldSelected(final String classId, final String fieldId) {
		return this.selectedElements.contains(SelectedElement.forField(classId, fieldId));
	}

	private boolean isInCommentResizeHandle(final Rectangle2D bounds, final Point2D.Double worldPoint) {
		return worldPoint.getX() >= bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE
				&& worldPoint.getY() >= bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE;
	}

	private boolean isLinkAttachedToMovingObject(final LinkModel linkModel) {
		if (linkModel == null || linkModel.getFrom() == null || linkModel.getTo() == null) {
			return false;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return this.movingClassIds.contains(linkModel.getFrom().getClassId())
					|| this.movingClassIds.contains(linkModel.getTo().getClassId());
		}

		return this.movingClassIds.contains(linkModel.getFrom().getClassId())
				|| this.movingClassIds.contains(linkModel.getTo().getClassId());
	}

	private boolean isLinkBetweenMovingObjects(final LinkModel linkModel) {
		if (linkModel == null || linkModel.getFrom() == null || linkModel.getTo() == null) {
			return false;
		}

		return this.movingClassIds.contains(linkModel.getFrom().getClassId())
				&& this.movingClassIds.contains(linkModel.getTo().getClassId());
	}

	private boolean isLinkSelected(final String linkId) {
		return this.selectedElements.contains(SelectedElement.forLink(linkId));
	}

	private boolean isMovingClass(final String classId) {
		return this.movingClassIds.contains(classId);
	}

	private boolean isMovingComment(final String commentId) {
		return this.movingCommentIds.contains(commentId);
	}

	private boolean isMovingLink(final String linkId) {
		return this.movingLinkIds.contains(linkId);
	}

	private boolean isPointNearGeometry(final Point2D.Double worldPoint, final LinkGeometry geometry) {
		for (int i = 0; i < geometry.points().size() - 1; i++) {
			final Point2D first = geometry.points().get(i);
			final Point2D second = geometry.points().get(i + 1);

			if (Line2D.ptSegDist(first.getX(),
					first.getY(),
					second.getX(),
					second.getY(),
					worldPoint.getX(),
					worldPoint.getY()) <= DiagramCanvas.LINK_HIT_DISTANCE) {
				return true;
			}
		}

		return false;
	}

	private boolean isSelfLink(final LinkModel linkModel) {
		return linkModel.getFrom() != null && linkModel.getTo() != null
				&& Objects.equals(linkModel.getFrom().getClassId(), linkModel.getTo().getClassId());
	}

	private boolean isValidPreviewTarget(final SelectedElement target) {
		if (target == null || this.linkCreationState == null) {
			return false;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return target.type() == SelectedType.CLASS && !Objects.equals(target.classId(), this.linkCreationState.classId());
		}

		if ((target.type() != SelectedType.FIELD) || (Objects.equals(target.classId(), this.linkCreationState.classId())
				&& Objects.equals(target.fieldId(), this.linkCreationState.fieldId()))) {
			return false;
		}

		final FieldModel targetField = this.findFieldById(target.classId(), target.fieldId());
		if (targetField == null || !targetField.isPrimaryKey()) {
			return false;
		}

		return !this.hasOutgoingTechnicalLink(this.linkCreationState.classId(), this.linkCreationState.fieldId());
	}

	private boolean isVisible(final ClassModel classModel) {
		return switch (this.panelType) {
		case CONCEPTUAL -> classModel.getVisibility().isConceptual();
		case LOGICAL -> classModel.getVisibility().isLogical();
		case PHYSICAL -> classModel.getVisibility().isPhysical();
		};
	}

	private void moveFieldSelection(final int delta) {
		if (this.selectedElement != null && this.selectedElement.type() == SelectedType.CLASS) {
			final ClassModel classModel = this.findClassById(this.selectedElement.classId);
			if (classModel.getFields().isEmpty()) {
				return;
			}
			this.select(SelectedElement.forField(this.selectedElement.classId, classModel.getFields().get(0).getId()));
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
			this.select(SelectedElement.forClass(classModel.getId()));
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

	private SelectedElement normalizeLinkEndpointSelection(final SelectedElement selection) {
		if (selection == null) {
			return null;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return switch (selection.type()) {
			case CLASS -> SelectedElement.forClass(selection.classId());
			case FIELD -> SelectedElement.forClass(selection.classId());
			default -> null;
			};
		}

		return selection.type() == SelectedType.FIELD ? selection : null;
	}

	private void notifySelectionChanged() {
		if (this.statusListener != null) {
			this.statusListener.onSelectionChanged(this.getSelectionInfo());
		}
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

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		this.ensureLayouts();

		final Graphics2D g2 = (Graphics2D) graphics.create();
		this.configureGraphics(g2);

		if (this.isDragRenderingActive()) {
			g2.drawImage(this.staticDragLayer, 0, 0, null);

			final Graphics2D translated = (Graphics2D) g2.create();
			try {
				translated.translate(this.currentDragOffset.getX(), this.currentDragOffset.getY());
				translated.drawImage(this.movingDragLayer, 0, 0, null);
			} finally {
				translated.dispose();
			}

			final AffineTransform oldTransform = g2.getTransform();
			final PanelState state = this.getPanelState();
			g2.translate(state.getPanX(), state.getPanY());
			g2.scale(state.getZoom(), state.getZoom());

			this.drawLinks(g2, RenderPass.TRANSITION);
			this.drawLinkPreview(g2);

			g2.setTransform(oldTransform);
			g2.dispose();
			return;
		}

		this.drawGrid(g2);

		final AffineTransform oldTransform = g2.getTransform();
		final PanelState state = this.getPanelState();
		g2.translate(state.getPanX(), state.getPanY());
		g2.scale(state.getZoom(), state.getZoom());

		this.drawComments(g2, RenderPass.FULL);
		this.drawClasses(g2, RenderPass.FULL);
		this.drawLinks(g2, RenderPass.FULL);
		this.drawLinkPreview(g2);

		g2.setTransform(oldTransform);
		g2.dispose();
	}

	private void removeFromSelection(final SelectedElement element) {
		if (element == null) {
			return;
		}

		this.selectedElements.remove(element);

		if (Objects.equals(this.selectedElement, element)) {
			this.selectedElement = this.selectedElements.isEmpty() ? null : this.selectedElements.getLast();
		}

		this.notifySelectionChanged();
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
			final FieldModel fieldModel = this.findFieldById(this.selectedElement.classId(), this.selectedElement.fieldId());
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
			if (this.panelType != PanelType.CONCEPTUAL) {
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
		case FIELD ->
			this.setEditableFieldName(this.findFieldById(this.selectedElement.classId(), this.selectedElement.fieldId()), newValue);
		case COMMENT -> this.setEditableCommentText(this.selectedElement.commentId(), newValue);
		case LINK -> this.findLinkById(this.selectedElement.linkId()).setName(newValue);
		default -> {
		}
		}

		this.notifySelectionChanged();
		this.repaint();
	}

	private String resolveClassTitle(final ClassModel classModel) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			return this
					.blankToFallback(classModel.getNames().getConceptualName(), classModel.getNames().getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getNames().getTechnicalName(), classModel.getNames().getConceptualName(), "Unnamed class");
	}

	private String resolveCommentText(final CommentModel commentModel) {
		return commentModel == null ? ""
				: commentModel.getText() == null ? ""
				: commentModel.getText();
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

		for (final AnchorCandidate fromCandidate : this.computeConceptualCandidates(g2, targetLink, fromClass.getId(), fromBounds)) {
			for (final AnchorCandidate toCandidate : this.computeConceptualCandidates(g2, targetLink, toClass.getId(), toBounds)) {
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

	private Point2D resolveConceptualPreviewAnchor(final Graphics2D g2, final String classId, final Point2D reference) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);

		final Point2D effectiveReference = reference == null ? new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()) : reference;

		final List<Point2D> candidates = Arrays.asList(new Point2D.Double(bounds.getCenterX(), bounds.getY()),
				new Point2D.Double(bounds.getCenterX(), bounds.getMaxY()),
				new Point2D.Double(bounds.getX(), bounds.getCenterY()),
				new Point2D.Double(bounds.getMaxX(), bounds.getCenterY()));

		Point2D best = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (final Point2D candidate : candidates) {
			final double distance = candidate.distance(effectiveReference);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = candidate;
			}
		}

		return best;
	}

	private String resolveFieldName(final FieldModel fieldModel) {
		final String baseName;
		if (this.panelType == PanelType.CONCEPTUAL) {
			baseName = this.blankToFallback(fieldModel.getNames().getName(), fieldModel.getNames().getTechnicalName(), "Unnamed field");
		} else {
			baseName = this.blankToFallback(fieldModel.getNames().getTechnicalName(), fieldModel.getNames().getName(), "Unnamed field");
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
			fromPoint = this.resolveTechnicalFieldAnchor(g2,
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId(),
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId());
			toPoint = this.resolveTechnicalFieldAnchor(g2,
					linkModel.getTo().getClassId(),
					linkModel.getTo().getFieldId(),
					linkModel.getFrom().getClassId(),
					linkModel.getFrom().getFieldId());
		}

		final Point2D adjustedFromPoint = this.applyCurrentDragOffsetIfNeeded(fromPoint, linkModel.getFrom().getClassId());
		final Point2D adjustedToPoint = this.applyCurrentDragOffsetIfNeeded(toPoint, linkModel.getTo().getClassId());

		if (adjustedFromPoint == null || adjustedToPoint == null) {
			return null;
		}

		final List<Point2D> points;
		if (this.isSelfLink(linkModel)) {
			points = this.buildSelfLinkPoints(g2, linkModel, adjustedFromPoint, adjustedToPoint);
		} else {
			points = new ArrayList<>();
			points.add(adjustedFromPoint);

			final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkModel.getId());
			for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
				points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
			}

			points.add(adjustedToPoint);
		}

		final Point2D middlePoint = this.computePolylineMiddlePoint(points);
		final double labelAngle = this.computeUprightAngleAtMiddle(points);

		final Point2D labelPoint;
		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkModel.getId());
		if (linkLayout.getNameLabelPosition() != null) {
			labelPoint = new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());
		} else {
			labelPoint = middlePoint;
		}

		return new LinkGeometry(adjustedFromPoint, adjustedToPoint, labelPoint, middlePoint, labelAngle, points);
	}

	private NodeLayout resolveNodeLayoutForSelection(final SelectedElement element, final NodeLayout fallbackLayout) {
		if (element == null) {
			return fallbackLayout;
		}

		return switch (element.type()) {
		case CLASS, FIELD -> this.findNodeLayout(LayoutObjectType.CLASS, element.classId());
		case COMMENT -> this.findNodeLayout(LayoutObjectType.COMMENT, element.commentId());
		default -> fallbackLayout;
		};
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
				final double y = classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT
						+ DiagramCanvas.ROW_HEIGHT / 2.0;
				return new Point2D.Double(classBounds.getCenterX(), y);
			}
		}

		return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
	}

	private Point2D resolvePreviewSourceAnchor(final Graphics2D g2) {
		if (this.linkCreationState == null) {
			return null;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.linkPreviewTarget != null ? this.resolvePreviewTargetAnchor(g2, this.linkPreviewTarget)
					: this.linkPreviewMousePoint;
			return this.resolveConceptualPreviewAnchor(g2, this.linkCreationState.classId(), reference);
		}

		final Point2D reference = this.linkPreviewTarget != null ? this.resolvePreviewTargetAnchor(g2, this.linkPreviewTarget)
				: this.linkPreviewMousePoint;

		final String oppositeClassId = this.linkPreviewTarget == null ? null : this.linkPreviewTarget.classId();
		final String oppositeFieldId = this.linkPreviewTarget == null ? null : this.linkPreviewTarget.fieldId();

		if (oppositeClassId != null) {
			return this.resolveTechnicalFieldAnchor(g2,
					this.linkCreationState.classId(),
					this.linkCreationState.fieldId(),
					oppositeClassId,
					oppositeFieldId);
		}

		return this.resolveTechnicalFieldAnchor(g2, this.linkCreationState.classId(), this.linkCreationState.fieldId(), reference);
	}

	private Point2D resolvePreviewSourceAnchorReference(final Graphics2D g2) {
		if (this.linkCreationState == null) {
			return this.linkPreviewMousePoint;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final ClassModel classModel = this.findClassById(this.linkCreationState.classId());
			if (classModel == null || !this.isVisible(classModel)) {
				return this.linkPreviewMousePoint;
			}

			final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		return this.resolveOppositeReferencePoint(g2, this.linkCreationState.classId(), this.linkCreationState.fieldId());
	}

	private Point2D resolvePreviewTargetAnchor(final Graphics2D g2, final SelectedElement target) {
		if (target == null) {
			return null;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.resolvePreviewSourceAnchorReference(g2);
			return this.resolveConceptualPreviewAnchor(g2, target.classId(), reference);
		}

		return this.resolveTechnicalFieldAnchor(g2,
				target.classId(),
				target.fieldId(),
				this.linkCreationState.classId(),
				this.linkCreationState.fieldId());
	}

	private Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final Point2D oppositeReference) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId);
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);

		if (fieldId == null) {
			final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);
				final double x = oppositeReference.getX() < fieldBounds.getCenterX() ? fieldBounds.getX() : fieldBounds.getMaxX();
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
		return new Point2D.Double(x, classBounds.getCenterY());
	}

	private Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId) {
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
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);

				final Point2D left = new Point2D.Double(fieldBounds.getX(), fieldBounds.getCenterY());
				final Point2D right = new Point2D.Double(fieldBounds.getMaxX(), fieldBounds.getCenterY());

				return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
			}
		}

		final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
		final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
		return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
	}

	private Point2D.Double screenToWorld(final Point point) {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((point.getX() - state.getPanX()) / state.getZoom(), (point.getY() - state.getPanY()) / state.getZoom());
	}

	private void select(final SelectedElement element) {
		this.selectedElements.clear();
		if (element != null) {
			this.selectedElements.add(element);
		}
		this.document.getModel().getClasses().sort(this.comparator);
		this.selectedElement = element;
		this.notifySelectionChanged();
		this.repaint();
	}

	public void setDefaultPalette(final StylePalette defaultPalette) {
		this.defaultPalette = defaultPalette;
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

	private void updateSelectionFromMouse(final SelectedElement element, final MouseEvent event) {
		if (element == null) {
			if (!event.isShiftDown() && !event.isControlDown()) {
				this.clearSelection();
			}
			return;
		}

		if (event.isShiftDown()) {
			this.addToSelection(element);
			return;
		}

		if (event.isControlDown()) {
			this.removeFromSelection(element);
			return;
		}

		this.select(element);
	}

	private Point2D.Double viewportCenterWorld() {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((this.getWidth() / 2.0 - state.getPanX()) / state.getZoom(),
				(this.getHeight() / 2.0 - state.getPanY()) / state.getZoom());
	}

	private Color withAlpha(final Color color, final int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
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

}
