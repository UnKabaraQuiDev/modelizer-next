package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

final class EditMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	EditMenu(final MainFrame frame) {
		super("Edit");

		frame.undoMenuItem = new JMenuItem("Undo");
		frame.undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		frame.undoMenuItem.addActionListener(event -> frame.undo());

		frame.redoMenuItem = new JMenuItem("Redo");
		frame.redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		frame.redoMenuItem.addActionListener(event -> frame.redo());

		this.add(frame.undoMenuItem);
		this.add(frame.redoMenuItem);
	}

}
