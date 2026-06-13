package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.layout.PanelType;

/**
 * Detailed selection information used by menus and style actions.
 *
 * @param panelType diagram panel type whose model or layout should be used
 * @param path      file system path to read or write
 */
public record SelectionInfo(PanelType panelType, String path, SelectedElement element) {
}
