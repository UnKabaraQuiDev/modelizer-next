package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.InputEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import lu.kbra.modelizer_next.ui.ThemeMode;

final class MainFrameMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	MainFrameMenuBar(final MainFrame frame) {
		this.add(new FileMenu(frame));
		this.add(new EditMenu(frame));
		this.add(new InsertMenu(frame));
		this.add(this.createAppearanceMenu(frame));
		this.add(this.createStylesMenu(frame));
		this.add(new InfoMenu(frame));

		frame.updateUndoRedoMenuItems();
	}

	private JMenu createAppearanceMenu(final MainFrame frame) {
		final JMenu appearanceMenu = new JMenu("Appearance");
		final ButtonGroup group = new ButtonGroup();
		appearanceMenu.add(this.createThemeItem(frame, "Light", ThemeMode.LIGHT, group));
		appearanceMenu.add(this.createThemeItem(frame, "Dark", ThemeMode.DARK, group));
		appearanceMenu.add(this.createThemeItem(frame, "Follow system", ThemeMode.SYSTEM, group));
		return appearanceMenu;
	}

	private JRadioButtonMenuItem createThemeItem(final MainFrame frame,
			final String text,
			final ThemeMode mode,
			final ButtonGroup group) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		item.setSelected(frame.appConfig.getThemeMode() == mode);
		item.addActionListener(event -> frame.applyThemeAndReopen(mode));
		group.add(item);
		return item;
	}

	private JMenu createStylesMenu(final MainFrame frame) {
		final JMenu stylesMenu = new JMenu("Styles");
		frame.populateStylesMenu(stylesMenu);
		return stylesMenu;
	}

	static KeyStroke ctrl(final int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK);
	}

	static KeyStroke ctrlShift(final int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	}

}
