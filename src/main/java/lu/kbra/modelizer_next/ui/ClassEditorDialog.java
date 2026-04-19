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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.ui.ClassEditorDialog.Result;

public final class ClassEditorDialog {

	private ClassEditorDialog() {
	}

	public static Result showDialog(final Component parent, final ClassModel classModel) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit table", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField conceptualNameField = new JTextField(classModel.getNames().getConceptualName(), 24);
		final JTextField technicalNameField = new JTextField(classModel.getNames().getTechnicalName(), 24);
		final JTextArea commentArea = new JTextArea(classModel.getComment(), 8, 32);
		commentArea.setLineWrap(true);
		commentArea.setWrapStyleWord(true);

		final ColorButton textColorButton = new ColorButton("Text color", classModel.getStyle().getTextColor());
		final ColorButton backgroundColorButton = new ColorButton("Background color",
				classModel.getStyle().getBackgroundColor());
		final ColorButton borderColorButton = new ColorButton("Border color", classModel.getStyle().getBorderColor());

		final Holder holder = new Holder();

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		form.add(row("Conceptual name", conceptualNameField));
		form.add(row("Technical name", technicalNameField));
		form.add(row("Comment", new JScrollPane(commentArea)));

		final JPanel colorRow = new JPanel(new GridLayout(1, 3, 8, 0));
		colorRow.add(textColorButton);
		colorRow.add(backgroundColorButton);
		colorRow.add(borderColorButton);
		form.add(row("Colors", colorRow));

		final JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			holder.result = new Result(conceptualNameField.getText(), technicalNameField.getText(),
					commentArea.getText(), textColorButton.getSelectedColor(), backgroundColorButton.getSelectedColor(),
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

	public record Result(String conceptualName, String technicalName, String comment, Color textColor,
			Color backgroundColor, Color borderColor) {
	}

}