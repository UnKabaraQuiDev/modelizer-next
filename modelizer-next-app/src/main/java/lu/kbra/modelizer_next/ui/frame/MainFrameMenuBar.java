package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.InputEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.ui.ThemeMode;

/**
 * Menu bar builder for the main frame.
 */
final class MainFrameMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a keyboard shortcut with the Control modifier.
	 *
	 * @param keyCode numeric key code value
	 * @return the ctrl result
	 */
	static KeyStroke ctrl(final int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, MainFrame.CTRL_MODIFIER);
	}

	/**
	 * Creates a keyboard shortcut with Control and Shift modifiers.
	 *
	 * @param keyCode numeric key code value
	 * @return the ctrl shift result
	 */
	static KeyStroke ctrlShift(final int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK);
	}

	/**
	 * Creates a main frame menu bar instance.
	 *
	 * @param frame frame that owns the created UI component
	 */
	MainFrameMenuBar(final MainFrame frame) {
		this.add(new FileMenu(frame));
		this.add(new EditMenu(frame));
		this.add(new InsertMenu(frame));
		this.add(this.createAppearanceMenu(frame));
		this.add(this.createStylesMenu(frame));
		this.add(new InfoMenu(frame));
	}

	/**
	 * Creates an appearance menu.
	 *
	 * @param frame frame that owns the created UI component
	 * @return the created appearance menu
	 */
	private JMenu createAppearanceMenu(final MainFrame frame) {
		final JMenu appearanceMenu = new JMenu("Appearance");
		final ButtonGroup group = new ButtonGroup();
		appearanceMenu.add(this.createThemeItem(frame, "Light", ThemeMode.LIGHT, group));
		appearanceMenu.add(this.createThemeItem(frame, "Dark", ThemeMode.DARK, group));
		appearanceMenu.add(this.createThemeItem(frame, "Follow system", ThemeMode.SYSTEM, group));
		return appearanceMenu;
	}

	/**
	 * Creates a styles menu.
	 *
	 * @param frame frame that owns the created UI component
	 * @return the created styles menu
	 */
	private JMenu createStylesMenu(final MainFrame frame) {
		final JMenu stylesMenu = new JMenu("Styles");
		frame.populateStylesMenu(stylesMenu);
		return stylesMenu;
	}

	/**
	 * Creates a theme item.
	 *
	 * @param frame frame that owns the created UI component
	 * @param text  text to display or edit
	 * @param mode  mode value used by the operation
	 * @param group group value used by the operation
	 * @return the created theme item
	 */
	private JRadioButtonMenuItem createThemeItem(final MainFrame frame, final String text, final ThemeMode mode, final ButtonGroup group) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		item.setSelected(App.CONFIG.getThemeMode() == mode);
		item.addActionListener(event -> frame.applyThemeAndReopen(mode));
		group.add(item);
		return item;
	}

}
