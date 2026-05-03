package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;

public interface DiagramCanvasExt {

	default DiagramCanvas getCanvas() {
		return (DiagramCanvas) this;
	}

	default ModelDocument getDocument() {
		return ((DiagramCanvas) this).document;
	}

	default PanelType getPanelType() {
		return ((DiagramCanvas) this).panelType;
	}

}
