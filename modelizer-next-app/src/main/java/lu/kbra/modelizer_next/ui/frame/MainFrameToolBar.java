package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.pclib.PCUtils;

final class MainFrameToolBar extends JToolBar {

	private static final long serialVersionUID = 1L;
	JButton undoButton;
	JButton redoButton;

	MainFrameToolBar(final MainFrame frame) {
		this.setFloatable(false);
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());

		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		buttons.add(undoButton = this.createToolbarButton(frame, "undo.png", "Undo", "undo"));
		buttons.add(redoButton = this.createToolbarButton(frame, "redo.png", "Redo", "redo"));
		buttons.add(this.createToolbarButton(frame, "add-table.png", "New table", "addTable"));
		buttons.add(this.createToolbarButton(frame, "add-field.png", "New field", "addField"));
		buttons.add(this.createToolbarButton(frame, "add-comment.png", "New comment", "addComment"));
		buttons.add(this.createToolbarButton(frame, "add-link.png", "New link", "addLink"));
		buttons.add(this.createToolbarButton(frame, "delete.png", "Delete", "deleteSelection"));
		buttons.add(this.createToolbarButton(frame, "duplicate.png", "Duplicate", "duplicateSelection"));

		this.add(buttons, BorderLayout.WEST);
	}

	private JButton createToolbarButton(final MainFrame frame, final String icon, final String description, final String actionKey) {
		final JButton button = new JButton();
		final ImageIcon rawIcon = new ImageIcon(PCUtils.readPackagedBytesFile(frame.getClass(), "/icons/" + icon));
		final Image scaled = rawIcon.getImage().getScaledInstance(34, 34, Image.SCALE_SMOOTH);
		button.setIcon(new ImageIcon(scaled));
		button.putClientProperty("baseText", description);
		button.putClientProperty("actionKey", actionKey);

		button.addActionListener(event -> {
			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final ActionEvent actionEvent = new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey);
			final Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(actionEvent);
				canvas.requestFocusInWindow();
			}
		});

		final DiagramCanvas canvas = frame.getActiveCanvas();
		if (canvas != null) {
			final String shortcutText = frame.findShortcutText(canvas, actionKey);
			if (!shortcutText.isBlank()) {
				button.setToolTipText(description + " (" + shortcutText + ")");
			}
		}

		button.setPreferredSize(new Dimension(40, 40));
		return button;
	}

}
