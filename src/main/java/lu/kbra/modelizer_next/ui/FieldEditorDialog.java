package lu.kbra.modelizer_next.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.ui.FieldEditorDialog.Result;

public final class FieldEditorDialog {

	private FieldEditorDialog() {
	}

	public static Result showDialog(final Component parent, final FieldModel fieldModel) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit field", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField nameField = new JTextField(fieldModel.getNames().getName(), 24);
		final JTextField technicalNameField = new JTextField(fieldModel.getNames().getTechnicalName(), 24);

		final ColorButton textColorButton = new ColorButton("Text color", fieldModel.getStyle().getTextColor());
		final ColorButton backgroundColorButton = new ColorButton("Background color",
				fieldModel.getStyle().getBackgroundColor());

		final Holder holder = new Holder();

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		form.add(row("Name", nameField));
		form.add(row("Technical name", technicalNameField));

		final JPanel colorRow = new JPanel(new GridLayout(1, 2, 8, 0));
		colorRow.add(textColorButton);
		colorRow.add(backgroundColorButton);
		form.add(row("Colors", colorRow));

		final JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton upButton = new JButton("Move up");
		final JButton downButton = new JButton("Move down");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			holder.result = new Result(nameField.getText(), technicalNameField.getText(),
					textColorButton.getSelectedColor(), backgroundColorButton.getSelectedColor(), 0);
			dialog.dispose();
		});
		upButton.addActionListener(event -> {
			holder.result = new Result(nameField.getText(), technicalNameField.getText(),
					textColorButton.getSelectedColor(), backgroundColorButton.getSelectedColor(), -1);
			dialog.dispose();
		});
		downButton.addActionListener(event -> {
			holder.result = new Result(nameField.getText(), technicalNameField.getText(),
					textColorButton.getSelectedColor(), backgroundColorButton.getSelectedColor(), 1);
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		buttons.add(saveButton);
		buttons.add(upButton);
		buttons.add(downButton);
		buttons.add(cancelButton);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(form, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(saveButton);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return holder.result;
	}

	private static JPanel row(final String labelText, final Component component) {
		final JPanel row = new JPanel(new BorderLayout(6, 6));
		row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		row.add(new JLabel(labelText), BorderLayout.NORTH);
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	private static final class Holder {
		private Result result;
	}

	public record Result(String name, String technicalName, Color textColor, Color backgroundColor, int moveDelta) {
	}

}