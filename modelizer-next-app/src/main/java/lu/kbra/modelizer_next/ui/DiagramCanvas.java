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

import javax.swing.Action;
import javax.swing.JPanel;
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

	private static final double PASTE_OFFSET = 30.0;

	private static ClipboardSnapshot clipboardSnapshot;

	private record ClipboardSnapshot(PanelType panelType, List<CopiedClass> classes, List<CopiedField> fields, List<CopiedComment> comments,
			List<CopiedLink> links) {

		private boolean isEmpty() {
			return this.classes.isEmpty() && this.fields.isEmpty() && this.comments.isEmpty() && this.links.isEmpty();
		}
	}

	private record CopiedNodeLayout(double x, double y, double width, double height) {
	}

	private record CopiedClass(String sourceId, String conceptualName, String technicalName, String group, boolean visibleInConceptual,
			boolean visibleInLogical, boolean visibleInPhysical, Color textColor, Color backgroundColor, Color borderColor,
			List<CopiedField> fields, CopiedNodeLayout layout) {
	}

	private record CopiedField(String ownerClassId, String sourceId, String name, String technicalName, boolean notConceptual,
			String comment, boolean primaryKey, boolean unique, boolean notNull, String type, Color textColor, Color backgroundColor) {
	}

	private record CopiedComment(String sourceId, CommentKind kind, String text, Color textColor, Color backgroundColor, Color borderColor,
			boolean visibleInConceptual, boolean visibleInLogical, boolean visibleInPhysical, BoundTargetType bindingTargetType,
			String bindingTargetId, CopiedNodeLayout layout) {
	}

	private record CopiedLink(String sourceId, String name, Color lineColor, String associationClassId, String fromClassId,
			String fromFieldId, String toClassId, String toFieldId, Cardinality cardinalityFrom, Cardinality cardinalityTo,
			String labelFrom, String labelTo, CopiedLinkLayout layout) {
	}

	private record CopiedLinkLayout(List<Point2D.Double> bendPoints, Point2D.Double nameLabelPosition) {
	}

	private record DraggedLayout(NodeLayout layout, double startX, double startY) {
	}

	private record DraggedSelection(List<DraggedLayout> layouts, double offsetX, double offsetY, double anchorStartX, double anchorStartY) {
	}

	private record HitResult(NodeLayout layout, Rectangle2D bounds, SelectedElement selection) {
	}

	private record FieldHitResult(FieldModel field, Rectangle2D bounds) {
	}

	private record LinkCreationState(SelectedType sourceType, String classId, String fieldId, String commentId, String linkId) {
		private static LinkCreationState fromSelection(final SelectedElement selection) {
			if (selection == null) {
				return null;
			}

			return new LinkCreationState(selection
					.type(), selection.classId(), selection.fieldId(), selection.commentId(), selection.linkId());
		}

		private SelectedElement toSelectedElement() {
			return switch (this.sourceType) {
			case CLASS -> SelectedElement.forClass(this.classId);
			case FIELD -> SelectedElement.forField(this.classId, this.fieldId);
			case COMMENT -> SelectedElement.forComment(this.commentId);
			case LINK -> SelectedElement.forLink(this.linkId);
			default -> null;
			};
		}
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

	public enum StylePreviewType {
		NONE,
		CLASS,
		FIELD,
		COMMENT,
		LINK
	}

	private record AnchorPair(Point2D from, Point2D to) {
	}

	private record ClassSideKey(String classId, AnchorSide side) {
	}

	private record AnchorSidePair(AnchorSide fromSide, AnchorSide toSide) {
	}

	private record LinkAnchorPlacement(AnchorSide fromSide, AnchorSide toSide, int fromIndex, int fromCount, int toIndex, int toCount) {
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
	private static final Color CANVAS_BACKGROUND_COLOR = new Color(0xF2F2F2);
	private static final Color GRID_COLOR = new Color(0xE4E4E4);
	private static final Color SELECTION_COLOR = new Color(0x2F7DFF);
	private static final Color SELECTION_FILL_COLOR = new Color(DiagramCanvas.SELECTION_COLOR.getRed(),
			DiagramCanvas.SELECTION_COLOR.getGreen(),
			DiagramCanvas.SELECTION_COLOR.getBlue(),
			60);
	private static final Color COMMENT_CONNECTOR_COLOR = new Color(0x777777);
	private static final BasicStroke DEFAULT_STROKE = new BasicStroke(1.0f);

	private static final BasicStroke FIELD_SELECTION_STROKE = new BasicStroke(2.0f);
	private static final BasicStroke SELECTION_STROKE = new BasicStroke(2.5f);
	private static final BasicStroke LINK_DEFAULT_STROKE = new BasicStroke(1.2f);
	private static final BasicStroke COMMENT_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f);
	private static final BasicStroke ASSOCIATION_CONNECTOR_DEFAULT_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	private static final BasicStroke ASSOCIATION_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	private static final BasicStroke LINK_PREVIEW_STROKE = new BasicStroke(1.5f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 6.0f, 6.0f },
			0.0f);
	private static final double LINK_HIT_DISTANCE = 6.0;

	private static final double CONCEPTUAL_ANCHOR_SPACING = 18.0;
	private static final double SELF_LINK_OUTSIDE_OFFSET = 40.0;
	private final ModelDocument document;

	private final PanelType panelType;
	private final CanvasEventListener eventListener;
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

	private Point2D.Double currentDragOffset = new Point2D.Double();
	private final Map<String, AnchorPair> conceptualAnchorCache = new HashMap<>();
	private final Map<String, LinkAnchorPlacement> conceptualAnchorPlacements = new HashMap<>();
	private final Map<ClassSideKey, List<String>> conceptualSideLinkCache = new HashMap<>();
	private boolean conceptualAnchorCacheValid;
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

	public DiagramCanvas(final ModelDocument document, final PanelType panelType, final CanvasEventListener eventListener) {
		this.document = document;
		this.panelType = panelType;
		this.eventListener = eventListener;

		this.setBackground(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
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

		if (this.selectedElement != null && this.selectedElement.type() == SelectedType.COMMENT) {
			final CommentModel cm = this.findCommentById(this.selectedElement.commentId());
			final CommentBinding cb = cm.getBinding();
			if (cm.getKind() != CommentKind.STANDALONE) {
				commentModel.setKind(CommentKind.BOUND);
				commentModel.setBinding(new CommentBinding(cb.getTargetType(), cb.getTargetId()));
			}
		} else if (this.selectedElement != null && this.selectedElement.type() != SelectedType.COMMENT
				&& this.selectedElement.type() != SelectedType.NONE) {
			commentModel.setKind(CommentKind.BOUND);
			commentModel.setBinding(switch (this.selectedElement.type()) {
			case CLASS -> new CommentBinding(BoundTargetType.CLASS, this.selectedElement.classId());
			case LINK -> new CommentBinding(BoundTargetType.LINK, this.selectedElement.linkId());
			case FIELD -> new CommentBinding(BoundTargetType.CLASS, this.selectedElement.classId());
			default -> throw new IllegalStateException("Cannot bind comment to: " + this.selectedElement);
			});
		}

		this.document.getModel().getComments().add(commentModel);

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Point2D.Double center = this.mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2D(220, 80));

		this.select(SelectedElement.forComment(commentModel.getId()));
		this.notifySelectionChanged();
		this.notifyDocumentChanged();
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

		if (this.selectedElement != null && this.selectedElement.type() == SelectedType.CLASS
				|| this.selectedElement != null && this.selectedElement.type() == SelectedType.FIELD) {
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
		this.notifyDocumentChanged();
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
		createdLink.setLineColor(result.lineColor());
		createdLink.setAssociationClassId(result.associationClassId());
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
		this.applyDefaultPaletteToLink(createdLink);

		this.findOrCreateLinkLayout(createdLink.getId());
		this.select(SelectedElement.forLink(createdLink.getId()));
		this.notifySelectionChanged();
		this.notifyDocumentChanged();
		this.repaint();
	}

	private void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		this.applyDefaultPaletteToClass(classModel);

		this.document.getModel().getClasses().add(classModel);

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Point2D.Double center = this.viewportCenterWorld();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, 0));

		this.select(SelectedElement.forClass(classModel.getId()));
		this.notifySelectionChanged();
		this.notifyDocumentChanged();
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

		this.notifyDocumentChanged();
		this.repaint();
	}

	private void bindCommentToTarget(final String commentId, final SelectedElement target) {
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
		this.currentDragOffset = new Point2D.Double();
	}

	private String buildForeignKeyFieldName(final ClassModel targetClass, final FieldModel targetField) {
		final String className = this
				.blankToFallback(targetClass.getNames().getTechnicalName(), targetClass.getNames().getConceptualName(), "target");
		final String fieldName = this.blankToFallback(targetField.getNames().getTechnicalName(), targetField.getNames().getName(), "id");
		return className + "_" + fieldName;
	}

	private String buildForeignKeyFieldTechnicalName(final ClassModel targetClass, final FieldModel targetField) {
		final String rawName = this.buildForeignKeyFieldName(targetClass, targetField);
		return rawName.trim().replaceAll("[^A-Za-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "").toLowerCase();
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

		if (this.panelType != PanelType.CONCEPTUAL) {
			final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
			final AnchorSide side = this.chooseTechnicalSelfLinkSide(g2, linkModel);
			final int sideLoad = this.getTechnicalSideLinkCount(g2, classModel.getId(), side, linkModel.getId());
			final double outsideOffset = DiagramCanvas.SELF_LINK_OUTSIDE_OFFSET + sideLoad * 12.0;
			final double outsideX = side == AnchorSide.LEFT ? bounds.getX() - outsideOffset : bounds.getMaxX() + outsideOffset;

			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
			points.add(toPoint);
			return points;
		}

		final LinkAnchorPlacement placement = this.conceptualAnchorPlacements.get(linkModel.getId());
		if (placement == null) {
			points.add(toPoint);
			return points;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
		final double outsideOffset = DiagramCanvas.SELF_LINK_OUTSIDE_OFFSET + Math.max(placement.fromCount(), placement.toCount()) * 4.0;

		switch (placement.fromSide()) {
		case TOP -> {
			final double outsideY = bounds.getY() - outsideOffset;
			final double outsideX = bounds.getMaxX() + outsideOffset;
			points.add(new Point2D.Double(fromPoint.getX(), outsideY));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
		}
		case RIGHT -> {
			final double outsideX = bounds.getMaxX() + outsideOffset;
			final double outsideY = bounds.getMaxY() + outsideOffset;
			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(toPoint.getX(), outsideY));
		}
		case BOTTOM -> {
			final double outsideY = bounds.getMaxY() + outsideOffset;
			final double outsideX = bounds.getX() - outsideOffset;
			points.add(new Point2D.Double(fromPoint.getX(), outsideY));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(outsideX, toPoint.getY()));
		}
		case LEFT -> {
			final double outsideX = bounds.getX() - outsideOffset;
			final double outsideY = bounds.getY() - outsideOffset;
			points.add(new Point2D.Double(outsideX, fromPoint.getY()));
			points.add(new Point2D.Double(outsideX, outsideY));
			points.add(new Point2D.Double(toPoint.getX(), outsideY));
		}
		}

		points.add(toPoint);
		return points;
	}

	private AnchorSidePair chooseBestConceptualSidePair(
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

	private AnchorSide chooseSelfLinkFromSide(final String classId) {
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

	private AnchorSide chooseTechnicalSelfLinkSide(final Graphics2D g2, final LinkModel linkModel) {
		final String classId = linkModel.getFrom().getClassId();
		final int leftCount = this.getTechnicalSideLinkCount(g2, classId, AnchorSide.LEFT, linkModel.getId());
		final int rightCount = this.getTechnicalSideLinkCount(g2, classId, AnchorSide.RIGHT, linkModel.getId());
		return leftCount <= rightCount ? AnchorSide.LEFT : AnchorSide.RIGHT;
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

	private AnchorSide clockwise(final AnchorSide side) {
		return switch (side) {
		case TOP -> AnchorSide.RIGHT;
		case RIGHT -> AnchorSide.BOTTOM;
		case BOTTOM -> AnchorSide.LEFT;
		case LEFT -> AnchorSide.TOP;
		};
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

	private Point2D computeConceptualAnchorPoint(final Rectangle2D bounds, final AnchorSide side, final int index, final int totalCount) {
		final double offset = (index - (totalCount - 1) / 2.0) * DiagramCanvas.CONCEPTUAL_ANCHOR_SPACING;
		return switch (side) {
		case TOP -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getY());
		case BOTTOM -> new Point2D.Double(bounds.getCenterX() + offset, bounds.getMaxY());
		case LEFT -> new Point2D.Double(bounds.getX(), bounds.getCenterY() + offset);
		case RIGHT -> new Point2D.Double(bounds.getMaxX(), bounds.getCenterY() + offset);
		};
	}

	private Point2D computeConceptualSideCenter(final Rectangle2D bounds, final AnchorSide side) {
		return this.computeConceptualAnchorPoint(bounds, side, 0, 1);
	}

	private double computeConceptualSortValue(
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

	private void createConceptualLink(final String fromClassId, final String toClassId) {
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

	private void createTechnicalLink(final SelectedElement fromEndpoint, final SelectedElement toEndpoint) {
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
		this.notifyDocumentChanged();
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

	private void drawAssociationClassConnector(final Graphics2D g2, final LinkModel linkModel, final LinkGeometry geometry) {
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

	private void drawClasses(final Graphics2D g2) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (!this.isVisible(classModel)) {
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

	private void drawCommentConnector(final Graphics2D g2, final CommentModel commentModel, final Rectangle2D bounds) {
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

	private void drawComments(final Graphics2D g2) {
		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String commentText = this.resolveCommentText(commentModel);
			if (commentText == null || commentText.isBlank() || !this.isCommentVisible(commentModel)) {
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
			previewGraphics.setStroke(DiagramCanvas.LINK_PREVIEW_STROKE);
			previewGraphics.draw(new Line2D.Double(fromAnchor, toAnchor));
		} finally {
			previewGraphics.dispose();
		}
	}

	private void drawLinks(final Graphics2D g2) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			this.ensureConceptualAnchorCache(g2);
		}

		g2.setFont(DiagramCanvas.BODY_FONT);

		for (final LinkModel linkModel : this.getActiveLinks()) {
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

		final Rectangle2D.Double selectionBounds = this.computeSelectionBounds(snapshot);
		final Point2D.Double duplicateTarget = this.mouseWorldOrViewportCenter();

		final double deltaX = selectionBounds == null ? DiagramCanvas.PASTE_OFFSET : duplicateTarget.getX() - selectionBounds.getCenterX();
		final double deltaY = selectionBounds == null ? DiagramCanvas.PASTE_OFFSET : duplicateTarget.getY() - selectionBounds.getCenterY();

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
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
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
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
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
				copyLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + deltaX, bendPoint.getY() + deltaY));
			}
			if (sourceLayout.getNameLabelPosition() != null) {
				copyLayout.setNameLabelPosition(new Point2D.Double(sourceLayout.getNameLabelPosition().getX() + deltaX,
						sourceLayout.getNameLabelPosition().getY() + deltaY));
			}

			newSelection.add(SelectedElement.forLink(copy.getId()));
		}

		this.selectedElements.clear();
		this.selectedElements.addAll(newSelection);
		this.selectedElement = this.selectedElements.isEmpty() ? null : this.selectedElements.getLast();

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
		this.repaint();
	}

	private Point2D.Double mouseWorldOrViewportCenter() {
		final Point mousePoint = this.getMousePosition();

		if (mousePoint == null) {
			return this.viewportCenterWorld();
		}

		return this.screenToWorld(mousePoint);
	}

	private Rectangle2D.Double expandBounds(
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

	private Rectangle2D.Double computeClipboardBounds(final ClipboardSnapshot clipboard) {
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

	private Rectangle2D.Double computeSelectionBounds(final List<SelectedElement> selection) {
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
		classModel.getStyle().setTextColor(result.textColor());
		classModel.getStyle().setBackgroundColor(result.backgroundColor());
		classModel.getStyle().setBorderColor(result.borderColor());

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
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
		this.notifyDocumentChanged();
		this.repaint();
	}

	private void editField(final String classId, final String fieldId) {
		final FieldModel fieldModel = this.findFieldById(classId, fieldId);
		if (fieldModel == null) {
			return;
		}

		final FieldEditorDialog.Result result = FieldEditorDialog.showDialog(this, fieldModel, this::moveSelectedFieldInList);
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
		fieldModel.setType(result.type());

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
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

	@SuppressWarnings("incomplete-switch")
	private void editSelected() {
		if (this.selectedElement == null || this.selectedElement.type() == SelectedType.NONE) {
			return;
		}
		switch (this.selectedElement.type()) {
		case CLASS -> this.editClass(this.selectedElement.classId());
		case FIELD -> this.editField(this.selectedElement.classId(), this.selectedElement.fieldId());
		case COMMENT -> this.editComment(this.selectedElement.commentId());
		case LINK -> this.editLink(this.selectedElement.linkId());
		}
	}

	private void ensureConceptualAnchorCache(final Graphics2D g2) {
		if (this.panelType != PanelType.CONCEPTUAL || this.conceptualAnchorCacheValid) {
			return;
		}

		this.rebuildConceptualAnchorCache(g2);
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

	private FieldModel ensureTechnicalSourceField(
			final ClassModel sourceClass,
			final ClassModel targetClass,
			final FieldModel targetField) {
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
						|| displayName.equalsIgnoreCase(fieldModel.getNames().getName())) {
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
			fieldModel.getNames().setName(displayName);
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

	private Point2D findBoundTargetAnchor(final Graphics2D g2, final CommentModel commentModel) {
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

	private ClassModel findClassById(final String id) {
		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (classModel.getId().equals(id)) {
				return classModel;
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

	private FieldModel findPrimaryKeyField(final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null) {
			return null;
		}

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.isPrimaryKey()) {
				return fieldModel;
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

				final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
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

	public Object findType(final SelectedElement se) {
		return switch (se.type()) {
		case CLASS -> this.findClassById(se.classId());
		case COMMENT -> this.findCommentById(se.commentId());
		case FIELD -> this.findFieldById(se.classId(), se.fieldId());
		case LINK -> this.findLinkById(se.linkId());
		default -> null;
		};
	}

	private void finishLinkCreation(final Point2D.Double worldPoint) {
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

	private List<LinkModel> getActiveLinks() {
		return this.panelType == PanelType.CONCEPTUAL ? this.document.getModel().getConceptualLinks()
				: this.document.getModel().getTechnicalLinks();
	}

	public Action getCanvasAction(final String actionKey) {
		return this.getActionMap().get(actionKey);
	}

	private int getConceptualSideLinkCount(final String classId, final AnchorSide side) {
		final List<String> links = this.conceptualSideLinkCache.get(new ClassSideKey(classId, side));
		return links == null ? 0 : links.size();
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

		return commentModel.getText();
	}

	private String getEditableFieldName(final FieldModel fieldModel) {
		return this.panelType == PanelType.CONCEPTUAL ? fieldModel.getNames().getName() : fieldModel.getNames().getTechnicalName();
	}

	private SelectedElement getLinkCreationSource() {
		return this.linkCreationState == null ? null : this.linkCreationState.toSelectedElement();
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

	public StylePreviewType getStylePreviewType() {
		if (this.selectedElement == null) {
			return StylePreviewType.NONE;
		}

		return switch (this.selectedElement.type()) {
		case CLASS -> StylePreviewType.CLASS;
		case FIELD -> StylePreviewType.FIELD;
		case COMMENT -> StylePreviewType.COMMENT;
		case LINK -> StylePreviewType.LINK;
		default -> StylePreviewType.NONE;
		};
	}

	private int getTechnicalSideLinkCount(final Graphics2D g2, final String classId, final AnchorSide side, final String ignoredLinkId) {
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
			this.linkPreviewTarget = hitResult == null ? null : this.normalizeConnectionTargetSelection(hitResult.selection());

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

			final SelectedElement source = this.normalizeConnectionSourceSelection(hitResult.selection());
			if (source == null) {
				return;
			}

			if (!this.selectedElements.contains(source)) {
				this.select(source);
			} else {
				this.selectedElement = source;
				this.notifySelectionChanged();
			}

			this.linkCreationState = LinkCreationState.fromSelection(source);
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
		final boolean clickedAlreadySelected = this.isElementSelected(clickedElement);

		if (!this.pendingModifierSelection) {
			if (clickedAlreadySelected) {
				this.selectedElement = clickedElement;
				this.document.getModel().getClasses().sort(this.comparator);
				this.notifySelectionChanged();
				this.repaint();
			} else {
				this.select(clickedElement);
			}
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
		boolean documentChanged = false;

		if (SwingUtilities.isRightMouseButton(event) && this.linkCreationState != null) {
			this.finishLinkCreation(this.screenToWorld(event.getPoint()));
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.draggedSelection != null && this.dragOccurred) {
			final double zoom = this.getPanelState().getZoom();
			final double deltaX = this.currentDragOffset.getX() / zoom;
			final double deltaY = this.currentDragOffset.getY() / zoom;

			if (Math.abs(deltaX) > 0.0001 || Math.abs(deltaY) > 0.0001) {
				for (final DraggedLayout draggedLayout : this.draggedSelection.layouts()) {
					draggedLayout.layout().getPosition().setLocation(draggedLayout.startX() + deltaX, draggedLayout.startY() + deltaY);
				}
				documentChanged = true;
			}
		}

		if (SwingUtilities.isLeftMouseButton(event) && this.resizingComment != null) {
			final double currentWidth = this.resizingComment.layout().getSize().getWidth();
			final double currentHeight = this.resizingComment.layout().getSize().getHeight();

			if (Math.abs(currentWidth - this.resizingComment.initialWidth()) > 0.0001
					|| Math.abs(currentHeight - this.resizingComment.initialHeight()) > 0.0001) {
				documentChanged = true;
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

		this.currentDragOffset = new Point2D.Double();

		if (documentChanged) {
			this.notifyDocumentChanged();
		}

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

	private boolean hasAssociationClass(final LinkModel linkModel) {
		return linkModel != null && linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank();
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
						this::pasteSelection));
	}

	private void cutSelection() {
		copySelection();
		deleteSelection();
	}

	private void copySelection() {
		if (this.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.selectedElements);

		final Set<String> selectedClassIds = new LinkedHashSet<>();
		final Set<String> selectedFieldIds = new LinkedHashSet<>();
		final Set<String> selectedCommentIds = new LinkedHashSet<>();
		final Set<String> selectedLinkIds = new LinkedHashSet<>();

		for (final SelectedElement element : snapshot) {
			if (element.type() == SelectedType.CLASS) {
				selectedClassIds.add(element.classId());
			}
		}

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
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

		final List<CopiedClass> copiedClasses = new ArrayList<>();
		final List<CopiedField> copiedFields = new ArrayList<>();
		final List<CopiedComment> copiedComments = new ArrayList<>();
		final List<CopiedLink> copiedLinks = new ArrayList<>();

		final Set<String> copiedFieldIds = new HashSet<>();

		for (final String classId : selectedClassIds) {
			final ClassModel classModel = this.findClassById(classId);
			if (classModel == null) {
				continue;
			}

			for (final FieldModel fieldModel : classModel.getFields()) {
				copiedFieldIds.add(fieldModel.getId());
			}

			copiedClasses.add(this.captureClass(classModel));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.findOwnerClassOfField(fieldId);
			if (owner == null) {
				continue;
			}

			final FieldModel fieldModel = this.findFieldById(owner.getId(), fieldId);
			if (fieldModel == null) {
				continue;
			}

			copiedFieldIds.add(fieldModel.getId());
			copiedFields.add(this.captureField(owner.getId(), fieldModel));
		}

		final Set<String> linksToCopy = new LinkedHashSet<>(selectedLinkIds);

		for (final LinkModel linkModel : this.getActiveLinks()) {
			if (linkModel == null || linkModel.getFrom() == null || linkModel.getTo() == null) {
				continue;
			}

			final boolean touchesCopiedClass = selectedClassIds.contains(linkModel.getFrom().getClassId())
					|| selectedClassIds.contains(linkModel.getTo().getClassId())
					|| selectedClassIds.contains(linkModel.getAssociationClassId());

			final boolean touchesCopiedField = copiedFieldIds.contains(linkModel.getFrom().getFieldId())
					|| copiedFieldIds.contains(linkModel.getTo().getFieldId());

			if (touchesCopiedClass || touchesCopiedField) {
				linksToCopy.add(linkModel.getId());
			}
		}

		for (final String linkId : linksToCopy) {
			final LinkModel linkModel = this.findLinkById(linkId);
			if (linkModel != null) {
				copiedLinks.add(this.captureLink(linkModel));
			}
		}

		for (final String commentId : selectedCommentIds) {
			final CommentModel commentModel = this.findCommentById(commentId);
			if (commentModel != null) {
				copiedComments.add(this.captureComment(commentModel));
			}
		}

		DiagramCanvas.clipboardSnapshot = new ClipboardSnapshot(this.panelType,
				List.copyOf(copiedClasses),
				List.copyOf(copiedFields),
				List.copyOf(copiedComments),
				List.copyOf(copiedLinks));
	}

	private void pasteSelection() {
		final ClipboardSnapshot clipboard = DiagramCanvas.clipboardSnapshot;

		if (clipboard == null || clipboard.isEmpty()) {
			return;
		}

		final Rectangle2D.Double clipboardBounds = this.computeClipboardBounds(clipboard);
		final Point2D.Double pasteTarget = this.mouseWorldOrViewportCenter();

		final double deltaX = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getX() - clipboardBounds.getCenterX();

		final double deltaY = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getY() - clipboardBounds.getCenterY();

		final Map<String, String> pastedClassIds = new HashMap<>();
		final Map<String, String> pastedFieldIds = new HashMap<>();
		final Map<String, String> pastedLinkIds = new HashMap<>();

		final LinkedHashSet<SelectedElement> newSelection = new LinkedHashSet<>();

		for (final CopiedClass copiedClass : clipboard.classes()) {
			final ClassModel classCopy = new ClassModel();

			classCopy.getNames().setConceptualName(this.appendSuffix(copiedClass.conceptualName(), " Copy"));
			classCopy.getNames().setTechnicalName(this.appendSuffix(copiedClass.technicalName(), "_COPY"));
			classCopy.setGroup(copiedClass.group());

			classCopy.getVisibility().setConceptual(copiedClass.visibleInConceptual());
			classCopy.getVisibility().setLogical(copiedClass.visibleInLogical());
			classCopy.getVisibility().setPhysical(copiedClass.visibleInPhysical());

			classCopy.getStyle().setTextColor(copiedClass.textColor());
			classCopy.getStyle().setBackgroundColor(copiedClass.backgroundColor());
			classCopy.getStyle().setBorderColor(copiedClass.borderColor());

			for (final CopiedField copiedField : copiedClass.fields()) {
				final FieldModel fieldCopy = this.createFieldFromClipboard(copiedField, false);
				classCopy.getFields().add(fieldCopy);
				pastedFieldIds.put(copiedField.sourceId(), fieldCopy.getId());
			}

			this.document.getModel().getClasses().add(classCopy);
			pastedClassIds.put(copiedClass.sourceId(), classCopy.getId());

			this.applyNodeLayout(LayoutObjectType.CLASS, classCopy.getId(), copiedClass.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forClass(classCopy.getId()));
		}

		for (final CopiedField copiedField : clipboard.fields()) {
			final String ownerClassId = this.mapId(pastedClassIds, copiedField.ownerClassId());
			final ClassModel owner = this.findClassById(ownerClassId);

			if (owner == null) {
				continue;
			}

			final FieldModel fieldCopy = this.createFieldFromClipboard(copiedField, true);

			int insertIndex = -1;
			for (int i = 0; i < owner.getFields().size(); i++) {
				if (Objects.equals(owner.getFields().get(i).getId(), copiedField.sourceId())) {
					insertIndex = i;
					break;
				}
			}

			if (insertIndex < 0) {
				owner.getFields().add(fieldCopy);
			} else {
				owner.getFields().add(insertIndex + 1, fieldCopy);
			}

			pastedFieldIds.put(copiedField.sourceId(), fieldCopy.getId());
			newSelection.add(SelectedElement.forField(owner.getId(), fieldCopy.getId()));
		}

		if (clipboard.panelType() == this.panelType) {
			for (final CopiedLink copiedLink : clipboard.links()) {
				final LinkModel linkCopy = this.createLinkFromClipboard(copiedLink, pastedClassIds, pastedFieldIds);

				if (linkCopy == null) {
					continue;
				}

				this.getActiveLinks().add(linkCopy);
				pastedLinkIds.put(copiedLink.sourceId(), linkCopy.getId());

				this.applyLinkLayout(linkCopy.getId(), copiedLink.layout(), deltaX, deltaY);

				newSelection.add(SelectedElement.forLink(linkCopy.getId()));
			}
		}

		for (final CopiedComment copiedComment : clipboard.comments()) {
			final CommentModel commentCopy = new CommentModel();

			commentCopy.setKind(copiedComment.kind());
			commentCopy.setText(copiedComment.text());
			commentCopy.setTextColor(copiedComment.textColor());
			commentCopy.setBackgroundColor(copiedComment.backgroundColor());
			commentCopy.setBorderColor(copiedComment.borderColor());

			commentCopy.setVisibleInConceptual(copiedComment.visibleInConceptual());
			commentCopy.setVisibleInLogical(copiedComment.visibleInLogical());
			commentCopy.setVisibleInPhysical(copiedComment.visibleInPhysical());

			final CommentBinding binding = this.createRemappedCommentBinding(copiedComment, pastedClassIds, pastedLinkIds);
			if (binding != null) {
				commentCopy.setBinding(binding);
			} else if (copiedComment.kind() == CommentKind.BOUND) {
				commentCopy.setKind(CommentKind.STANDALONE);
			}

			this.document.getModel().getComments().add(commentCopy);
			this.applyNodeLayout(LayoutObjectType.COMMENT, commentCopy.getId(), copiedComment.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forComment(commentCopy.getId()));
		}

		if (newSelection.isEmpty()) {
			return;
		}

		this.selectedElements.clear();
		this.selectedElements.addAll(newSelection);
		this.selectedElement = this.selectedElements.getLast();

		this.document.getModel().getClasses().sort(this.comparator);

		this.notifySelectionChanged();
		this.notifyDocumentChanged();
		this.repaint();
	}

	private void invalidateConceptualAnchorCache() {
		this.conceptualAnchorCache.clear();
		this.conceptualAnchorPlacements.clear();
		this.conceptualSideLinkCache.clear();
		this.conceptualAnchorCacheValid = false;
	}

	private String appendSuffix(final String value, final String suffix) {
		if (value == null || value.isBlank()) {
			return value;
		}
		return value + suffix;
	}

	private String mapId(final Map<String, String> idMap, final String oldId) {
		if (oldId == null) {
			return null;
		}
		return idMap.getOrDefault(oldId, oldId);
	}

	private CopiedNodeLayout captureNodeLayout(final LayoutObjectType type, final String objectId) {
		final NodeLayout layout = this.findOrCreateNodeLayout(type, objectId);

		return new CopiedNodeLayout(layout.getPosition().getX(),
				layout.getPosition().getY(),
				layout.getSize().getWidth(),
				layout.getSize().getHeight());
	}

	private CopiedClass captureClass(final ClassModel classModel) {
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

	private CopiedField captureField(final String ownerClassId, final FieldModel fieldModel) {
		return new CopiedField(ownerClassId,
				fieldModel.getId(),
				fieldModel.getNames().getName(),
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

	private CopiedComment captureComment(final CommentModel commentModel) {
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

	private CopiedLink captureLink(final LinkModel linkModel) {
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

	private CopiedLinkLayout captureLinkLayout(final String linkId) {
		final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkId);
		final List<Point2D.Double> bendPoints = new ArrayList<>();

		for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
			bendPoints.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
		}

		final Point2D.Double labelPosition = linkLayout.getNameLabelPosition() == null ? null
				: new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());

		return new CopiedLinkLayout(List.copyOf(bendPoints), labelPosition);
	}

	private FieldModel createFieldFromClipboard(final CopiedField copiedField, final boolean rename) {
		final FieldModel fieldCopy = new FieldModel();

		fieldCopy.getNames().setName(rename ? this.appendSuffix(copiedField.name(), " Copy") : copiedField.name());
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

	private LinkModel createLinkFromClipboard(
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

	private boolean linkEndpointExists(final String classId, final String fieldId) {
		if (classId == null || this.findClassById(classId) == null) {
			return false;
		}

		return fieldId == null || this.findFieldById(classId, fieldId) != null;
	}

	private CommentBinding createRemappedCommentBinding(
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

		if (copiedComment.bindingTargetType() == BoundTargetType.CLASS && this.findClassById(targetId) == null) {
			return null;
		}

		if (copiedComment.bindingTargetType() == BoundTargetType.LINK && this.findLinkById(targetId) == null) {
			return null;
		}

		return new CommentBinding(copiedComment.bindingTargetType(), targetId);
	}

	private void applyNodeLayout(
			final LayoutObjectType type,
			final String objectId,
			final CopiedNodeLayout copiedLayout,
			final double deltaX,
			final double deltaY) {

		final NodeLayout layout = this.findOrCreateNodeLayout(type, objectId);

		layout.setPosition(new Point2D.Double(copiedLayout.x() + deltaX, copiedLayout.y() + deltaY));

		layout.setSize(new Size2D(copiedLayout.width(), copiedLayout.height()));
	}

	private void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double deltaX, final double deltaY) {

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

	private void applyLinkLayout(final String linkId, final CopiedLinkLayout copiedLayout, final double offset) {

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
		return this.draggedSelection != null;
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

	private boolean isLinkSelected(final String linkId) {
		return this.selectedElements.contains(SelectedElement.forLink(linkId));
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

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return false;
		}

		if (source.type() == SelectedType.COMMENT) {
			return target.type() == SelectedType.CLASS || target.type() == SelectedType.LINK;
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			return target.type() == SelectedType.CLASS && !Objects.equals(target.classId(), source.classId());
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
		this.notifyDocumentChanged();
		this.repaint();
	}

	private SelectedElement normalizeConnectionSourceSelection(final SelectedElement selection) {
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

	private SelectedElement normalizeConnectionTargetSelection(final SelectedElement selection) {
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
			default -> null;
			};
		}

		return switch (selection.type()) {
		case FIELD -> SelectedElement.forField(selection.classId(), selection.fieldId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		default -> null;
		};
	}

	private void notifyDocumentChanged() {
		this.invalidateConceptualAnchorCache();
		if (this.eventListener != null) {
			this.eventListener.onDocumentChanged();
		}
	}

	private void notifySelectionChanged() {
		if (this.eventListener != null) {
			this.eventListener.onSelectionChanged(this.getSelectionInfo());
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

	private void rebuildConceptualAnchorCache(final Graphics2D g2) {
		this.invalidateConceptualAnchorCache();

		final Map<String, Rectangle2D> boundsByClassId = new HashMap<>();
		final List<LinkModel> visibleLinks = new ArrayList<>();
		final Map<String, AnchorSidePair> sidePairs = new HashMap<>();

		for (final LinkModel linkModel : this.getActiveLinks()) {
			final ClassModel fromClass = this.findClassById(linkModel.getFrom().getClassId());
			final ClassModel toClass = this.findClassById(linkModel.getTo().getClassId());
			if (fromClass == null || toClass == null || !this.isVisible(fromClass) || !this.isVisible(toClass)) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.computeIfAbsent(fromClass.getId(), classId -> {
				final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.computeClassBounds(g2, fromClass, layout);
			});
			final Rectangle2D toBounds = boundsByClassId.computeIfAbsent(toClass.getId(), classId -> {
				final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
				return this.computeClassBounds(g2, toClass, layout);
			});

			final AnchorSidePair sidePair;
			if (linkModel.isSelfLinking()) {
				final AnchorSide fromSide = this.chooseSelfLinkFromSide(fromClass.getId());
				sidePair = new AnchorSidePair(fromSide, this.clockwise(fromSide));
			} else {
				sidePair = this.chooseBestConceptualSidePair(fromClass.getId(), fromBounds, toClass.getId(), toBounds);
			}

			sidePairs.put(linkModel.getId(), sidePair);
			this.conceptualSideLinkCache.computeIfAbsent(new ClassSideKey(fromClass.getId(), sidePair.fromSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			this.conceptualSideLinkCache.computeIfAbsent(new ClassSideKey(toClass.getId(), sidePair.toSide()), key -> new ArrayList<>())
					.add(linkModel.getId());
			visibleLinks.add(linkModel);
		}

		final Map<ClassSideKey, Map<String, Integer>> indexByKey = new HashMap<>();
		for (final Map.Entry<ClassSideKey, List<String>> entry : this.conceptualSideLinkCache.entrySet()) {
			final ClassSideKey key = entry.getKey();
			final List<String> linkIds = entry.getValue();
			linkIds.sort(Comparator.comparingDouble(
					(final String linkId) -> this.computeConceptualSortValue(linkId, key.classId(), key.side(), boundsByClassId, sidePairs))
					.thenComparing(linkId -> linkId));

			final Map<String, Integer> indices = new HashMap<>();
			for (int i = 0; i < linkIds.size(); i++) {
				indices.put(linkIds.get(i), i);
			}
			indexByKey.put(key, indices);
		}

		for (final LinkModel linkModel : visibleLinks) {
			final AnchorSidePair sidePair = sidePairs.get(linkModel.getId());
			if (sidePair == null) {
				continue;
			}

			final Rectangle2D fromBounds = boundsByClassId.get(linkModel.getFrom().getClassId());
			final Rectangle2D toBounds = boundsByClassId.get(linkModel.getTo().getClassId());
			if (fromBounds == null || toBounds == null) {
				continue;
			}

			final ClassSideKey fromKey = new ClassSideKey(linkModel.getFrom().getClassId(), sidePair.fromSide());
			final ClassSideKey toKey = new ClassSideKey(linkModel.getTo().getClassId(), sidePair.toSide());
			final List<String> fromLinks = this.conceptualSideLinkCache.get(fromKey);
			final List<String> toLinks = this.conceptualSideLinkCache.get(toKey);
			if (fromLinks == null || toLinks == null) {
				continue;
			}

			final int fromIndex = indexByKey.get(fromKey).get(linkModel.getId());
			final int toIndex = indexByKey.get(toKey).get(linkModel.getId());
			final Point2D fromPoint = this.computeConceptualAnchorPoint(fromBounds, sidePair.fromSide(), fromIndex, fromLinks.size());
			final Point2D toPoint = this.computeConceptualAnchorPoint(toBounds, sidePair.toSide(), toIndex, toLinks.size());

			this.conceptualAnchorCache.put(linkModel.getId(), new AnchorPair(fromPoint, toPoint));
			this.conceptualAnchorPlacements.put(linkModel.getId(),
					new LinkAnchorPlacement(sidePair.fromSide(), sidePair.toSide(), fromIndex, fromLinks.size(), toIndex, toLinks.size()));
		}

		this.conceptualAnchorCacheValid = true;
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
		this.notifyDocumentChanged();
		this.repaint();
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

	private Point2D resolveClassCenterAnchor(final Graphics2D g2, final String classId) {
		final ClassModel classModel = this.findClassById(classId);
		if (classModel == null || !this.isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Rectangle2D bounds = this.computeClassBounds(g2, classModel, layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	private String resolveClassTitle(final ClassModel classModel) {
		if (this.panelType == PanelType.CONCEPTUAL) {
			return this
					.blankToFallback(classModel.getNames().getConceptualName(), classModel.getNames().getTechnicalName(), "Unnamed class");
		}
		return this.blankToFallback(classModel.getNames().getTechnicalName(), classModel.getNames().getConceptualName(), "Unnamed class");
	}

	private Point2D resolveCommentCenterAnchor(final Graphics2D g2, final String commentId) {
		final CommentModel commentModel = this.findCommentById(commentId);
		if (commentModel == null || !this.isCommentVisible(commentModel)) {
			return null;
		}

		final NodeLayout layout = this.resolveRenderLayout(this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Rectangle2D bounds = this.computeCommentBounds(g2, this.resolveCommentText(commentModel), layout);
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}

	private String resolveCommentText(final CommentModel commentModel) {
		return commentModel == null ? ""
				: commentModel.getText() == null ? ""
				: commentModel.getText();
	}

	private AnchorPair resolveConceptualAnchorPair(final Graphics2D g2, final LinkModel targetLink) {
		this.ensureConceptualAnchorCache(g2);
		return this.conceptualAnchorCache.get(targetLink.getId());
	}

	private Point2D resolveConceptualPreviewAnchor(final Graphics2D g2, final String classId, final Point2D reference) {
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

		return baseName + " [" + String.join(", ", flags) + "] - " + (fieldModel.getType() == null ? "No type" : fieldModel.getType());
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
		} else if (this.isSelfLink(linkModel)) {
			final AnchorSide selfLinkSide = this.chooseTechnicalSelfLinkSide(g2, linkModel);
			fromPoint = this
					.resolveTechnicalSelfLinkAnchor(g2, linkModel.getFrom().getClassId(), linkModel.getFrom().getFieldId(), selfLinkSide);
			toPoint = this.resolveTechnicalSelfLinkAnchor(g2, linkModel.getTo().getClassId(), linkModel.getTo().getFieldId(), selfLinkSide);
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
		if (fromPoint == null || toPoint == null) {
			return null;
		}

		final List<Point2D> points;
		if (this.isSelfLink(linkModel)) {
			points = this.buildSelfLinkPoints(g2, linkModel, fromPoint, toPoint);
		} else {
			points = new ArrayList<>();
			points.add(fromPoint);

			final LinkLayout linkLayout = this.findOrCreateLinkLayout(linkModel.getId());
			for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
				points.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
			}

			points.add(toPoint);
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

		return new LinkGeometry(fromPoint, toPoint, labelPoint, middlePoint, labelAngle, points);
	}

	private Point2D resolveLinkMiddleAnchor(final Graphics2D g2, final String linkId) {
		final LinkModel linkModel = this.findLinkById(linkId);
		final LinkGeometry geometry = linkModel == null ? null : this.resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.middlePoint();
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

	private Point2D resolvePreviewSourceAnchor(final Graphics2D g2) {
		if (this.linkCreationState == null) {
			return null;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return this.resolveCommentCenterAnchor(g2, source.commentId());
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.linkPreviewTarget != null ? this.resolvePreviewTargetAnchor(g2, this.linkPreviewTarget)
					: this.linkPreviewMousePoint;
			return this.resolveConceptualPreviewAnchor(g2, source.classId(), reference);
		}

		final Point2D reference = this.linkPreviewTarget != null ? this.resolvePreviewTargetAnchor(g2, this.linkPreviewTarget)
				: this.linkPreviewMousePoint;

		final String oppositeClassId = this.linkPreviewTarget == null ? null : this.linkPreviewTarget.classId();
		final String oppositeFieldId = this.linkPreviewTarget == null ? null : this.linkPreviewTarget.fieldId();

		if (oppositeClassId != null) {
			return this.resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), oppositeClassId, oppositeFieldId);
		}

		return this.resolveTechnicalFieldAnchor(g2, source.classId(), source.fieldId(), reference);
	}

	private Point2D resolvePreviewSourceAnchorReference(final Graphics2D g2) {
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

	private Point2D resolvePreviewTargetAnchor(final Graphics2D g2, final SelectedElement target) {
		if (target == null) {
			return null;
		}

		final SelectedElement source = this.getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return switch (target.type()) {
			case CLASS -> this.resolveClassCenterAnchor(g2, target.classId());
			case LINK -> this.resolveLinkMiddleAnchor(g2, target.linkId());
			default -> null;
			};
		}

		if (this.panelType == PanelType.CONCEPTUAL) {
			final Point2D reference = this.resolvePreviewSourceAnchorReference(g2);
			return this.resolveConceptualPreviewAnchor(g2, target.classId(), reference);
		}

		return this.resolveTechnicalFieldAnchor(g2, target.classId(), target.fieldId(), source.classId(), source.fieldId());
	}

	private NodeLayout resolveRenderLayout(final NodeLayout layout) {
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

	private AnchorSide resolveTechnicalEndpointSide(
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

	private Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final Point2D oppositeReference) {
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

	private Point2D resolveTechnicalSelfLinkAnchor(final Graphics2D g2, final String classId, final String fieldId, final AnchorSide side) {
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

	private SelectedElement resolveTechnicalSourceEndpoint(final SelectedElement source, final SelectedElement target) {
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

	private SelectedElement resolveTechnicalTargetEndpoint(final SelectedElement target) {
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

	private void selectAll() {
		this.selectedElements.clear();

		for (final ClassModel classModel : this.document.getModel().getClasses()) {
			if (this.isVisible(classModel)) {
				this.selectedElements.add(SelectedElement.forClass(classModel.getId()));
			}
		}

		for (final CommentModel commentModel : this.document.getModel().getComments()) {
			final String text = this.resolveCommentText(commentModel);
			if (this.isCommentVisible(commentModel) && text != null && !text.isBlank()) {
				this.selectedElements.add(SelectedElement.forComment(commentModel.getId()));
			}
		}

		for (final LinkModel linkModel : this.getActiveLinks()) {
			this.selectedElements.add(SelectedElement.forLink(linkModel.getId()));
		}

		this.selectedElement = this.selectedElements.isEmpty() ? null : this.selectedElements.getLast();
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

		commentModel.setText(value);
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
