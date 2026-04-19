package lu.kbra.modelizer_next.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.PanelType;

public final class LinkEditorDialog {

	private LinkEditorDialog() {
	}

	public static Result showDialog(final Component parent, final ModelDocument document, final LinkModel linkModel,
			final PanelType panelType) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit relation", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField nameField = new JTextField(linkModel.getName(), 24);
		final JTextField commentField = new JTextField(linkModel.getComment(), 24);
		final ColorButton colorButton = new ColorButton("Line color", linkModel.getLineColor());

		final JComboBox<ClassModel> fromClassBox = new JComboBox<>(
				document.getModel().getClasses().toArray(ClassModel[]::new));
		final JComboBox<ClassModel> toClassBox = new JComboBox<>(
				document.getModel().getClasses().toArray(ClassModel[]::new));
		fromClassBox.setRenderer(new ClassRenderer(panelType));
		toClassBox.setRenderer(new ClassRenderer(panelType));

		final JComboBox<FieldModel> fromFieldBox = new JComboBox<>();
		final JComboBox<FieldModel> toFieldBox = new JComboBox<>();
		fromFieldBox.setRenderer(new FieldRenderer(panelType));
		toFieldBox.setRenderer(new FieldRenderer(panelType));

		final JComboBox<Cardinality> fromCardinalityBox = new JComboBox<>(Cardinality.values());
		final JComboBox<Cardinality> toCardinalityBox = new JComboBox<>(Cardinality.values());

		fromClassBox.setSelectedItem(findClass(document.getModel().getClasses(), linkModel.getFrom().getClassId()));
		toClassBox.setSelectedItem(findClass(document.getModel().getClasses(), linkModel.getTo().getClassId()));
		fromCardinalityBox.setSelectedItem(linkModel.getCardinalityFrom());
		toCardinalityBox.setSelectedItem(linkModel.getCardinalityTo());

		final Runnable syncFields = () -> {
			fromFieldBox.removeAllItems();
			toFieldBox.removeAllItems();

			final ClassModel fromClass = (ClassModel) fromClassBox.getSelectedItem();
			final ClassModel toClass = (ClassModel) toClassBox.getSelectedItem();

			if (fromClass != null) {
				for (final FieldModel field : fromClass.getFields()) {
					fromFieldBox.addItem(field);
				}
			}
			if (toClass != null) {
				for (final FieldModel field : toClass.getFields()) {
					toFieldBox.addItem(field);
				}
			}

			if (fromClass != null) {
				fromFieldBox.setSelectedItem(findField(fromClass, linkModel.getFrom().getFieldId()));
			}
			if (toClass != null) {
				toFieldBox.setSelectedItem(findField(toClass, linkModel.getTo().getFieldId()));
			}
		};

		fromClassBox.addActionListener(event -> syncFields.run());
		toClassBox.addActionListener(event -> syncFields.run());
		syncFields.run();

		final JPanel topRow = new JPanel(new GridLayout(1, 3, 8, 0));
		topRow.add(labeled("Name", nameField));
		topRow.add(labeled("Comment", commentField));
		topRow.add(labeled("Color", colorButton));

		final JPanel leftPanel = new JPanel(new GridLayout(panelType == PanelType.CONCEPTUAL ? 2 : 2, 1, 6, 6));
		final JPanel rightPanel = new JPanel(new GridLayout(panelType == PanelType.CONCEPTUAL ? 2 : 2, 1, 6, 6));

		if (panelType == PanelType.CONCEPTUAL) {
			leftPanel.setBorder(BorderFactory.createTitledBorder("Table 1"));
			rightPanel.setBorder(BorderFactory.createTitledBorder("Table 2"));
			leftPanel.add(labeled("Cardinality", fromCardinalityBox));
			leftPanel.add(labeled("Table", fromClassBox));
			rightPanel.add(labeled("Cardinality", toCardinalityBox));
			rightPanel.add(labeled("Table", toClassBox));
		} else {
			leftPanel.setBorder(BorderFactory.createTitledBorder("Table 1"));
			rightPanel.setBorder(BorderFactory.createTitledBorder("Table 2"));
			leftPanel.add(labeled("Table", fromClassBox));
			leftPanel.add(labeled("Field", fromFieldBox));
			rightPanel.add(labeled("Table", toClassBox));
			rightPanel.add(labeled("Field", toFieldBox));
		}

		final JPanel bottomRow = new JPanel(new GridLayout(1, 2, 8, 0));
		bottomRow.add(leftPanel);
		bottomRow.add(rightPanel);

		final JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.add(topRow, BorderLayout.NORTH);
		content.add(bottomRow, BorderLayout.CENTER);

		final Holder holder = new Holder();

		final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			final ClassModel fromClass = (ClassModel) fromClassBox.getSelectedItem();
			final ClassModel toClass = (ClassModel) toClassBox.getSelectedItem();
			final FieldModel fromField = (FieldModel) fromFieldBox.getSelectedItem();
			final FieldModel toField = (FieldModel) toFieldBox.getSelectedItem();

			holder.result = new Result(nameField.getText(), commentField.getText(), colorButton.getSelectedColor(),
					fromClass == null ? null : fromClass.getId(), toClass == null ? null : toClass.getId(),
					panelType == PanelType.CONCEPTUAL ? null : fromField == null ? null : fromField.getId(),
					panelType == PanelType.CONCEPTUAL ? null : toField == null ? null : toField.getId(),
					panelType == PanelType.CONCEPTUAL ? (Cardinality) fromCardinalityBox.getSelectedItem() : null,
					panelType == PanelType.CONCEPTUAL ? (Cardinality) toCardinalityBox.getSelectedItem() : null);
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(content, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(saveButton);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return holder.result;
	}

	private static JPanel labeled(final String labelText, final Component component) {
		final JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.add(new JLabel(labelText), BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	private static ClassModel findClass(final List<ClassModel> classes, final String id) {
		for (final ClassModel classModel : classes) {
			if (classModel.getId().equals(id)) {
				return classModel;
			}
		}
		return null;
	}

	private static FieldModel findField(final ClassModel classModel, final String id) {
		if (classModel == null || id == null) {
			return null;
		}
		for (final FieldModel fieldModel : classModel.getFields()) {
			if (fieldModel.getId().equals(id)) {
				return fieldModel;
			}
		}
		return null;
	}

	private static final class ClassRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		private final PanelType panelType;

		private ClassRenderer(final PanelType panelType) {
			this.panelType = panelType;
		}

		@Override
		public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof ClassModel classModel) {
				this.setText(this.panelType == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
						: classModel.getNames().getTechnicalName());
			}
			return this;
		}
	}

	private static final class FieldRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		private final PanelType panelType;

		private FieldRenderer(final PanelType panelType) {
			this.panelType = panelType;
		}

		@Override
		public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof FieldModel fieldModel) {
				this.setText(this.panelType == PanelType.CONCEPTUAL ? fieldModel.getNames().getName()
						: fieldModel.getNames().getTechnicalName());
			}
			return this;
		}
	}

	private static final class Holder {
		private Result result;
	}

	public record Result(String name, String comment, Color lineColor, String fromClassId, String toClassId,
			String fromFieldId, String toFieldId, Cardinality cardinalityFrom, Cardinality cardinalityTo) {
	}

}