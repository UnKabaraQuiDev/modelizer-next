package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;

public interface DiagramCanvasExt {

	DiagramCanvas getCanvas();

	ModelDocument getDocument();

	PanelType getPanelType();

}
