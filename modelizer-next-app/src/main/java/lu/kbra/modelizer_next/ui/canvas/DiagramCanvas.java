package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JPanel;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClassSideKey;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedSelection;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkCreationState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ResizingComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;

public class DiagramCanvas extends JPanel implements DiagramModelLookup, NodeLayoutCache, SelectionController, NameResolver,
		PaletteController, ClipboardController, LinkGeometryResolver, ConceptualAnchorCache, CanvasHitTester, CanvasExportRenderer,
		DiagramModelEditor, DragSelectionController, DiagramPathBuilder, MouseInteractionController, ElementEditor, ElementRenderer,
		ElementDeleter, ElementCreator, VisibilityManager, CaptureManager, LinkLayoutManager, ExportManager, NodeLayoutManager {

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

	void configureGraphics(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
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

	boolean isLinkConnectedTo(final LinkModel linkModel, final String classId) {
		if (linkModel == null || classId == null) {
			return false;
		}
		return linkModel.getFrom() != null && Objects.equals(linkModel.getFrom().getClassId(), classId)
				|| linkModel.getTo() != null && Objects.equals(linkModel.getTo().getClassId(), classId);
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
