package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.ColorButton;

public final class LinkEditorDialog {

	private LinkEditorDialog() {
	}

	public static Result showDialog(
			final Component parent,
			final ModelDocument document,
			final LinkModel linkModel,
			final PanelType panelType) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit relation", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextField nameField = new JTextField(linkModel.getName(), 24);
		final JTextField commentField = new JTextField(linkModel.getComment(), 24);
		final ColorButton colorButton = new ColorButton("Line color", linkModel.getLineColor());

		final JComboBox<ClassModel> fromClassBox = new JComboBox<>(document.getModel().getClasses().toArray(ClassModel[]::new));
		final JComboBox<ClassModel> toClassBox = new JComboBox<>(document.getModel().getClasses().toArray(ClassModel[]::new));
		fromClassBox.setRenderer(new ClassRenderer(panelType));
		toClassBox.setRenderer(new ClassRenderer(panelType));

		final JComboBox<FieldModel> fromFieldBox = new JComboBox<>();
		final JComboBox<FieldModel> toFieldBox = new JComboBox<>();
		fromFieldBox.setRenderer(new FieldRenderer(panelType));
		toFieldBox.setRenderer(new FieldRenderer(panelType));

		final JComboBox<Cardinality> fromCardinalityBox = new JComboBox<>(Cardinality.values());
		final JComboBox<Cardinality> toCardinalityBox = new JComboBox<>(Cardinality.values());

