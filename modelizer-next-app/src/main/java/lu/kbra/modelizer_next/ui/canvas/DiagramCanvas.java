package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorPair;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClassSideKey;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DraggedSelection;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkAnchorPlacement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkCreationState;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ResizingComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;

public class DiagramCanvas extends JPanel
		implements DiagramModelLookup, NodeLayoutCache, SelectionController, NameResolver, PaletteController, ClipboardController,
		LinkGeometryResolver, ConceptualAnchorCache, CanvasHitTester, CanvasExportRenderer, DiagramModelEditor, DragSelectionController,
		DiagramPathBuilder, MouseInteractionController, ElementEditor, ElementRenderer, ElementDeleter, ElementCreator, VisibilityManager,
		CaptureManager, LinkLayoutManager, ExportManager, NodeLayoutManager, DiagramCanvasCoreSupport {

	private static final long serialVersionUID = -768210073584363710L;

	public static final double PASTE_OFFSET = 30.0;

	public static final String DEBUG_DRAW_LINK_ANCHORS_PROPERTY = DiagramCanvas.class.getSimpleName() + ".debug_draw_link_anchors";
	public static boolean DEBUG_DRAW_LINK_ANCHORS = Boolean.getBoolean(DEBUG_DRAW_LINK_ANCHORS_PROPERTY);

	public static ClipboardSnapshot clipboardSnapshot;

	public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	public static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public static final int COMMENT_MIN_WIDTH = 120;
	public static final int COMMENT_RESIZE_HANDLE_SIZE = 12;
	public static final int COMMENT_MIN_HEIGHT = 40;
	public static final int COMMENT_MIN_WIDTH_VALUE = 120;

	public static final int CLASS_MIN_WIDTH = 140;
	public static final int CLASS_HEADER_HEIGHT = 28;
	public static final int CLASS_ROW_HEIGHT = 22;

	public static final int TEXT_PADDING = 8;

	public static final Color CANVAS_BACKGROUND_COLOR = new Color(0xF2F2F2);
	public static final Color GRID_COLOR = new Color(0xE4E4E4);
	public static final Color SELECTION_COLOR = new Color(0x2F7DFF);
	public static final Color SELECTION_FILL_COLOR = new Color(DiagramCanvas.SELECTION_COLOR.getRed(),
			DiagramCanvas.SELECTION_COLOR.getGreen(),
			DiagramCanvas.SELECTION_COLOR.getBlue(),
			60);
	public static final Color COMMENT_CONNECTOR_COLOR = new Color(0x777777);
	public static final BasicStroke DEFAULT_STROKE = new BasicStroke(1.0f);

	public static final BasicStroke FIELD_SELECTION_STROKE = new BasicStroke(2.0f);
	public static final BasicStroke SELECTION_STROKE = new BasicStroke(2.5f);
	public static final BasicStroke LINK_DEFAULT_STROKE = new BasicStroke(1.2f);
	public static final BasicStroke COMMENT_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f);
	public static final BasicStroke ASSOCIATION_CONNECTOR_DEFAULT_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	public static final BasicStroke ASSOCIATION_CONNECTOR_SELECTION_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 5.0f, 5.0f },
			0.0f);
	public static final BasicStroke LINK_PREVIEW_STROKE = new BasicStroke(1.5f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f,
			new float[] { 6.0f, 6.0f },
			0.0f);
	public static final double LINK_HIT_DISTANCE = 6.0;

	public static final double CONCEPTUAL_ANCHOR_SPACING = 18.0;
	public static final double SELF_LINK_OUTSIDE_OFFSET = 40.0;
	public static final int EXPORT_MARGIN = 32;
	public static final int DEFAULT_EXPORT_WIDTH = 1200;
	public static final int DEFAULT_EXPORT_HEIGHT = 800;

	final ModelDocument document;

	final PanelType panelType;
	final DocumentChangeListener documentEventListener;
	DraggedSelection draggedSelection;

	Point lastScreenPoint;
	boolean panning;
	LinkCreationState linkCreationState;
	SelectedElement linkPreviewTarget;

	JTextField renamingField;
	SelectedElement renamingElement;

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

		super.setBackground(DiagramCanvas.CANVAS_BACKGROUND_COLOR);
		super.setOpaque(true);
		super.setFocusable(true);

		this.installKeyBindings();

		final MouseAdapter mouseAdapter = this.createMouseAdapter();
		super.addMouseListener(mouseAdapter);
		super.addMouseMotionListener(mouseAdapter);
		super.addMouseWheelListener(mouseAdapter);

		this.renamingField = this.createRenamingField();
		super.add(this.renamingField);
		super.setLayout(null);
	}

	@Override
	public DiagramCanvas getCanvas() {
		return this;
	}

	public Action getCanvasAction(final String actionKey) {
		return this.getActionMap().get(actionKey);
	}

	@Override
	public ModelDocument getDocument() {
		return this.document;
	}

	@Override
	public PanelType getPanelType() {
		return this.panelType;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		this.invalidateConceptualAnchorCache();
		this.ensureLayouts();

		final Graphics2D g2 = (Graphics2D) g.create();
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
		this.renamingField.setVisible(false);

		this.setCursor(Cursor.getDefaultCursor());
		this.notifySelectionChanged();
		this.revalidate();
		this.repaint();
	}

}
