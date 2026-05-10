package lu.kbra.modelizer_next.ui.frame;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

/**
 * Menu builder for applying style palettes to selected elements.
 */
final class StyleApplyMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a style apply menu instance.
	 *
	 * @param frame frame that owns the created UI component
	 */
	StyleApplyMenu(final MainFrame frame) {
		super("Apply style");
		for (final StylePalette palette : frame.palettes) {
			final JMenuItem item = new JMenuItem(palette.getName());
			item.addActionListener(event -> {
				final DiagramCanvas canvas = frame.getActiveCanvas();
				if (canvas != null) {
					canvas.applyPaletteToSelection(palette);
					App.CONFIG.setSelectedPaletteName(palette.getName());
					App.saveConfig();
				}
			});
			this.add(item);
		}
	}

}