		final JComboBox<AssociationOption> associationBox = new JComboBox<>();
		associationBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(
					final javax.swing.JList<?> list,
					final Object value,
					final int index,
					final boolean isSelected,
					final boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof final AssociationOption option) {
					this.setText(option.label());
				}
				return this;
			}
		});

		fromClassBox.setSelectedItem(LinkEditorDialog.findClass(document.getModel().getClasses(), linkModel.getFrom().getClassId()));
		toClassBox.setSelectedItem(LinkEditorDialog.findClass(document.getModel().getClasses(), linkModel.getTo().getClassId()));
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
					if (panelType == PanelType.CONCEPTUAL || field.isPrimaryKey()) {
						toFieldBox.addItem(field);
					}
				}
			}

			if (fromClass != null) {
				fromFieldBox.setSelectedItem(LinkEditorDialog.findField(fromClass, linkModel.getFrom().getFieldId()));
			}

			if (toClass != null) {
				final FieldModel currentTargetField = LinkEditorDialog.findField(toClass, linkModel.getTo().getFieldId());
				if (panelType == PanelType.CONCEPTUAL) {
					toFieldBox.setSelectedItem(currentTargetField);
				} else if (currentTargetField != null && currentTargetField.isPrimaryKey()) {
					toFieldBox.setSelectedItem(currentTargetField);
				} else if (toFieldBox.getItemCount() > 0) {
					toFieldBox.setSelectedIndex(0);
				}
			}
		};

		final Runnable syncAssociation = () -> {
			final AssociationOption currentSelection = (AssociationOption) associationBox.getSelectedItem();
			final String selectedAssociationId = currentSelection == null ? linkModel.getAssociationClassId() : currentSelection.classId();

			associationBox.removeAllItems();
			associationBox.addItem(AssociationOption.none());

			final ClassModel fromClass = (ClassModel) fromClassBox.getSelectedItem();
			final ClassModel toClass = (ClassModel) toClassBox.getSelectedItem();

			for (final ClassModel classModel : document.getModel().getClasses()) {
				if (fromClass != null && fromClass.getId().equals(classModel.getId())) {
					continue;
				}
				if (toClass != null && toClass.getId().equals(classModel.getId())) {
					continue;
				}
				associationBox.addItem(AssociationOption.forClass(classModel, panelType));
			}

			associationBox.setSelectedItem(LinkEditorDialog.findAssociationOption(associationBox, selectedAssociationId));
		};

		fromClassBox.addActionListener(event -> {
			syncFields.run();
			syncAssociation.run();
		});
		toClassBox.addActionListener(event -> {
			syncFields.run();
			syncAssociation.run();
		});
		syncFields.run();
		syncAssociation.run();

		final JPanel topRow = new JPanel(new GridLayout(1, 3, 8, 0));
		topRow.add(LinkEditorDialog.labeled("Name", nameField));
		topRow.add(LinkEditorDialog.labeled("Comment", commentField));
		topRow.add(LinkEditorDialog.labeled("Color", colorButton));

		final JPanel leftPanel = new JPanel(new GridLayout(panelType == PanelType.CONCEPTUAL ? 2 : 2, 1, 6, 6));
		final JPanel rightPanel = new JPanel(new GridLayout(panelType == PanelType.CONCEPTUAL ? 2 : 2, 1, 6, 6));

		if (panelType == PanelType.CONCEPTUAL) {
			leftPanel.setBorder(BorderFactory.createTitledBorder("Table 1"));
			leftPanel.add(LinkEditorDialog.labeled("Cardinality", fromCardinalityBox));
			leftPanel.add(LinkEditorDialog.labeled("Table", fromClassBox));
			rightPanel.setBorder(BorderFactory.createTitledBorder("Table 2"));
			rightPanel.add(LinkEditorDialog.labeled("Cardinality", toCardinalityBox));
			rightPanel.add(LinkEditorDialog.labeled("Table", toClassBox));
		} else {
			leftPanel.setBorder(BorderFactory.createTitledBorder("From"));
			leftPanel.add(LinkEditorDialog.labeled("Table", fromClassBox));
			leftPanel.add(LinkEditorDialog.labeled("Field", fromFieldBox));
			rightPanel.setBorder(BorderFactory.createTitledBorder("To"));
			rightPanel.add(LinkEditorDialog.labeled("Table", toClassBox));
			rightPanel.add(LinkEditorDialog.labeled("Field", toFieldBox));
		}

		final JPanel bottomRow = new JPanel(new GridLayout(1, 2, 8, 0));
		bottomRow.add(leftPanel);
		bottomRow.add(rightPanel);

		final JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.add(topRow, BorderLayout.NORTH);
		content.add(bottomRow, BorderLayout.CENTER);
		if (panelType == PanelType.CONCEPTUAL) {
			content.add(LinkEditorDialog.labeled("Association table", associationBox), BorderLayout.SOUTH);
		}

		final Holder holder = new Holder();

		final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			final ClassModel fromClass = (ClassModel) fromClassBox.getSelectedItem();
			final ClassModel toClass = (ClassModel) toClassBox.getSelectedItem();
			final FieldModel fromField = (FieldModel) fromFieldBox.getSelectedItem();
			final FieldModel toField = (FieldModel) toFieldBox.getSelectedItem();

			if (panelType != PanelType.CONCEPTUAL) {
				final FieldModel selectedToField = (FieldModel) toFieldBox.getSelectedItem();
				final FieldModel selectedFromField = (FieldModel) fromFieldBox.getSelectedItem();

				if (selectedToField == null || !selectedToField.isPrimaryKey() || (selectedFromField == null)) {
					return;
				}
			}

			final AssociationOption associationOption = (AssociationOption) associationBox.getSelectedItem();

			holder.result = new Result(nameField.getText(), commentField.getText(), colorButton.getSelectedColor(),
					fromClass == null ? null : fromClass.getId(), toClass == null ? null : toClass.getId(),
					panelType == PanelType.CONCEPTUAL ? null
							: fromField == null ? null
							: fromField.getId(),
					panelType == PanelType.CONCEPTUAL ? null
							: toField == null ? null
							: toField.getId(),
					panelType == PanelType.CONCEPTUAL ? (Cardinality) fromCardinalityBox.getSelectedItem() : null,
					panelType == PanelType.CONCEPTUAL ? (Cardinality) toCardinalityBox.getSelectedItem() : null,
					panelType == PanelType.CONCEPTUAL && associationOption != null ? associationOption.classId() : null);
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(content, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
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

	private static AssociationOption findAssociationOption(final JComboBox<AssociationOption> box, final String classId) {
		for (int i = 0; i < box.getItemCount(); i++) {
			final AssociationOption option = box.getItemAt(i);
			if (Objects.equals(option.classId(), classId)) {
				return option;
			}
		}
		return box.getItemAt(0);
	}

	private record AssociationOption(String classId, String label) {
		private static AssociationOption none() {
			return new AssociationOption(null, "None");
		}

		private static AssociationOption forClass(final ClassModel classModel, final PanelType panelType) {
			final String label = panelType == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
					: classModel.getNames().getTechnicalName();
			return new AssociationOption(classModel.getId(), label);
		}
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
		public Component getListCellRendererComponent(
				final javax.swing.JList<?> list,
				final Object value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof final ClassModel classModel) {
				this
						.setText(this.panelType == PanelType.CONCEPTUAL ? classModel.getNames().getConceptualName()
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
		public Component getListCellRendererComponent(
				final javax.swing.JList<?> list,
				final Object value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof final FieldModel fieldModel) {
				this
						.setText(this.panelType == PanelType.CONCEPTUAL ? fieldModel.getNames().getName()
								: fieldModel.getNames().getTechnicalName());
			}
			return this;
		}
	}

	private static final class Holder {
		private Result result;
	}

	public record Result(String name, String comment, Color lineColor, String fromClassId, String toClassId, String fromFieldId,
			String toFieldId, Cardinality cardinalityFrom, Cardinality cardinalityTo, String associationClassId) {
	}

}
