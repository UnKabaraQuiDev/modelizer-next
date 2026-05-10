package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

/**
 * Base extension contract that gives canvas mixins access to the owning DiagramCanvas and panel
 * type.
 */
public interface DiagramCanvasExt {

	/**
	 * Returns the canvas.
	 *
	 * @return the canvas
	 */
	default DiagramCanvas getCanvas() {
		return (DiagramCanvas) this;
	}

	/**
	 * Returns the document on the active canvas.
	 *
	 * @return the document
	 */
	default ModelDocument getDocument() {
		return ((DiagramCanvas) this).document;
	}

	/**
	 * Returns the panel type on the active canvas.
	 *
	 * @return the panel type
	 */
	default PanelType getPanelType() {
		return ((DiagramCanvas) this).panelType;
	}

	/**
	 * Returns the frame on the active canvas.
	 *
	 * @return the frame
	 */
	default MainFrame getFrame() {
		return ((DiagramCanvas) this).mainFrame;
	}

}
