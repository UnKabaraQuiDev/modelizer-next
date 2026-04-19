package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.ui.ColorButton;

public final class FieldEditorDialog {

	private FieldEditorDialog() {
	}

	public static Result showDialog(final Component parent, final FieldModel fieldModel, final IntConsumer moveCallback) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit field", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField nameField = new JTextField(fieldModel.getNames().getName(), 24);
		final JTextField technicalNameField = new JTextField(fieldModel.getNames().getTechnicalName(), 24);

		final JCheckBox primaryKeyBox = new JCheckBox("PK", fieldModel.isPrimaryKey());
		final JCheckBox uniqueBox = new JCheckBox("UQ", fieldModel.isUnique());
		final JCheckBox notNullBox = new JCheckBox("NN", fieldModel.isNotNull());

		final ColorButton textColorButton = new ColorButton("Text color", fieldModel.getStyle().getTextColor());
		final ColorButton backgroundColorButton = new ColorButton("Background color", fieldModel.getStyle().getBackgroundColor());

		final Holder holder = new Holder();

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		form.add(FieldEditorDialog.row("Name", nameField));
		form.add(FieldEditorDialog.row("Technical name", technicalNameField));

		final JPanel flagsRow = new JPanel(new GridLayout(1, 3, 8, 0));
		flagsRow.add(primaryKeyBox);
		flagsRow.add(uniqueBox);
		flagsRow.add(notNullBox);
		form.add(FieldEditorDialog.row("Flags", flagsRow));

		final JPanel colorRow = new JPanel(new GridLayout(1, 2, 8, 0));
		colorRow.add(textColorButton);
		colorRow.add(backgroundColorButton);
		form.add(FieldEditorDialog.row("Colors", colorRow));

		final JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton upButton = new JButton("Move up");
		final JButton downButton = new JButton("Move down");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			holder.result = new Result(nameField.getText(),
					technicalNameField.getText(),
					primaryKeyBox.isSelected(),
					uniqueBox.isSelected(),
					notNullBox.isSelected(),
					textColorButton.getSelectedColor(),
					backgroundColorButton.getSelectedColor(),
					0);
			dialog.dispose();
		});
		upButton.addActionListener(event -> {
			FieldEditorDialog.applyFieldValues(fieldModel,
					nameField,
					technicalNameField,
					primaryKeyBox,
					uniqueBox,
					notNullBox,
					textColorButton,
					backgroundColorButton);
			if (moveCallback != null) {
				moveCallback.accept(-1);
			}
		});
		downButton.addActionListener(event -> {
			FieldEditorDialog.applyFieldValues(fieldModel,
					nameField,
					technicalNameField,
					primaryKeyBox,
					uniqueBox,
					notNullBox,
					textColorButton,
					backgroundColorButton);
			if (moveCallback != null) {
				moveCallback.accept(1);
			}
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

	private static void applyFieldValues(
			final FieldModel fieldModel,
			final JTextField nameField,
			final JTextField technicalNameField,
			final JCheckBox primaryKeyBox,
			final JCheckBox uniqueBox,
			final JCheckBox notNullBox,
			final ColorButton textColorButton,
			final ColorButton backgroundColorButton) {
		fieldModel.getNames().setName(nameField.getText());
		fieldModel.getNames().setTechnicalName(technicalNameField.getText());
		fieldModel.setPrimaryKey(primaryKeyBox.isSelected());
		fieldModel.setUnique(uniqueBox.isSelected());
		fieldModel.setNotNull(notNullBox.isSelected());
		fieldModel.getStyle().setTextColor(textColorButton.getSelectedColor());
		fieldModel.getStyle().setBackgroundColor(backgroundColorButton.getSelectedColor());
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

	public record Result(String name, String technicalName, boolean primaryKey, boolean unique, boolean notNull, Color textColor,
			Color backgroundColor, int moveDelta) {
	}

}
