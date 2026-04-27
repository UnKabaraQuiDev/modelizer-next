package lu.kbra.modelizer_next.ui.frame;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

final class StyleApplyMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	StyleApplyMenu(final MainFrame frame) {
		super("Apply style");
		for (final StylePalette palette : frame.palettes) {
			final JMenuItem item = new JMenuItem(palette.getName());
			item.addActionListener(event -> {
				final DiagramCanvas canvas = frame.getActiveCanvas();
				if (canvas != null) {
					canvas.applyPalette(palette);
					frame.appConfig.setSelectedPaletteName(palette.getName());
					App.saveConfig(frame.appConfig);
					frame.refreshPinnedStylesPanel();
				}
			});
			this.add(item);
		}
	}

}
