package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public final class RenameDialog {

	private RenameDialog() {
	}

	public static String showDialog(final Component parent, final String title, final String initialValue) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = owner instanceof final Frame frame ? new JDialog(frame, title, true)
				: new JDialog((Frame) null, title, true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final JTextField textField = new JTextField(initialValue == null ? "" : initialValue, 30);
		final ResultHolder resultHolder = new ResultHolder();

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton okButton = new JButton("OK");
		final JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(event -> {
			resultHolder.value = textField.getText();
			dialog.dispose();
		});

		cancelButton.addActionListener(event -> {
			resultHolder.cancelled = true;
			dialog.dispose();
		});

		textField.addActionListener(event -> okButton.doClick());

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		dialog.getRootPane().setDefaultButton(okButton);

		final AbstractAction cancelAction = new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				cancelButton.doClick();
			}
		};

		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		dialog.getRootPane().getActionMap().put("cancel", cancelAction);

		textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		textField.getActionMap().put("cancel", cancelAction);

		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(textField, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.getRootPane().setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
		dialog.pack();
		dialog.setLocationRelativeTo(parent);

		SwingUtilities.invokeLater(() -> {
			textField.requestFocusInWindow();
			textField.selectAll();
		});

		dialog.setVisible(true);
		return resultHolder.cancelled ? null : resultHolder.value;
	}

	private static final class ResultHolder {
		private boolean cancelled;
		private String value;
	}

}