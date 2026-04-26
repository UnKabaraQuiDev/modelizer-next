package lu.kbra.modelizer_next.ui.canvas;

import java.util.Objects;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;

final class DiagramCanvasModuleRegistry {

	private final ModelDocument document;
	private final PanelType panelType;
	private LayoutCache layoutCache;
	private DiagramModelLookup modelLookup;
	private SelectionController selectionController;
	private NameResolver nameResolver;
	private PaletteController paletteController;
	private ClipboardController clipboardController;
	private LinkGeometryResolver linkGeometryResolver;
	private ConceptualAnchorCache conceptualAnchorCache;
	private CanvasHitTester canvasHitTester;
	private CanvasRenderer canvasRenderer;
	private CanvasExportRenderer canvasExportRenderer;
	private DiagramModelEditor modelEditor;
	private MouseInteractionController mouseInteractionController;

	DiagramCanvasModuleRegistry(final ModelDocument document, final PanelType panelType) {
		this.document = Objects.requireNonNull(document, "document");
		this.panelType = Objects.requireNonNull(panelType, "panelType");
	}

	CanvasExportRenderer canvasExportRenderer() {
		return Objects.requireNonNull(this.canvasExportRenderer, "canvasExportRenderer");
	}

	CanvasHitTester canvasHitTester() {
		return Objects.requireNonNull(this.canvasHitTester, "canvasHitTester");
	}

	CanvasRenderer canvasRenderer() {
		return Objects.requireNonNull(this.canvasRenderer, "canvasRenderer");
	}

	ClipboardController clipboardController() {
		return Objects.requireNonNull(this.clipboardController, "clipboardController");
	}

	ConceptualAnchorCache conceptualAnchorCache() {
		return Objects.requireNonNull(this.conceptualAnchorCache, "conceptualAnchorCache");
	}

	ModelDocument document() {
		return this.document;
	}

	LayoutCache layoutCache() {
		return Objects.requireNonNull(this.layoutCache, "layoutCache");
	}

	LinkGeometryResolver linkGeometryResolver() {
		return Objects.requireNonNull(this.linkGeometryResolver, "linkGeometryResolver");
	}

	DiagramModelEditor modelEditor() {
		return Objects.requireNonNull(this.modelEditor, "modelEditor");
	}

	DiagramModelLookup modelLookup() {
		return Objects.requireNonNull(this.modelLookup, "modelLookup");
	}

	MouseInteractionController mouseInteractionController() {
		return Objects.requireNonNull(this.mouseInteractionController, "mouseInteractionController");
	}

	NameResolver nameResolver() {
		return Objects.requireNonNull(this.nameResolver, "nameResolver");
	}

	PaletteController paletteController() {
		return Objects.requireNonNull(this.paletteController, "paletteController");
	}

	PanelType panelType() {
		return this.panelType;
	}

	SelectionController selectionController() {
		return Objects.requireNonNull(this.selectionController, "selectionController");
	}

	void setCanvasExportRenderer(final CanvasExportRenderer canvasExportRenderer) {
		this.canvasExportRenderer = Objects.requireNonNull(canvasExportRenderer, "canvasExportRenderer");
	}

	void setCanvasHitTester(final CanvasHitTester canvasHitTester) {
		this.canvasHitTester = Objects.requireNonNull(canvasHitTester, "canvasHitTester");
	}

	void setCanvasRenderer(final CanvasRenderer canvasRenderer) {
		this.canvasRenderer = Objects.requireNonNull(canvasRenderer, "canvasRenderer");
	}

	void setClipboardController(final ClipboardController clipboardController) {
		this.clipboardController = Objects.requireNonNull(clipboardController, "clipboardController");
	}

	void setConceptualAnchorCache(final ConceptualAnchorCache conceptualAnchorCache) {
		this.conceptualAnchorCache = Objects.requireNonNull(conceptualAnchorCache, "conceptualAnchorCache");
	}

	void setLayoutCache(final LayoutCache layoutCache) {
		this.layoutCache = Objects.requireNonNull(layoutCache, "layoutCache");
	}

	void setLinkGeometryResolver(final LinkGeometryResolver linkGeometryResolver) {
		this.linkGeometryResolver = Objects.requireNonNull(linkGeometryResolver, "linkGeometryResolver");
	}

	void setModelEditor(final DiagramModelEditor modelEditor) {
		this.modelEditor = Objects.requireNonNull(modelEditor, "modelEditor");
	}

	void setModelLookup(final DiagramModelLookup modelLookup) {
		this.modelLookup = Objects.requireNonNull(modelLookup, "modelLookup");
	}

	void setMouseInteractionController(final MouseInteractionController mouseInteractionController) {
		this.mouseInteractionController = Objects.requireNonNull(mouseInteractionController, "mouseInteractionController");
	}

	void setNameResolver(final NameResolver nameResolver) {
		this.nameResolver = Objects.requireNonNull(nameResolver, "nameResolver");
	}

	void setPaletteController(final PaletteController paletteController) {
		this.paletteController = Objects.requireNonNull(paletteController, "paletteController");
	}

	void setSelectionController(final SelectionController selectionController) {
		this.selectionController = Objects.requireNonNull(selectionController, "selectionController");
	}

}
