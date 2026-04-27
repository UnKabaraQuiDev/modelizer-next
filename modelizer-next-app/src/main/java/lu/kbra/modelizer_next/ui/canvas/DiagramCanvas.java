package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
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

import javax.swing.Action;
import javax.swing.JPanel;

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
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSidePair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClassSideKey;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLinkLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedNodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedSelection;
import lu.kbra.modelizer_next.ui.canvas.datastruct.HitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkCreationState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ResizingComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;
import lu.kbra.modelizer_next.ui.dialogs.ClassEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.CommentEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.FieldEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;

public class DiagramCanvas extends JPanel implements DiagramModelLookup, LayoutCache, SelectionController, NameResolver, PaletteController,
		ClipboardController, LinkGeometryResolver, ConceptualAnchorCache, CanvasHitTester, CanvasExportRenderer, DiagramModelEditor,
		DragSelectionController, DiagramPathBuilder, MouseInteractionController {

	static final double PASTE_OFFSET = 30.0;

	static ClipboardSnapshot clipboardSnapshot;

	static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	static final int CLASS_MIN_WIDTH = 140;

	static final int COMMENT_MIN_WIDTH = 120;
	static final int HEADER_HEIGHT = 28;
	static final int ROW_HEIGHT = 22;

	static final int PADDING = 8;
	static final int COMMENT_RESIZE_HANDLE_SIZE = 12;
	static final int COMMENT_MIN_HEIGHT = 40;

	static final int COMMENT_MIN_WIDTH_VALUE = 120;
	static final Color CANVAS_BACKGROUND_COLOR = new Color(0xF2F2F2);
	static final Color GRID_COLOR = new Color(0xE4E4E4);
	static final Color SELECTION_COLOR = new Color(0x2F7DFF);
	static final Color SELECTION_FILL_COLOR = new Color(DiagramCanvas.SELECTION_COLOR.getRed(),
			DiagramCanvas.SELECTION_COLOR.getGreen(),
			DiagramCanvas.SELECTION_COLOR.getBlue(),
			60);
	static final Color COMMENT_CONNECTOR_COLOR = new Color(0x777777);
	static final BasicStroke DEFAULT_STROKE = new BasicStroke(1.0f);

	static final BasicStroke FIELD_SELECTION_STROKE = new BasicStroke(2.0f);
	static final BasicStroke SELECTION_STROKE = new BasicStroke(2.5f);
	static final BasicStroke LINK_DEFAULT_STROKE = new BasicStroke(1.2f);
	static final BasicStroke COMMENT_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f);
	static final BasicStroke ASSOCIATION_CONNECTOR_DEFAULT_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	static final BasicStroke ASSOCIATION_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	static final BasicStroke LINK_PREVIEW_STROKE = new BasicStroke(1.5f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 6.0f, 6.0f },
			0.0f);
	static final double LINK_HIT_DISTANCE = 6.0;

	static final double CONCEPTUAL_ANCHOR_SPACING = 18.0;
	static final double SELF_LINK_OUTSIDE_OFFSET = 40.0;
	static final int EXPORT_MARGIN = 32;
	static final int DEFAULT_EXPORT_WIDTH = 1200;
	static final int DEFAULT_EXPORT_HEIGHT = 800;

	final ModelDocument document;

	final PanelType panelType;
	final DocumentChangeListener documentEventListener;
	DraggedSelection draggedSelection;

	Point lastScreenPoint;
	boolean panning;
	LinkCreationState linkCreationState;
	SelectedElement linkPreviewTarget;

	Point2D.Double linkPreviewMousePoint;
	SelectedElement selectedElement;
	final LinkedHashSet<SelectedElement> selectedElements = new LinkedHashSet<>();

	ResizingComment resizingComment;

	StylePalette defaultPalette;
	SelectedElement pendingClickSelection;
	boolean pendingModifierSelection;

	boolean dragOccurred;

	Point2D.Double currentDragOffset = new Point2D.Double();
	final Map<String, AnchorPair> conceptualAnchorCache = new HashMap<>();
	final Map<String, LinkAnchorPlacement> conceptualAnchorPlacements = new HashMap<>();
	final Map<ClassSideKey, List<String>> conceptualSideLinkCache = new HashMap<>();
	boolean conceptualAnchorCacheValid;
	LinkedHashSet<SelectedElement> exportSelectionFilter;
	boolean suppressSelectionDecorations;
	boolean suppressInteractiveOverlays;
	final Comparator<ClassModel> comparator = (a, b) -> {
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

	public DiagramCanvas(final ModelDocument document, final PanelType panelType, final DocumentChangeListener documentEventListener) {
		this.document = document;
		this.panelType = panelType;
		this.documentEventListener = documentEventListener;

		this.setBackground(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
		this.setOpaque(true);
		this.setFocusable(true);

		this.installKeyBindings();

		final MouseAdapter mouseAdapter = createMouseAdapter();
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		this.addMouseWheelListener(mouseAdapter);
	}

	public Action getCanvasAction(final String actionKey) {
		return this.getActionMap().get(actionKey);
	}

	@Override
	public ModelDocument getDocument() {
		return document;
	}

	@Override
	public DiagramCanvas getCanvas() {
		return this;
	}

	@Override
	public PanelType getPanelType() {
		return this.panelType;
	}

	public void resetUiAfterDocumentRestore() {
		this.draggedSelection = null;
		this.lastScreenPoint = null;
		this.panning = false;
		this.linkCreationState = null;
		this.linkPreviewTarget = null;
		this.linkPreviewMousePoint = null;
		this.selectedElement = null;
		this.selectedElements.clear();
		this.resizingComment = null;

		this.pendingClickSelection = null;
		this.pendingModifierSelection = false;
		this.dragOccurred = false;

		this.currentDragOffset = new Point2D.Double();

		this.setCursor(Cursor.getDefaultCursor());
		this.notifySelectionChanged();
		this.revalidate();
		this.repaint();
	}

	String appendSuffix(final String value, final String suffix) {
		if (value == null || value.isBlank()) {
			return value;
		}
		return value + suffix;
	}

	void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double offset) {

		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + offset, bendPoint.getY() + offset));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + offset, copiedLayout.nameLabelPosition().getY() + offset));
		}
	}

	void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double deltaX, final double deltaY) {

		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkId);

		linkLayout.getBendPoints().clear();

		for (final Point2D.Double bendPoint : copiedLayout.bendPoints()) {
			linkLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + deltaX, bendPoint.getY() + deltaY));
		}

		if (copiedLayout.nameLabelPosition() != null) {
			linkLayout.setNameLabelPosition(
					new Point2D.Double(copiedLayout.nameLabelPosition().getX() + deltaX, copiedLayout.nameLabelPosition().getY() + deltaY));
		}
	}

	void applyNodeLayout(
			final LayoutObjectType type,
			final String objectId,
			final CopiedNodeLayout copiedLayout,
			final double deltaX,
			final double deltaY) {

		final NodeLayout layout = this.findOrCreateNodeLayout(type, objectId);

		layout.setPosition(new Point2D.Double(copiedLayout.x() + deltaX, copiedLayout.y() + deltaY));

		layout.setSize(new Size2D(copiedLayout.width(), copiedLayout.height()));
	}

	void bindCommentToTarget(final String commentId, final SelectedElement target) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null || target == null) {
			return;
		}

		commentModel.setKind(CommentKind.BOUND);
		commentModel.setBinding(switch (target.type()) {
		case CLASS, FIELD -> new CommentBinding(BoundTargetType.CLASS, target.classId());
		case LINK -> new CommentBinding(BoundTargetType.LINK, target.linkId());
		default -> throw new IllegalStateException("Cannot bind comment to: " + target);
		});

		this.select(SelectedElement.forComment(commentId));
		this.notifyDocumentChanged();
	}

	CopiedClass captureClass(final ClassModel classModel) {
		final List<CopiedField> fields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			fields.add(this.captureField(classModel.getId(), fieldModel));
		}

		return new CopiedClass(classModel.getId(),
				classModel.getNames().getConceptualName(),
				classModel.getNames().getTechnicalName(),
				classModel.getGroup(),
				classModel.getVisibility().isConceptual(),
				classModel.getVisibility().isLogical(),
				classModel.getVisibility().isPhysical(),
				classModel.getStyle().getTextColor(),
				classModel.getStyle().getBackgroundColor(),
				classModel.getStyle().getBorderColor(),
				List.copyOf(fields),
				this.captureNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
	}

	CopiedComment captureComment(final CommentModel commentModel) {
		final CommentBinding binding = commentModel.getBinding();

		return new CopiedComment(commentModel.getId(),
				commentModel.getKind(),
				commentModel.getText(),
				commentModel.getTextColor(),
				commentModel.getBackgroundColor(),
				commentModel.getBorderColor(),
				commentModel.isVisibleInConceptual(),
				commentModel.isVisibleInLogical(),
				commentModel.isVisibleInPhysical(),
				binding == null ? null : binding.getTargetType(),
				binding == null ? null : binding.getTargetId(),
				this.captureNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
	}

	CopiedField captureField(final String ownerClassId, final FieldModel fieldModel) {
		return new CopiedField(ownerClassId,
				fieldModel.getId(),
				fieldModel.getNames().getConceptualName(),
				fieldModel.getNames().getTechnicalName(),
				fieldModel.isNotConceptual(),
				fieldModel.getComment(),
				fieldModel.isPrimaryKey(),
				fieldModel.isUnique(),
				fieldModel.isNotNull(),
				fieldModel.getType(),
				fieldModel.getStyle().getTextColor(),
				fieldModel.getStyle().getBackgroundColor());
	}

	CopiedLink captureLink(final LinkModel linkModel) {
		final LinkEnd from = linkModel.getFrom();
		final LinkEnd to = linkModel.getTo();

		return new CopiedLink(linkModel.getId(),
				linkModel.getName(),
				linkModel.getLineColor(),
				linkModel.getAssociationClassId(),
				from == null ? null : from.getClassId(),
				from == null ? null : from.getFieldId(),
				to == null ? null : to.getClassId(),
				to == null ? null : to.getFieldId(),
				linkModel.getCardinalityFrom(),
				linkModel.getCardinalityTo(),
				linkModel.getLabelFrom(),
				linkModel.getLabelTo(),
				this.captureLinkLayout(linkModel.getId()));
	}

	CopiedLinkLayout captureLinkLayout(final String linkId) {
		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkId);
		final List<Point2D.Double> bendPoints = new ArrayList<>();

		for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
			bendPoints.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
		}

		final Point2D.Double labelPosition = linkLayout.getNameLabelPosition() == null ? null
				: new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());

		return new CopiedLinkLayout(List.copyOf(bendPoints), labelPosition);
	}

	CopiedNodeLayout captureNodeLayout(final LayoutObjectType type, final String objectId) {
		final NodeLayout layout = this.findOrCreateNodeLayout(type, objectId);

		return new CopiedNodeLayout(layout.getPosition().getX(),
				layout.getPosition().getY(),
				layout.getSize().getWidth(),
				layout.getSize().getHeight());
	}

	AnchorSidePair chooseBestConceptualSidePair(
			final String fromClassId,
			final Rectangle2D fromBounds,
			final String toClassId,
			final Rectangle2D toBounds) {

		AnchorSidePair bestPair = new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT);
		double bestScore = Double.POSITIVE_INFINITY;

		final List<AnchorSidePair> allowedPairs = List.of(new AnchorSidePair(AnchorSide.LEFT, AnchorSide.RIGHT),
				new AnchorSidePair(AnchorSide.RIGHT, AnchorSide.LEFT),
				new AnchorSidePair(AnchorSide.TOP, AnchorSide.BOTTOM),
				new AnchorSidePair(AnchorSide.BOTTOM, AnchorSide.TOP));

		for (final AnchorSidePair pair : allowedPairs) {
			final Point2D fromCenter = this.computeConceptualSideCenter(fromBounds, pair.fromSide());
			final Point2D toCenter = this.computeConceptualSideCenter(toBounds, pair.toSide());

			final double distance = fromCenter.distance(toCenter);
			final double loadPenalty = (this.getConceptualSideLinkCount(fromClassId, pair.fromSide())
					+ this.getConceptualSideLinkCount(toClassId, pair.toSide())) * 12.0;

			final double score = distance + loadPenalty;

			if (score < bestScore) {
				bestScore = score;
				bestPair = pair;
			}
		}

		return bestPair;
	}

	AnchorSide chooseSelfLinkFromSide(final String classId) {
		AnchorSide bestSide = AnchorSide.TOP;
		int bestCount = Integer.MAX_VALUE;

		for (final AnchorSide side : AnchorSide.values()) {
			final int sideCount = this.getConceptualSideLinkCount(classId, side);
			if (sideCount < bestCount) {
				bestCount = sideCount;
				bestSide = side;
			}
		}

		return bestSide;
	}

	AnchorSide chooseTechnicalSelfLinkSide(final Graphics2D g2, final LinkModel linkModel) {
		final String classId = linkModel.getFrom().getClassId();
		final int leftCount = this.getTechnicalSideLinkCount(g2, classId, AnchorSide.LEFT, linkModel.getId());
		final int rightCount = this.getTechnicalSideLinkCount(g2, classId, AnchorSide.RIGHT, linkModel.getId());
		return leftCount <= rightCount ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(max, value));
	}

	AnchorSide clockwise(final AnchorSide side) {
		return switch (side) {
		case TOP -> AnchorSide.RIGHT;
		case RIGHT -> AnchorSide.BOTTOM;
		case BOTTOM -> AnchorSide.LEFT;
		case LEFT -> AnchorSide.TOP;
		};
	}

	Rectangle2D computeClassBounds(final Graphics2D g2, final ClassModel classModel, final NodeLayout layout) {
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

	Rectangle2D.Double computeClipboardBounds(final ClipboardSnapshot clipboard) {
		Rectangle2D.Double bounds = null;

		for (final CopiedClass copiedClass : clipboard.classes()) {
			final CopiedNodeLayout layout = copiedClass.layout();
			bounds = this.expandBounds(bounds, layout.x(), layout.y(), layout.width(), layout.height());
		}

		for (final CopiedComment copiedComment : clipboard.comments()) {
			final CopiedNodeLayout layout = copiedComment.layout();
			bounds = this.expandBounds(bounds, layout.x(), layout.y(), layout.width(), layout.height());
		}

		if (bounds != null) {
			return bounds;
		}

		for (final CopiedLink copiedLink : clipboard.links()) {
			final CopiedLinkLayout layout = copiedLink.layout();

			for (final Point2D.Double bendPoint : layout.bendPoints()) {
				bounds = this.expandBounds(bounds, bendPoint.getX(), bendPoint.getY(), 1.0, 1.0);
			}

			if (layout.nameLabelPosition() != null) {
				bounds = this.expandBounds(bounds, layout.nameLabelPosition().getX(), layout.nameLabelPosition().getY(), 1.0, 1.0);
			}
		}

		return bounds;
	}

	Rectangle2D computeCommentBounds(final Graphics2D g2, final String text, final NodeLayout layout) {
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

	double computeConceptualSortValue(
			final String linkId,
			final String classId,
			final AnchorSide side,
			final Map<String, Rectangle2D> boundsByClassId,
			final Map<String, AnchorSidePair> sidePairs) {
		final LinkModel linkModel = this.findLinkById(linkId);
		if (linkModel == null) {
			return 0.0;
		}

		final AnchorSidePair sidePair = sidePairs.get(linkId);
		if (sidePair == null) {
			return 0.0;
		}

		final boolean fromEndpoint = classId.equals(linkModel.getFrom().getClassId()) && side == sidePair.fromSide();
		final boolean toEndpoint = classId.equals(linkModel.getTo().getClassId()) && side == sidePair.toSide();
		if (!fromEndpoint && !toEndpoint) {
			return 0.0;
		}

		if (linkModel.isSelfLinking()) {
			final Rectangle2D bounds = boundsByClassId.get(classId);
			if (bounds == null) {
				return 0.0;
			}

			final AnchorSide oppositeSide = fromEndpoint ? sidePair.toSide() : sidePair.fromSide();
			final Point2D oppositePoint = this.computeConceptualSideCenter(bounds, oppositeSide);
			return switch (side) {
			case TOP, BOTTOM -> oppositePoint.getX();
			case LEFT, RIGHT -> oppositePoint.getY();
			};
		}

		final String otherClassId = fromEndpoint ? linkModel.getTo().getClassId() : linkModel.getFrom().getClassId();
		final Rectangle2D otherBounds = boundsByClassId.get(otherClassId);
		if (otherBounds == null) {
			return 0.0;
		}

		return switch (side) {
		case TOP, BOTTOM -> otherBounds.getCenterX();
		case LEFT, RIGHT -> otherBounds.getCenterY();
		};
	}

	Rectangle2D.Double computeExportContentBounds(final Graphics2D g2, final ViewExportScope scope) {
		final LinkedHashSet<SelectedElement> previousFilter = this.exportSelectionFilter;
		if (scope == ViewExportScope.SELECTION && this.exportSelectionFilter == null) {
			this.exportSelectionFilter = new LinkedHashSet<>(this.selectedElements);
		}

		try {
			this.ensureLayouts();

			if (this.panelType == PanelType.CONCEPTUAL) {
				this.invalidateConceptualAnchorCache();
				this.ensureConceptualAnchorCache(g2);
			}

			Rectangle2D.Double bounds = null;
			final boolean onlySelection = scope == ViewExportScope.SELECTION;

			for (final ClassModel classModel : this.document.getModel().getClasses()) {
				if (!this.isVisible(classModel) || onlySelection && !this.shouldExportClass(classModel)) {
					continue;
				}

				final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
				final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);
				bounds = this.expandBounds(bounds, classBounds.getX(), classBounds.getY(), classBounds.getWidth(), classBounds.getHeight());
			}

			for (final CommentModel commentModel : this.document.getModel().getComments()) {
				final String text = this.resolveCommentText(commentModel);
				if (text == null || text.isBlank() || !this.isCommentVisible(commentModel)
						|| onlySelection && !this.shouldExportComment(commentModel)) {
					continue;
				}

				final NodeLayout layout = this
						.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
				final Rectangle2D commentBounds = this.computeCommentBounds(g2, text, layout);
				bounds = this.expandBounds(bounds,
						commentBounds.getX(),
						commentBounds.getY(),
						commentBounds.getWidth(),
						commentBounds.getHeight());

				final Point2D connectorAnchor = this.findBoundTargetAnchor(g2, commentModel);
				if (connectorAnchor != null) {
					bounds = this.expandBounds(bounds,
							Math.min(connectorAnchor.getX(), commentBounds.getCenterX()),
							Math.min(connectorAnchor.getY(), commentBounds.getCenterY()),
							Math.abs(connectorAnchor.getX() - commentBounds.getCenterX()),
							Math.abs(connectorAnchor.getY() - commentBounds.getCenterY()));
				}
			}

			for (final LinkModel linkModel : this.getActiveLinks()) {
				if (onlySelection && !this.shouldExportLink(linkModel)) {
					continue;
				}

				final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);
				if (geometry == null) {
					continue;
				}

				for (final Point2D point : geometry.points()) {
					bounds = this.expandBounds(bounds, point.getX(), point.getY(), 1.0, 1.0);
				}

				if (geometry.labelPoint() != null) {
					bounds = this.expandBounds(bounds, geometry.labelPoint().getX() - 60, geometry.labelPoint().getY() - 20, 120, 40);
				}
			}

			return bounds;
		} finally {
			this.exportSelectionFilter = previousFilter;
		}
	}

	Dimension computeExportSize(final Graphics2D g2, final ViewExportScope scope) {
		if (scope == ViewExportScope.VIEW) {
			return this.getViewportExportSize();
		}

		final Rectangle2D.Double contentBounds = this.computeExportContentBounds(g2, scope);
		if (contentBounds == null) {
			return this.getViewportExportSize();
		}

		return new Dimension(Math.max(1, (int) Math.ceil(contentBounds.getWidth() + DiagramCanvas.EXPORT_MARGIN * 2.0)),
				Math.max(1, (int) Math.ceil(contentBounds.getHeight() + DiagramCanvas.EXPORT_MARGIN * 2.0)));
	}

	Rectangle2D.Double computeExportWorldBounds(final Graphics2D g2, final ViewExportScope scope) {
		if (scope == ViewExportScope.VIEW) {
			final PanelState state = this.getPanelState();
			final Dimension viewportSize = this.getViewportExportSize();
			return new Rectangle2D.Double(-state.getPanX() / state.getZoom(),
					-state.getPanY() / state.getZoom(),
					viewportSize.getWidth() / state.getZoom(),
					viewportSize.getHeight() / state.getZoom());
		}

		final Rectangle2D.Double contentBounds = this.computeExportContentBounds(g2, scope);
		if (contentBounds == null) {
			return this.computeExportWorldBounds(g2, ViewExportScope.VIEW);
		}

		return new Rectangle2D.Double(contentBounds.getX() - DiagramCanvas.EXPORT_MARGIN,
				contentBounds.getY() - DiagramCanvas.EXPORT_MARGIN,
				contentBounds.getWidth() + DiagramCanvas.EXPORT_MARGIN * 2.0,
				contentBounds.getHeight() + DiagramCanvas.EXPORT_MARGIN * 2.0);
	}

	Rectangle2D.Double computeSelectionBounds(final List<SelectedElement> selection) {
		Rectangle2D.Double bounds = null;
		final Set<String> seenNodeLayouts = new HashSet<>();

		for (final SelectedElement element : selection) {
			if (element == null) {
				continue;
			}

			if (element.type() == SelectedType.CLASS || element.type() == SelectedType.FIELD) {
				final String key = LayoutObjectType.CLASS + ":" + element.classId();

				if (!seenNodeLayouts.add(key)) {
					continue;
				}

				final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.CLASS, element.classId());
				bounds = this.expandBounds(bounds,
						layout.getPosition().getX(),
						layout.getPosition().getY(),
						layout.getSize().getWidth(),
						layout.getSize().getHeight());
			} else if (element.type() == SelectedType.COMMENT) {
				final String key = LayoutObjectType.COMMENT + ":" + element.commentId();

				if (!seenNodeLayouts.add(key)) {
					continue;
				}

				final NodeLayout layout = this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, element.commentId());
				bounds = this.expandBounds(bounds,
						layout.getPosition().getX(),
						layout.getPosition().getY(),
						layout.getSize().getWidth(),
						layout.getSize().getHeight());
			} else if (element.type() == SelectedType.LINK) {
				final LinkLayout layout = this.findOrCreateLinkLayout(element.linkId());

				for (final Point2D.Double bendPoint : layout.getBendPoints()) {
					bounds = this.expandBounds(bounds, bendPoint.getX(), bendPoint.getY(), 1.0, 1.0);
				}

				if (layout.getNameLabelPosition() != null) {
					bounds = this
							.expandBounds(bounds, layout.getNameLabelPosition().getX(), layout.getNameLabelPosition().getY(), 1.0, 1.0);
				}
			}
		}

		return bounds;
	}

	void configureGraphics(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	void createConceptualLink(final String fromClassId, final String toClassId) {
		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(fromClassId, null));
		linkModel.setTo(new LinkEnd(toClassId, null));
		linkModel.setCardinalityFrom(Cardinality.ONE);
		linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		this.applyDefaultPaletteToLink(linkModel);
		this.document.getModel().getConceptualLinks().add(linkModel);

		this.findOrCreateLinkLayout(linkModel.getId());
		this.select(SelectedElement.forLink(linkModel.getId()));
		this.notifyDocumentChanged();
	}

	FieldModel createFieldFromClipboard(final CopiedField copiedField, final boolean rename) {
		final FieldModel fieldCopy = new FieldModel();

		fieldCopy.getNames().setConceptualName(rename ? this.appendSuffix(copiedField.name(), " Copy") : copiedField.name());
		fieldCopy.getNames()
				.setTechnicalName(rename ? this.appendSuffix(copiedField.technicalName(), "_COPY") : copiedField.technicalName());

		fieldCopy.setNotConceptual(copiedField.notConceptual());
		fieldCopy.setComment(copiedField.comment());
		fieldCopy.setPrimaryKey(copiedField.primaryKey());
		fieldCopy.setUnique(copiedField.unique());
		fieldCopy.setNotNull(copiedField.notNull());
		fieldCopy.setType(copiedField.type());

		fieldCopy.getStyle().setTextColor(copiedField.textColor());
		fieldCopy.getStyle().setBackgroundColor(copiedField.backgroundColor());

		return fieldCopy;
	}

	Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
	}

	LinkModel createLinkFromClipboard(
			final CopiedLink copiedLink,
			final Map<String, String> classIdMap,
			final Map<String, String> fieldIdMap) {

		final String fromClassId = this.mapId(classIdMap, copiedLink.fromClassId());
		final String fromFieldId = this.mapId(fieldIdMap, copiedLink.fromFieldId());
		final String toClassId = this.mapId(classIdMap, copiedLink.toClassId());
		final String toFieldId = this.mapId(fieldIdMap, copiedLink.toFieldId());

		if (!this.linkEndpointExists(fromClassId, fromFieldId) || !this.linkEndpointExists(toClassId, toFieldId)) {
			return null;
		}

		String associationClassId = this.mapId(classIdMap, copiedLink.associationClassId());
		if (associationClassId != null && this.findClassById(associationClassId) == null) {
			associationClassId = null;
		}

		final LinkModel linkCopy = new LinkModel();

		linkCopy.setName(copiedLink.name());
		linkCopy.setLineColor(copiedLink.lineColor());
		linkCopy.setAssociationClassId(associationClassId);
		linkCopy.setFrom(new LinkEnd(fromClassId, fromFieldId));
		linkCopy.setTo(new LinkEnd(toClassId, toFieldId));
		linkCopy.setCardinalityFrom(copiedLink.cardinalityFrom());
		linkCopy.setCardinalityTo(copiedLink.cardinalityTo());
		linkCopy.setLabelFrom(copiedLink.labelFrom());
		linkCopy.setLabelTo(copiedLink.labelTo());

		return linkCopy;
	}

	CommentBinding createRemappedCommentBinding(
			final CopiedComment copiedComment,
			final Map<String, String> classIdMap,
			final Map<String, String> linkIdMap) {

		if (copiedComment.bindingTargetType() == null || copiedComment.bindingTargetId() == null) {
			return null;
		}

		final String targetId = switch (copiedComment.bindingTargetType()) {
		case CLASS -> this.mapId(classIdMap, copiedComment.bindingTargetId());
		case LINK -> this.mapId(linkIdMap, copiedComment.bindingTargetId());
		};

		if (copiedComment.bindingTargetType() == BoundTargetType.CLASS && this.findClassById(targetId) == null
				|| copiedComment.bindingTargetType() == BoundTargetType.LINK && this.findLinkById(targetId) == null) {
			return null;
		}

		return new CommentBinding(copiedComment.bindingTargetType(), targetId);
	}

	void createTechnicalLink(final SelectedElement fromEndpoint, final SelectedElement toEndpoint) {
		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(fromEndpoint.classId(), fromEndpoint.fieldId()));
		linkModel.setTo(new LinkEnd(toEndpoint.classId(), toEndpoint.fieldId()));
		linkModel.setCardinalityFrom(null);
		linkModel.setCardinalityTo(null);
		this.applyDefaultPaletteToLink(linkModel);
		this.document.getModel().getTechnicalLinks().add(linkModel);

		this.findOrCreateLinkLayout(linkModel.getId());
		this.select(SelectedElement.forLink(linkModel.getId()));
		this.notifyDocumentChanged();
	}

	void deleteClass(final String classId) {
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

	void deleteComment(final String commentId) {
		this.document.getModel().getComments().removeIf(comment -> comment.getId().equals(commentId));
		this.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	void deleteField(final String classId, final String fieldId) {
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

	void deleteLink(final String linkId) {
		this.getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getConceptualLinks().removeIf(link -> link.getId().equals(linkId));
		this.document.getModel().getTechnicalLinks().removeIf(link -> link.getId().equals(linkId));
		this.getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

	void drawAlignedLinkLabel(final Graphics2D g2, final String text, final Point2D center, final double angle) {
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

	void drawArrowHead(final Graphics2D g2, final Point2D from, final Point2D to) {
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

	void drawAssociationClassConnector(final Graphics2D g2, final LinkModel linkModel, final LinkGeometry geometry) {
		if (this.panelType != PanelType.CONCEPTUAL || !this.hasAssociationClass(linkModel) || geometry == null) {
			return;
		}

		final ClassModel associationClass = this.findClassById(linkModel.getAssociationClassId());
		if (associationClass == null || !this.isVisible(associationClass) || geometry.middlePoint() == null) {
			return;
		}

		final Point2D associationAnchor = this.resolveConceptualPreviewAnchor(g2, associationClass.getId(), geometry.middlePoint());

		if (associationAnchor == null) {
			return;
		}

		final Graphics2D connectorGraphics = (Graphics2D) g2.create();
		try {
			connectorGraphics.setColor(this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			connectorGraphics.setStroke(this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.ASSOCIATION_CONNECTOR_SELECTION_STROKE
					: DiagramCanvas.ASSOCIATION_CONNECTOR_DEFAULT_STROKE);
			connectorGraphics.draw(new Line2D.Double(associationAnchor, geometry.middlePoint()));
		} finally {
			connectorGraphics.dispose();
		}
	}

	void drawCardinalityLabel(
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

	void drawClasses(final Graphics2D g2) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (!this.isVisible(classModel) || !this.shouldExportClass(classModel)) {
				continue;
			}

			final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()).getSize().setWidth(bounds.getWidth());
			this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()).getSize().setHeight(bounds.getHeight());

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
					g2.setColor(DiagramCanvas.SELECTION_FILL_COLOR);
					g2.fill(fieldBounds);
				}

				g2.setColor(classModel.getStyle().getBorderColor());
				g2.draw(new Line2D.Double(bounds.getX(), rowY, bounds.getMaxX(), rowY));

				g2.setColor(fieldModel.getStyle().getTextColor());
				g2.drawString(this.resolveFieldName(fieldModel), (float) bounds.getX() + DiagramCanvas.PADDING, (float) rowY + 15);

				if (this.isFieldSelected(classModel.getId(), fieldModel.getId())) {
					g2.setColor(DiagramCanvas.SELECTION_COLOR);
					g2.setStroke(DiagramCanvas.FIELD_SELECTION_STROKE);
					g2.draw(fieldBounds);
					g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
				}

				rowY += DiagramCanvas.ROW_HEIGHT;
			}

			if (this.isClassSelected(classModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.setStroke(DiagramCanvas.SELECTION_STROKE);
				g2.draw(bounds);
				g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
			} else {
				g2.setColor(classModel.getStyle().getBorderColor());
				g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
				g2.draw(bounds);
				g2.draw(new Line2D.Double(bounds.getX(),
						bounds.getY() + DiagramCanvas.HEADER_HEIGHT,
						bounds.getMaxX(),
						bounds.getY() + DiagramCanvas.HEADER_HEIGHT));
			}
		}
	}

	void drawCommentConnector(final Graphics2D g2, final CommentModel commentModel, final Rectangle2D bounds) {
		if (commentModel.getKind() != CommentKind.BOUND || commentModel.getBinding() == null) {
			return;
		}

		final Point2D anchor = this.findBoundTargetAnchor(g2, commentModel);
		if (anchor == null) {
			return;
		}

		g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR : DiagramCanvas.COMMENT_CONNECTOR_COLOR);
		g2.setStroke(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.COMMENT_CONNECTOR_SELECTION_STROKE
				: DiagramCanvas.DEFAULT_STROKE);
		g2.draw(new Line2D.Double(anchor.getX(), anchor.getY(), bounds.getCenterX(), bounds.getCenterY()));
		g2.setStroke(DiagramCanvas.DEFAULT_STROKE);
	}

	void drawComments(final Graphics2D g2) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String commentText = this.resolveCommentText(commentModel);
			if (commentText == null || commentText.isBlank() || !this.isCommentVisible(commentModel)
					|| !this.shouldExportComment(commentModel)) {
				continue;
			}

			final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
			final Rectangle2D bounds = this.computeCommentBounds(g2, commentText, layout);
			this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()).getSize().setWidth(bounds.getWidth());
			this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()).getSize().setHeight(bounds.getHeight());

			this.drawCommentConnector(g2, commentModel, bounds);

			g2.setColor(commentModel.getBackgroundColor());
			g2.fill(bounds);

			g2.setColor(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_COLOR : commentModel.getBorderColor());
			g2.setStroke(this.isCommentSelected(commentModel.getId()) ? DiagramCanvas.SELECTION_STROKE : DiagramCanvas.DEFAULT_STROKE);
			g2.draw(bounds);
			g2.setStroke(DiagramCanvas.DEFAULT_STROKE);

			g2.setFont(DiagramCanvas.BODY_FONT);
			g2.setColor(commentModel.getTextColor());
			this.drawMultilineText(g2, commentText, bounds, DiagramCanvas.PADDING);

			if (this.isCommentSelected(commentModel.getId())) {
				g2.setColor(DiagramCanvas.SELECTION_COLOR);
				g2.fill(new Rectangle2D.Double(bounds.getMaxX() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						bounds.getMaxY() - DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE,
						DiagramCanvas.COMMENT_RESIZE_HANDLE_SIZE));
			}
		}
	}

	void drawExportGrid(final Graphics2D g2, final Dimension size) {
		g2.setColor(DiagramCanvas.GRID_COLOR);
		for (int x = 0; x < size.width; x += 40) {
			g2.drawLine(x, 0, x, size.height);
		}
		for (int y = 0; y < size.height; y += 40) {
			g2.drawLine(0, y, size.width, y);
		}
	}

	void drawGrid(final Graphics2D g2) {
		g2.setColor(DiagramCanvas.GRID_COLOR);
		for (int x = 0; x < this.getWidth(); x += 40) {
			g2.drawLine(x, 0, x, this.getHeight());
		}
		for (int y = 0; y < this.getHeight(); y += 40) {
			g2.drawLine(0, y, this.getWidth(), y);
		}
	}

	void drawLinkPreview(final Graphics2D g2) {
		if (this.suppressInteractiveOverlays || this.linkCreationState == null) {
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
			previewGraphics.setStroke(DiagramCanvas.LINK_PREVIEW_STROKE);
			previewGraphics.draw(new Line2D.Double(fromAnchor, toAnchor));
		} finally {
			previewGraphics.dispose();
		}
	}

	void drawLinks(final Graphics2D g2) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			this.ensureConceptualAnchorCache(g2);
		}

		g2.setFont(DiagramCanvas.BODY_FONT);

		for (final LinkModel linkModel : this.getActiveLinks()) {
			if (!this.shouldExportLink(linkModel)) {
				continue;
			}

			final LinkGeometry geometry = this.resolveLinkGeometry(g2, linkModel);
			if (geometry == null) {
				continue;
			}

			g2.setColor(this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_COLOR : linkModel.getLineColor());
			g2.setStroke(this.isLinkSelected(linkModel.getId()) ? DiagramCanvas.SELECTION_STROKE : DiagramCanvas.LINK_DEFAULT_STROKE);

			for (int i = 0; i < geometry.points().size() - 1; i++) {
				g2.draw(new Line2D.Double(geometry.points().get(i), geometry.points().get(i + 1)));
			}

			if (this.panelType != PanelType.CONCEPTUAL) {
				this.drawArrowHead(g2, geometry.points().get(geometry.points().size() - 2), geometry.toPoint());
			}

			g2.setStroke(DiagramCanvas.DEFAULT_STROKE);

			if (this.panelType == PanelType.CONCEPTUAL && linkModel.getName() != null && !linkModel.getName().isBlank()) {
				this.drawAlignedLinkLabel(g2, linkModel.getName(), geometry.labelPoint(), geometry.labelAngle());
			}

			if (this.panelType == PanelType.CONCEPTUAL) {
				// TODO: fix this
				final String from = (linkModel.getCardinalityFrom() != null ? linkModel.getCardinalityFrom().getDisplayValue() : "") + " "
						+ (linkModel.getLabelFrom() != null ? linkModel.getLabelFrom() : "");
				if (!from.isBlank()) {
					this.drawCardinalityLabel(g2, from, geometry.fromPoint(), geometry.points().get(1), geometry.labelAngle());
				}

				final String to = (linkModel.getCardinalityTo() != null ? linkModel.getCardinalityTo().getDisplayValue() : "") + " "
						+ (linkModel.getLabelTo() != null ? linkModel.getLabelTo() : "");
				if (!to.isBlank()) {
					this.drawCardinalityLabel(g2,
							to,
							geometry.toPoint(),
							geometry.points().get(geometry.points().size() - 2),
							geometry.labelAngle());
				}
			}

			this.drawAssociationClassConnector(g2, linkModel, geometry);
		}
	}

	void drawMultilineText(final Graphics2D g2, final String text, final Rectangle2D bounds, final int padding) {
		final FontMetrics metrics = g2.getFontMetrics();
		final List<String> wrappedLines = this.wrapText(text, metrics, (int) Math.max(40, bounds.getWidth() - padding * 2));

		float y = (float) bounds.getY() + padding + metrics.getAscent();
		for (final String line : wrappedLines) {
			g2.drawString(line, (float) bounds.getX() + padding, y);
			y += metrics.getHeight() + 2;
		}
	}

	void editClass(final String classId) {
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
		classModel.getStyle().setTextColor(result.textColor());
		classModel.getStyle().setBackgroundColor(result.backgroundColor());
		classModel.getStyle().setBorderColor(result.borderColor());

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
		this.repaint();
	}

	void editComment(final String commentId) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		final CommentEditorDialog.Result result = CommentEditorDialog.showDialog(this, this.document, commentModel, this.panelType);
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
		this.notifyDocumentChanged();
		this.repaint();
	}

	void editField(final String classId, final String fieldId) {
		final FieldModel fieldModel = this.findFieldById(classId, fieldId);
		if (fieldModel == null) {
			return;
		}

		final FieldEditorDialog.Result result = FieldEditorDialog.showDialog(this, fieldModel, this::moveSelectedFieldInList);
		if (result == null) {
			return;
		}

		fieldModel.getNames().setConceptualName(result.name());
		fieldModel.getNames().setTechnicalName(result.technicalName());
		fieldModel.setPrimaryKey(result.primaryKey());
		fieldModel.setUnique(result.unique());
		fieldModel.setNotNull(result.notNull());
		fieldModel.getStyle().setTextColor(result.textColor());
		fieldModel.getStyle().setBackgroundColor(result.backgroundColor());
		fieldModel.setType(result.type());

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
		this.repaint();
	}

	void editLink(final String linkId) {
		final LinkModel linkModel = this.findLinkById(linkId);
		if (linkModel == null) {
			return;
		}

		final LinkEditorDialog.Result result = LinkEditorDialog.showDialog(this, this.document, linkModel, this.panelType);
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		linkModel.setName(result.name());
		linkModel.setLineColor(result.lineColor());
		linkModel.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		linkModel.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));
		linkModel.setAssociationClassId(result.associationClassId());
		linkModel.setLabelFrom(result.labelFrom());
		linkModel.setLabelTo(result.labelTo());

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

	void ensureLayouts() {
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

	FieldModel ensureTechnicalSourceField(final ClassModel sourceClass, final ClassModel targetClass, final FieldModel targetField) {
		if (sourceClass == null || targetClass == null || targetField == null) {
			return null;
		}

		final String baseTechnicalName = this.buildForeignKeyFieldTechnicalName(targetClass, targetField);
		final String baseDisplayName = this.buildForeignKeyFieldName(targetClass, targetField);

		for (int suffix = 1; suffix < 1000; suffix++) {
			final String technicalName = suffix == 1 ? baseTechnicalName : baseTechnicalName + "_" + suffix;
			final String displayName = suffix == 1 ? baseDisplayName : baseDisplayName + "_" + suffix;

			FieldModel matchingField = null;
			for (final FieldModel fieldModel : sourceClass.getFields()) {
				if (fieldModel.isPrimaryKey()) {
					continue;
				}

				if (technicalName.equalsIgnoreCase(fieldModel.getNames().getTechnicalName())
						|| displayName.equalsIgnoreCase(fieldModel.getNames().getConceptualName())) {
					matchingField = fieldModel;
					break;
				}
			}

			if (matchingField != null) {
				if (!this.hasOutgoingTechnicalLink(sourceClass.getId(), matchingField.getId())) {
					return matchingField;
				}
				continue;
			}

			final FieldModel fieldModel = new FieldModel();
			fieldModel.getNames().setConceptualName(displayName);
			fieldModel.getNames().setTechnicalName(technicalName.isBlank() ? displayName : technicalName);
			fieldModel.setNotConceptual(true);
			fieldModel.setPrimaryKey(false);
			fieldModel.setUnique(false);
			fieldModel.setNotNull(false);
			fieldModel.setType(targetField.getType());
			this.applyDefaultPaletteToField(fieldModel);
			sourceClass.getFields().add(fieldModel);
			return fieldModel;
		}

		return null;
	}

	Rectangle2D.Double expandBounds(
			final Rectangle2D.Double bounds,
			final double x,
			final double y,
			final double width,
			final double height) {

		final double safeWidth = Math.max(1.0, width);
		final double safeHeight = Math.max(1.0, height);

		if (bounds == null) {
			return new Rectangle2D.Double(x, y, safeWidth, safeHeight);
		}

		final double minX = Math.min(bounds.getMinX(), x);
		final double minY = Math.min(bounds.getMinY(), y);
		final double maxX = Math.max(bounds.getMaxX(), x + safeWidth);
		final double maxY = Math.max(bounds.getMaxY(), y + safeHeight);

		bounds.setRect(minX, minY, maxX - minX, maxY - minY);
		return bounds;
	}

	Point2D findBoundTargetAnchor(final Graphics2D g2, final CommentModel commentModel) {
		if (commentModel.getBinding() == null) {
			return null;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.findClassById(commentModel.getBinding().getTargetId());
			if (classModel == null || !this.isVisible(classModel)) {
				return null;
			}

			final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		final LinkModel linkModel = this.findLinkById(commentModel.getBinding().getTargetId());
		final LinkGeometry geometry = linkModel == null ? null : this.resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.middlePoint();
	}

	void finishLinkCreation(final Point2D.Double worldPoint) {
		if (this.linkCreationState == null) {
			return;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return;
		}

		SelectedElement target = this.linkPreviewTarget;
		if (target == null) {
			final HitResult hitResult = this.findTopmostHit(worldPoint);
			target = hitResult == null ? null : this.normalizeConnectionTargetSelection(hitResult.selection());
		}

		if (!this.isValidPreviewTarget(target)) {
			return;
		}

		if (source.type() == SelectedType.COMMENT) {
			this.bindCommentToTarget(source.commentId(), target);
			return;
		}

		if (source.type() == SelectedType.CLASS && target.type() == SelectedType.LINK) {
			this.setAssociationClassForLink(source.classId(), target.linkId());
			return;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			this.createConceptualLink(source.classId(), target.classId());
			return;
		}

		final SelectedElement fromEndpoint = this.resolveTechnicalSourceEndpoint(source, target);
		final SelectedElement toEndpoint = this.resolveTechnicalTargetEndpoint(target);
		if (fromEndpoint == null || toEndpoint == null) {
			return;
		}

		this.createTechnicalLink(fromEndpoint, toEndpoint);
	}

	List<LinkModel> getActiveLinks() {
		return this.panelType == PanelType.CONCEPTUAL ? this.document.getModel().getConceptualLinks()
				: this.document.getModel().getTechnicalLinks();
	}

	SelectedElement getLinkCreationSource() {
		return this.linkCreationState == null ? null : this.linkCreationState.toSelectedElement();
	}

	int getTechnicalSideLinkCount(final Graphics2D g2, final String classId, final AnchorSide side, final String ignoredLinkId) {
		if (classId == null || side != AnchorSide.LEFT && side != AnchorSide.RIGHT) {
			return 0;
		}

		int count = 0;
		for (final LinkModel linkModel : this.getActiveLinks()) {
			if (linkModel == null || Objects.equals(linkModel.getId(), ignoredLinkId)) {
				continue;
			}
			if (classId.equals(linkModel.getFrom().getClassId())) {
				final AnchorSide endpointSide = this.resolveTechnicalEndpointSide(g2,
						linkModel.getFrom().getClassId(),
						linkModel.getFrom().getFieldId(),
						linkModel.getTo().getClassId(),
						linkModel.getTo().getFieldId(),
						linkModel.isSelfLinking());
				if (endpointSide == side) {
					count++;
				}
			}
			if (classId.equals(linkModel.getTo().getClassId())) {
				final AnchorSide endpointSide = this.resolveTechnicalEndpointSide(g2,
						linkModel.getTo().getClassId(),
						linkModel.getTo().getFieldId(),
						linkModel.getFrom().getClassId(),
						linkModel.getFrom().getFieldId(),
						linkModel.isSelfLinking());
				if (endpointSide == side) {
					count++;
				}
			}
		}
		return count;
	}

	Dimension getViewportExportSize() {
		return new Dimension(this.getWidth() <= 0 ? DiagramCanvas.DEFAULT_EXPORT_WIDTH : this.getWidth(),
				this.getHeight() <= 0 ? DiagramCanvas.DEFAULT_EXPORT_HEIGHT : this.getHeight());
	}

	List<FieldModel> getVisibleFields(final ClassModel classModel) {
		final List<FieldModel> visibleFields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (this.panelType == PanelType.CONCEPTUAL && fieldModel.isNotConceptual()) {
				continue;
			}
			visibleFields.add(fieldModel);
		}

		return visibleFields;
	}

	boolean hasAssociationClass(final LinkModel linkModel) {
		return linkModel != null && linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank();
	}

	boolean hasOutgoingTechnicalLink(final String classId, final String fieldId) {
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

	void installKeyBindings() {
		DiagramCanvasActionRegistrar.installDefault(this,
				new DiagramCanvasActionRegistrar.DiagramCanvasActions(this::renameSelection,
						this::moveFieldSelection,
						this::moveSelectedFieldInList,
						this::addTable,
						this::addField,
						this::addComment,
						this::deleteSelection,
						this::duplicateSelection,
						this::clearSelection,
						this::addLink,
						this::selectAll,
						this::editSelected,
						this::copySelection,
						this::cutSelection,
						this::pasteSelection,
						documentEventListener::undo,
						documentEventListener::redo));
	}

	boolean isCommentVisible(final CommentModel commentModel) {
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

	boolean isLinkConnectedTo(final LinkModel linkModel, final String classId) {
		if (linkModel == null || classId == null) {
			return false;
		}
		return linkModel.getFrom() != null && Objects.equals(linkModel.getFrom().getClassId(), classId)
				|| linkModel.getTo() != null && Objects.equals(linkModel.getTo().getClassId(), classId);
	}

	boolean isSelfLink(final LinkModel linkModel) {
		return linkModel.getFrom() != null && linkModel.getTo() != null
				&& Objects.equals(linkModel.getFrom().getClassId(), linkModel.getTo().getClassId());
	}

	boolean isValidPreviewTarget(final SelectedElement target) {
		if (target == null || this.linkCreationState == null) {
			return false;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return false;
		}

		if (source.type() == SelectedType.COMMENT) {
			return target.type() == SelectedType.CLASS || target.type() == SelectedType.LINK;
		}

		if (source.type() == SelectedType.CLASS && target.type() == SelectedType.LINK) {
			return this.findLinkById(target.linkId()) != null
					&& !this.isLinkConnectedTo(this.findLinkById(target.linkId()), source.classId());
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return target.type() == SelectedType.CLASS;
		}

		final SelectedElement technicalTarget = this.resolveTechnicalTargetEndpoint(target);
		if (technicalTarget == null) {
			return false;
		}

		if (source.type() == SelectedType.FIELD) {
			if (Objects.equals(technicalTarget.classId(), source.classId())
					&& Objects.equals(technicalTarget.fieldId(), source.fieldId())) {
				return false;
			}

			return !this.hasOutgoingTechnicalLink(source.classId(), source.fieldId());
		}

		return source.type() == SelectedType.CLASS;
	}

	boolean isVisible(final ClassModel classModel) {
		return switch (this.panelType) {
		case CONCEPTUAL -> classModel.getVisibility().isConceptual();
		case LOGICAL -> classModel.getVisibility().isLogical();
		case PHYSICAL -> classModel.getVisibility().isPhysical();
		};
	}

	String mapId(final Map<String, String> idMap, final String oldId) {
		if (oldId == null) {
			return null;
		}
		return idMap.getOrDefault(oldId, oldId);
	}

	Point2D.Double mouseWorldOrViewportCenter() {
		final Point mousePoint = this.getMousePosition();

		if (mousePoint == null) {
			return this.viewportCenterWorld();
		}

		return this.screenToWorld(mousePoint);
	}

	SelectedElement normalizeConnectionSourceSelection(final SelectedElement selection) {
		if (selection == null) {
			return null;
		}

		return switch (selection.type()) {
		case COMMENT -> SelectedElement.forComment(selection.commentId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		case FIELD -> this.panelType == PanelType.CONCEPTUAL ? SelectedElement.forClass(selection.classId()) : selection;
		default -> null;
		};
	}

	SelectedElement normalizeConnectionTargetSelection(final SelectedElement selection) {
		if (selection == null || this.linkCreationState == null) {
			return null;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return switch (selection.type()) {
			case CLASS, FIELD -> SelectedElement.forClass(selection.classId());
			case LINK -> SelectedElement.forLink(selection.linkId());
			default -> null;
			};
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return switch (selection.type()) {
			case CLASS, FIELD -> SelectedElement.forClass(selection.classId());
			case LINK -> source.type() == SelectedType.CLASS ? SelectedElement.forLink(selection.linkId()) : null;
			default -> null;
			};
		}

		return switch (selection.type()) {
		case FIELD -> SelectedElement.forField(selection.classId(), selection.fieldId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		case LINK -> source.type() == SelectedType.CLASS ? SelectedElement.forLink(selection.linkId()) : null;
		default -> null;
		};
	}

	void notifyDocumentChanged() {
		this.invalidateConceptualAnchorCache();
		if (this.documentEventListener != null) {
			this.documentEventListener.onDocumentChanged();
		}
	}

	void notifySelectionChanged() {
		if (this.documentEventListener != null) {
			this.documentEventListener.onSelectionChanged(this.getSelectionInfo());
		}
	}

	void openEditDialogForSelection() {
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

	void paintCanvasComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		this.invalidateConceptualAnchorCache();
		this.ensureLayouts();

		final Graphics2D g2 = (Graphics2D) graphics.create();
		this.configureGraphics(g2);

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

	AnchorPair resolveConceptualAnchorPair(final Graphics2D g2, final LinkModel targetLink) {
		this.ensureConceptualAnchorCache(g2);
		return this.conceptualAnchorCache.get(targetLink.getId());
	}

	Point2D resolveConceptualPreviewAnchor(final Graphics2D g2, final String classId, final Point2D reference) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
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

	Point2D resolveOppositeReferencePoint(final Graphics2D g2, final String classId, final String fieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
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

	Point2D resolvePreviewSourceAnchorReference(final Graphics2D g2) {
		if (this.linkCreationState == null) {
			return this.linkPreviewMousePoint;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return this.linkPreviewMousePoint;
		}

		if (source.type() == SelectedType.COMMENT) {
			return this.resolveCommentCenterAnchor(g2, source.commentId());
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final ClassModel classModel = this.findClassById(source.classId());
			if (classModel == null || !this.isVisible(classModel)) {
				return this.linkPreviewMousePoint;
			}

			final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		return this.resolveOppositeReferencePoint(g2, source.classId(), source.fieldId());
	}

	NodeLayout resolveRenderLayout(final NodeLayout layout) {
		if (layout == null || !this.isDragRenderingActive()) {
			return layout;
		}

		for (final DraggedLayout dragged : this.draggedSelection.layouts()) {
			if (dragged.layout() == layout) {
				final double zoom = this.getPanelState().getZoom();
				final double dx = this.currentDragOffset.getX() / zoom;
				final double dy = this.currentDragOffset.getY() / zoom;

				final NodeLayout copy = new NodeLayout();
				copy.setObjectType(layout.getObjectType());
				copy.setObjectId(layout.getObjectId());
				copy.setPosition(new Point2D.Double(dragged.startX() + dx, dragged.startY() + dy));
				copy.setSize(new Size2D(layout.getSize().getWidth(), layout.getSize().getHeight()));
				return copy;
			}
		}

		return layout;
	}

	AnchorSide resolveTechnicalEndpointSide(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId,
			final boolean selfLink) {
		if (selfLink) {
			return AnchorSide.LEFT;
		}

		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return AnchorSide.LEFT;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);
		final Point2D oppositeReference = this.resolveOppositeReferencePoint(g2, oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return AnchorSide.LEFT;
		}

		double centerX = classBounds.getCenterX();
		if (fieldId != null) {
			final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
			for (int i = 0; i < visibleFields.size(); i++) {
				if (visibleFields.get(i).getId().equals(fieldId)) {
					final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
							classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
							classBounds.getWidth(),
							DiagramCanvas.ROW_HEIGHT);
					centerX = fieldBounds.getCenterX();
					break;
				}
			}
		}

		return oppositeReference.getX() <= centerX ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	Point2D resolveTechnicalFieldAnchor(final Graphics2D g2, final String classId, final String fieldId, final Point2D oppositeReference) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
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

	Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
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

	Point2D resolveTechnicalSelfLinkAnchor(final Graphics2D g2, final String classId, final String fieldId, final AnchorSide side) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.computeClassBounds(g2, classModel, layout);
		final double x = side == AnchorSide.LEFT ? classBounds.getX() : classBounds.getMaxX();

		if (fieldId == null) {
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		return new Point2D.Double(x, classBounds.getCenterY());
	}

	SelectedElement resolveTechnicalSourceEndpoint(final SelectedElement source, final SelectedElement target) {
		if (source == null || target == null) {
			return null;
		}

		if (source.type() == SelectedType.FIELD) {
			return source;
		}

		if (source.type() != SelectedType.CLASS) {
			return null;
		}

		final SelectedElement targetEndpoint = this.resolveTechnicalTargetEndpoint(target);
		if (targetEndpoint == null) {
			return null;
		}

		final ClassModel sourceClass = this.findClassById(source.classId());
		final ClassModel targetClass = this.findClassById(targetEndpoint.classId());
		final FieldModel targetField = this.findFieldById(targetEndpoint.classId(), targetEndpoint.fieldId());
		final FieldModel sourceField = this.ensureTechnicalSourceField(sourceClass, targetClass, targetField);
		return sourceField == null ? null : SelectedElement.forField(sourceClass.getId(), sourceField.getId());
	}

	SelectedElement resolveTechnicalTargetEndpoint(final SelectedElement target) {
		if (target == null) {
			return null;
		}

		if (target.type() == SelectedType.FIELD) {
			final FieldModel fieldModel = this.findFieldById(target.classId(), target.fieldId());
			return fieldModel != null && fieldModel.isPrimaryKey() ? target : null;
		}

		if (target.type() != SelectedType.CLASS) {
			return null;
		}

		final FieldModel targetField = this.findPrimaryKeyField(target.classId());
		return targetField == null ? null : SelectedElement.forField(target.classId(), targetField.getId());
	}

	Point2D.Double screenToWorld(final Point point) {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((point.getX() - state.getPanX()) / state.getZoom(), (point.getY() - state.getPanY()) / state.getZoom());
	}

	void setAssociationClassForLink(final String classId, final String linkId) {
		final LinkModel linkModel = this.findLinkById(linkId);
		if (classId == null || linkModel == null || this.findClassById(classId) == null || this.isLinkConnectedTo(linkModel, classId)) {
			return;
		}
		final LinkModel alreadyExistingLinkModel = this.findLinkByAssociationClassId(classId);
		if (alreadyExistingLinkModel != null && alreadyExistingLinkModel.getAssociationClassId().equals(classId)) {
			alreadyExistingLinkModel.setAssociationClassId(null);
		}

		linkModel.setAssociationClassId(classId);
		this.select(SelectedElement.forLink(linkId));
		this.notifyDocumentChanged();
	}

	boolean shouldExportClass(final ClassModel classModel) {
		if (this.exportSelectionFilter == null) {
			return true;
		}
		if (classModel == null) {
			return false;
		}

		for (final SelectedElement element : this.exportSelectionFilter) {
			if (element.type() == SelectedType.CLASS && Objects.equals(element.classId(), classModel.getId())
					|| element.type() == SelectedType.FIELD && Objects.equals(element.classId(), classModel.getId())) {
				return true;
			}
		}

		return false;
	}

	boolean shouldExportComment(final CommentModel commentModel) {
		return this.exportSelectionFilter == null
				|| commentModel != null && this.exportSelectionFilter.contains(SelectedElement.forComment(commentModel.getId()));
	}

	boolean shouldExportLink(final LinkModel linkModel) {
		return this.exportSelectionFilter == null
				|| linkModel != null && this.exportSelectionFilter.contains(SelectedElement.forLink(linkModel.getId()));
	}

	Point2D.Double viewportCenterWorld() {
		final PanelState state = this.getPanelState();
		return new Point2D.Double((this.getWidth() / 2.0 - state.getPanX()) / state.getZoom(),
				(this.getHeight() / 2.0 - state.getPanY()) / state.getZoom());
	}

	List<String> wrapText(final String text, final FontMetrics metrics, final int maxWidth) {
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

	@Override
	public void paintComponent(Graphics g) {
		paintCanvasComponent(g);
	}

}
