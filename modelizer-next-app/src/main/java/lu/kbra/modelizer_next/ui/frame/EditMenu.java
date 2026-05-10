package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Edit menu builder for undo, redo, copy, paste, delete, and related actions.
 */
final class EditMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an edit menu instance.
	 * @param frame frame that owns the created UI component
	 */
	EditMenu(final MainFrame frame) {
		super("Edit");

		frame.undoMenuItem = new JMenuItem("Undo");
		frame.undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER));
		frame.undoMenuItem.addActionListener(event -> frame.undo());

		frame.redoMenuItem = new JMenuItem("Redo");
		frame.redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK));
		frame.redoMenuItem.addActionListener(event -> frame.redo());

		this.add(frame.undoMenuItem);
		this.add(frame.redoMenuItem);
	}

}
