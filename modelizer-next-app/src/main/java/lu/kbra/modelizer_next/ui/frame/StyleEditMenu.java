package lu.kbra.modelizer_next.ui.frame;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.dialogs.StylePaletteEditorDialog;

final class StyleEditMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	StyleEditMenu(final MainFrame frame) {
		super("Edit style");
		for (final StylePalette palette : frame.palettes) {
			final JMenuItem item = new JMenuItem(palette.getName());
			item.addActionListener(event -> this.editPalette(frame, palette));
			this.add(item);
		}
	}

	private void editPalette(final MainFrame frame, final StylePalette palette) {
		final String oldName = palette.getName();
		final StylePalette edited = StylePaletteEditorDialog.showDialog(frame, palette);
		if (edited == null) {
			return;
		}

		if (!oldName.equals(edited.getName())) {
			StylePaletteService.deleteByName(oldName);
			if (oldName.equals(frame.appConfig.getDefaultPaletteName())) {
				frame.appConfig.setDefaultPaletteName(edited.getName());
			}
			if (oldName.equals(frame.appConfig.getSelectedPaletteName())) {
				frame.appConfig.setSelectedPaletteName(edited.getName());
			}
			frame.replacePinnedPaletteName(oldName, edited.getName());
			App.saveConfig(frame.appConfig);
		}

		StylePaletteService.save(edited);
		frame.reloadStyles();
	}

}
