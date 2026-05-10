package lu.kbra.modelizer_next.ui.frame;

import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.dialogs.StylePaletteEditorDialog;

/**
 * Style and palette actions implemented by the main frame.
 */
public interface MainFrameStyleController {

	/**
	 * Creates a default style menu.
	 *
	 * @return the created default style menu
	 */
	default JMenu createDefaultStyleMenu() {
		final MainFrame frame = (MainFrame) this;
		final JMenu defaultMenu = new JMenu("Default style");
		final ButtonGroup defaultGroup = new ButtonGroup();

		for (final StylePalette palette : frame.palettes) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(palette.getName());
			item.setSelected(palette.getName().equals(App.CONFIG.getDefaultPaletteName()));
			item.addActionListener(event -> {
				App.CONFIG.setDefaultPaletteName(palette.getName());
				App.saveConfig();
				this.setDefaultPaletteToCanvases();
			});
			defaultGroup.add(item);
			defaultMenu.add(item);
		}
		return defaultMenu;
	}

	/**
	 * Creates a reload styles item.
	 *
	 * @return the created reload styles item
	 */
	default JMenuItem createReloadStylesItem() {
		final JMenuItem reloadItem = new JMenuItem("Reload styles");
		reloadItem.addActionListener(event -> this.reloadStyles());
		return reloadItem;
	}

	/**
	 * Finds the palette by name that matches the supplied input.
	 *
	 * @param paletteName name value to use
	 * @return the matching palette by name, or {@code null} when no match exists
	 */
	default StylePalette findPaletteByName(final String paletteName) {
		final MainFrame frame = (MainFrame) this;
		if (paletteName == null || paletteName.isBlank()) {
			return null;
		}

		for (final StylePalette palette : frame.palettes) {
			if (paletteName.equals(palette.getName())) {
				return palette;
			}
		}
		return null;
	}

	/**
	 * Blends the given color with white by the supplied ratio.
	 *
	 * @param color  color value to use
	 * @param amount numeric amount value
	 * @return the mix with white result
	 */
	default Color mixWithWhite(final Color color, final double amount) {
		if (color == null) {
			return Color.WHITE;
		}

		final double clampedAmount = Math.max(0.0, Math.min(1.0, amount));
		final int red = (int) Math.round(color.getRed() + (255 - color.getRed()) * clampedAmount);
		final int green = (int) Math.round(color.getGreen() + (255 - color.getGreen()) * clampedAmount);
		final int blue = (int) Math.round(color.getBlue() + (255 - color.getBlue()) * clampedAmount);
		return new Color(red, green, blue);
	}

	/**
	 * Rebuilds the style menu from the available palettes.
	 *
	 * @param stylesMenu styles menu value used by the operation
	 */
	default void populateStylesMenu(final JMenu stylesMenu) {
		final MainFrame frame = (MainFrame) this;
		stylesMenu.removeAll();

		final JMenuItem newPaletteItem = new JMenuItem("New palette...");
		newPaletteItem.addActionListener(event -> {
			final StylePalette palette = StylePaletteEditorDialog.showDialog(frame);
			if (palette == null) {
				return;
			}

			StylePaletteService.save(palette);
			this.reloadStyles();
		});

		stylesMenu.add(newPaletteItem);
		stylesMenu.addSeparator();
		stylesMenu.add(new StyleApplyMenu(frame));
		stylesMenu.add(new StyleEditMenu(frame));
		stylesMenu.add(this.createDefaultStyleMenu());
		stylesMenu.addSeparator();
		stylesMenu.add(this.createReloadStylesItem());
	}

	/**
	 * Reloads style palettes and refreshes the related UI controls.
	 */
	default void reloadStyles() {
		final MainFrame frame = (MainFrame) this;
		frame.palettes = StylePaletteService.loadAll();
		this.setDefaultPaletteToCanvases();
		frame.setJMenuBar(new MainFrameMenuBar(frame));
		frame.revalidate();
		frame.repaint();
	}

	/**
	 * Sets the default palette to canvases.
	 */
	default void setDefaultPaletteToCanvases() {
		final MainFrame frame = (MainFrame) this;
		final StylePalette palette = this.findPaletteByName(App.CONFIG.getDefaultPaletteName());
		frame.conceptualCanvas.setDefaultPalette(palette);
		frame.logicalCanvas.setDefaultPalette(palette);
		frame.physicalCanvas.setDefaultPalette(palette);
	}

}
