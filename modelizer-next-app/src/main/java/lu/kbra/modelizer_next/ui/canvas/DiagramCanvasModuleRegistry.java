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

	default CanvasExportRenderer canvasExportRenderer() {
		return Objects.requireNonNull(this.canvasExportRenderer, "canvasExportRenderer");
	}

	default CanvasHitTester canvasHitTester() {
		return Objects.requireNonNull(this.canvasHitTester, "canvasHitTester");
	}

	default CanvasRenderer canvasRenderer() {
		return Objects.requireNonNull(this.canvasRenderer, "canvasRenderer");
	}

	default ClipboardController clipboardController() {
		return Objects.requireNonNull(this.clipboardController, "clipboardController");
	}

	default ConceptualAnchorCache conceptualAnchorCache() {
		return Objects.requireNonNull(this.conceptualAnchorCache, "conceptualAnchorCache");
	}

	default ModelDocument document() {
		return this.document;
	}

	default LayoutCache layoutCache() {
		return Objects.requireNonNull(this.layoutCache, "layoutCache");
	}

	default LinkGeometryResolver linkGeometryResolver() {
		return Objects.requireNonNull(this.linkGeometryResolver, "linkGeometryResolver");
	}

	default DiagramModelEditor modelEditor() {
		return Objects.requireNonNull(this.modelEditor, "modelEditor");
	}

	default DiagramModelLookup modelLookup() {
		return Objects.requireNonNull(this.modelLookup, "modelLookup");
	}

	default MouseInteractionController mouseInteractionController() {
		return Objects.requireNonNull(this.mouseInteractionController, "mouseInteractionController");
	}

	default NameResolver nameResolver() {
		return Objects.requireNonNull(this.nameResolver, "nameResolver");
	}

	default PaletteController paletteController() {
		return Objects.requireNonNull(this.paletteController, "paletteController");
	}

	default PanelType panelType() {
		return this.panelType;
	}

	default SelectionController selectionController() {
		return Objects.requireNonNull(this.selectionController, "selectionController");
	}

	default void setCanvasExportRenderer(final CanvasExportRenderer canvasExportRenderer) {
		this.canvasExportRenderer = Objects.requireNonNull(canvasExportRenderer, "canvasExportRenderer");
	}

	default void setCanvasHitTester(final CanvasHitTester canvasHitTester) {
		this.canvasHitTester = Objects.requireNonNull(canvasHitTester, "canvasHitTester");
	}

	default void setCanvasRenderer(final CanvasRenderer canvasRenderer) {
		this.canvasRenderer = Objects.requireNonNull(canvasRenderer, "canvasRenderer");
	}

	default void setClipboardController(final ClipboardController clipboardController) {
		this.clipboardController = Objects.requireNonNull(clipboardController, "clipboardController");
	}

	default void setConceptualAnchorCache(final ConceptualAnchorCache conceptualAnchorCache) {
		this.conceptualAnchorCache = Objects.requireNonNull(conceptualAnchorCache, "conceptualAnchorCache");
	}

	default void setLayoutCache(final LayoutCache layoutCache) {
		this.layoutCache = Objects.requireNonNull(layoutCache, "layoutCache");
	}

	default void setLinkGeometryResolver(final LinkGeometryResolver linkGeometryResolver) {
		this.linkGeometryResolver = Objects.requireNonNull(linkGeometryResolver, "linkGeometryResolver");
	}

	default void setModelEditor(final DiagramModelEditor modelEditor) {
		this.modelEditor = Objects.requireNonNull(modelEditor, "modelEditor");
	}

	default void setModelLookup(final DiagramModelLookup modelLookup) {
		this.modelLookup = Objects.requireNonNull(modelLookup, "modelLookup");
	}

	default void setMouseInteractionController(final MouseInteractionController mouseInteractionController) {
		this.mouseInteractionController = Objects.requireNonNull(mouseInteractionController, "mouseInteractionController");
	}

	default void setNameResolver(final NameResolver nameResolver) {
		this.nameResolver = Objects.requireNonNull(nameResolver, "nameResolver");
	}

	default void setPaletteController(final PaletteController paletteController) {
		this.paletteController = Objects.requireNonNull(paletteController, "paletteController");
	}

	default void setSelectionController(final SelectionController selectionController) {
		this.selectionController = Objects.requireNonNull(selectionController, "selectionController");
	}

}
