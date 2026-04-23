package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.ui.ColorButton;

public final class ClassEditorDialog {

	private static final class Holder {
		private Result result;
	}

	public record Result(String conceptualName, String technicalName, Color textColor, Color backgroundColor, Color borderColor) {
	}

	private static JPanel row(final String labelText, final Component component) {
		final JPanel row = new JPanel(new BorderLayout(6, 6));
		row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		row.add(new JLabel(labelText), BorderLayout.NORTH);
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	public static Result showDialog(final Component parent, final ClassModel classModel) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit table", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField conceptualNameField = new JTextField(classModel.getNames().getConceptualName(), 24);
		final JTextField technicalNameField = new JTextField(classModel.getNames().getTechnicalName(), 24);

		final ColorButton textColorButton = new ColorButton("Text color", classModel.getStyle().getTextColor());
		final ColorButton backgroundColorButton = new ColorButton("Background color", classModel.getStyle().getBackgroundColor());
		final ColorButton borderColorButton = new ColorButton("Border color", classModel.getStyle().getBorderColor());

		final Holder holder = new Holder();

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		form.add(ClassEditorDialog.row("Conceptual name", conceptualNameField));
		form.add(ClassEditorDialog.row("Technical name", technicalNameField));

		final JPanel colorRow = new JPanel(new GridLayout(1, 3, 8, 0));
		colorRow.add(textColorButton);
		colorRow.add(backgroundColorButton);
		colorRow.add(borderColorButton);
		form.add(ClassEditorDialog.row("Colors", colorRow));

		final JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			holder.result = new Result(conceptualNameField.getText(),
					technicalNameField.getText(),
					textColorButton.getSelectedColor(),
					backgroundColorButton.getSelectedColor(),
					borderColorButton.getSelectedColor());
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		buttons.add(saveButton);
		buttons.add(cancelButton);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(form, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(saveButton);
		dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		dialog.getRootPane().getActionMap().put("cancel", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return holder.result;
	}

	private ClassEditorDialog() {
	}

}
