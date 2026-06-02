package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.InputEvent;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import lu.kbra.modelizer_next.common.App;

public class SettingsMenu extends JMenu {

	private static final long serialVersionUID = -2934928820039075083L;

	public SettingsMenu(final MainFrame frame) {
		super("Settings");
		this.add(this.createCheckboxItem("Emulate middle mouse button", v -> App.editConfig(c -> c.setEmulateMiddleClick(v))));
		this.add(this.createModifierMaskItem("Key modifier",
				mask -> App.editConfig(c -> c.setAlternativeLiveEditKey(mask)),
				App.CONFIG.hasAlternativeLiveEditKey() ? App.CONFIG.getAlternativeLiveEditKey() : 0));
	}

	private JMenu createModifierMaskItem(final String text, final Consumer<Integer> hook, final Integer originalValue) {
		final JMenu menu = new JMenu(text);

		final int[] masks = new int[] {
				0,
				InputEvent.CTRL_DOWN_MASK,
				InputEvent.ALT_DOWN_MASK,
				InputEvent.ALT_GRAPH_DOWN_MASK,
				InputEvent.SHIFT_DOWN_MASK,
				InputEvent.META_DOWN_MASK,
				MainFrame.CTRL_MODIFIER };

		final String[] labels = new String[] { "Default (Alt)", "Ctrl", "Alt", "AltGr", "Shift", "Meta", "Ctrl/Cmd" };

		final int activeIndex = IntStream.range(0, masks.length).filter(c -> masks[c] == originalValue).findFirst().orElse(0);

		final ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < masks.length; i++) {
			final int mask = masks[i];

			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(labels[i]);
			item.setSelected(activeIndex == i);

			item.addActionListener(e -> hook.accept(mask));

			group.add(item);
			menu.add(item);
		}

		return menu;
	}

	private JMenuItem createCheckboxItem(final String text, final Consumer<Boolean> hook) {
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(text);
		item.addActionListener(e -> hook.accept(item.isSelected()));
		return item;
	}

}
