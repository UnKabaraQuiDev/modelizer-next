package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lu.kbra.modelizer_next.common.App;

/**
 * File menu builder for open, save, import, export, and exit actions.
 */
final class FileMenu extends JMenu {

	private static final long serialVersionUID = 1L;
	private JMenu recentItem;
	private Action recentFileOpenAction;

	/**
	 * Creates a file menu instance.
	 *
	 * @param frame frame that owns the created UI component
	 */
	FileMenu(final MainFrame frame) {
		super("File");
		this.add(this.createMenuItem("New", MainFrameMenuBar.ctrl(KeyEvent.VK_N), frame::newDocument));
		this.add(this.createMenuItem("Load", MainFrameMenuBar.ctrl(KeyEvent.VK_O), frame::loadDocument));
		this.add(this.recentItem = new JMenu("Recent Files"));
		this.updateRecentItem();
		App.addConfigHook(c -> this.updateRecentItem());
		this.add(this.createMenuItem("Save", MainFrameMenuBar.ctrl(KeyEvent.VK_S), frame::saveDocument));
		this.add(this.createMenuItem("Save As...", MainFrameMenuBar.ctrlShift(KeyEvent.VK_S), frame::saveDocumentAs));
		this.addSeparator();
		this.add(this.createMenuItem("Export...", MainFrameMenuBar.ctrlShift(KeyEvent.VK_E), frame::exportView));
	}

	public void updateRecentItem() {
		this.recentItem.removeAll();
		for (final Path f : App.CONFIG.getRecentFiles()) {
			final JMenuItem item = new JMenuItem(f.toString());
			item.setAction(this.recentFileOpenAction);
			this.recentItem.add(item);
		}
		this.recentItem.addSeparator();
		this.recentItem.add(new JMenuItem("Clear Recent Files"));
	}

	/**
	 * Creates a menu item.
	 *
	 * @param text      text to display or edit
	 * @param keyStroke keyboard shortcut to register
	 * @param action    action to register or execute
	 * @return the created menu item
	 */
	private JMenuItem createMenuItem(final String text, final KeyStroke keyStroke, final Runnable action) {
		final JMenuItem item = new JMenuItem(new AbstractAction(text) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
		item.setAccelerator(keyStroke);
		return item;
	}

}
