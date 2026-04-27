package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

final class FileMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	FileMenu(final MainFrame frame) {
		super("File");
		this.add(this.createMenuItem("New", MainFrameMenuBar.ctrl(KeyEvent.VK_N), frame::newDocument));
		this.add(this.createMenuItem("Load", MainFrameMenuBar.ctrl(KeyEvent.VK_O), frame::loadDocument));
		this.add(this.createMenuItem("Save", MainFrameMenuBar.ctrl(KeyEvent.VK_S), frame::saveDocument));
		this.add(this.createMenuItem("Save As...", MainFrameMenuBar.ctrlShift(KeyEvent.VK_S), frame::saveDocumentAs));
		this.addSeparator();
		this.add(this.createMenuItem("Export...", MainFrameMenuBar.ctrlShift(KeyEvent.VK_E), frame::exportImage));
	}

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
