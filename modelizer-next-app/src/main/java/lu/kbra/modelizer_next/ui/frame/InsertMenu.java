package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

final class InsertMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	InsertMenu(final MainFrame frame) {
		super("Insert");
		this.add(this.createCanvasMenuItem(frame, "New table", "addTable", MainFrameMenuBar.ctrl(KeyEvent.VK_T)));
		this.add(this.createCanvasMenuItem(frame, "New field", "addField", MainFrameMenuBar.ctrl(KeyEvent.VK_F)));
		this.add(this.createCanvasMenuItem(frame, "New comment", "addComment", MainFrameMenuBar.ctrl(KeyEvent.VK_C)));
		this.add(this.createCanvasMenuItem(frame, "New link", "addLink", MainFrameMenuBar.ctrl(KeyEvent.VK_L)));
	}

	private JMenuItem createCanvasMenuItem(final MainFrame frame,
			final String text,
			final String actionKey,
			final KeyStroke keyStroke) {
		final JMenuItem item = new JMenuItem(text);
		item.setAccelerator(keyStroke);
		item.addActionListener(event -> {
			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final javax.swing.Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey));
				canvas.requestFocusInWindow();
			}
		});
		return item;
	}

}
