package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.component.ColorButton;

public final class CommentEditorDialog {

	public record Result(String text, Color textColor, Color backgroundColor, Color borderColor, CommentKind kind, CommentBinding binding,
			boolean visibleInConceptual, boolean visibleInLogical, boolean visibleInPhysical) {
	}

	private static final class Holder {
		private Result result;
	}

	private record AssociationTarget(String label, CommentKind kind, CommentBinding binding) {
		private static AssociationTarget standalone() {
			return new AssociationTarget("Standalone", CommentKind.STANDALONE, null);
		}

		private static AssociationTarget forClass(final ClassModel classModel) {
			final String name = classModel.getConceptualName() != null && !classModel.getConceptualName().isBlank()
					? classModel.getConceptualName()
					: classModel.getTechnicalName();
			return new AssociationTarget("Class: " + name,
					CommentKind.BOUND,
					new CommentBinding(BoundTargetType.CLASS, classModel.getId()));
		}

		private static AssociationTarget forLink(final LinkModel linkModel, final boolean conceptual) {
			final String label = (conceptual ? "Conceptual link: " : "Technical link: ")
					+ (linkModel.getName() == null || linkModel.getName().isBlank() ? linkModel.getId() : linkModel.getName());
			return new AssociationTarget(label, CommentKind.BOUND, new CommentBinding(BoundTargetType.LINK, linkModel.getId()));
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.binding, this.kind);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || this.getClass() != obj.getClass()) {
				return false;
			}
			final AssociationTarget other = (AssociationTarget) obj;
			return Objects.equals(this.binding, other.binding) && this.kind == other.kind;
		}

	}

	public static Result showDialog(
			final Component parent,
			final ModelDocument document,
			final CommentModel initialComment,
			final PanelType panelType) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, "Edit comment", Dialog.ModalityType.APPLICATION_MODAL);

		final JTextArea textArea = new JTextArea(initialComment == null ? "" : CommentEditorDialog.safe(initialComment.getText()), 10, 32);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		final ColorButton textColorButton = new ColorButton("Text color",
				initialComment == null ? new Color(0x333333) : initialComment.getTextColor());
		final ColorButton backgroundColorButton = new ColorButton("Background color",
				initialComment == null ? new Color(0xFFF8CC) : initialComment.getBackgroundColor());
		final ColorButton borderColorButton = new ColorButton("Border color",
				initialComment == null ? new Color(0x444444) : initialComment.getBorderColor());

		final JCheckBox conceptualBox = new JCheckBox("Conceptual", initialComment == null || initialComment.isVisibleInConceptual());
		final JCheckBox logicalBox = new JCheckBox("Logical", initialComment == null || initialComment.isVisibleInLogical());
		final JCheckBox physicalBox = new JCheckBox("Physical", initialComment == null || initialComment.isVisibleInPhysical());

		final List<AssociationTarget> associationList = new ArrayList<>();
		associationList.add(AssociationTarget.standalone());
		if (document != null) {
			for (final ClassModel classModel : document.getModel().getClasses()) {
				associationList.add(AssociationTarget.forClass(classModel));
			}
			if (panelType == PanelType.CONCEPTUAL) {
				for (final LinkModel linkModel : document.getModel().getConceptualLinks()) {
					associationList.add(AssociationTarget.forLink(linkModel, true));
				}
			} else {
				for (final LinkModel linkModel : document.getModel().getTechnicalLinks()) {
					associationList.add(AssociationTarget.forLink(linkModel, false));
				}
			}
		}
		final JComboBox<AssociationTarget> associationBox = new JComboBox<>(
				new DefaultComboBoxModel<>(associationList.toArray(AssociationTarget[]::new)));

		associationBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(
					final JList<?> list,
					final Object value,
					final int index,
					final boolean isSelected,
					final boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof final AssociationTarget target) {
					this.setText(target.label());
				}
				return this;
			}
		});

		if (initialComment != null) {
			associationBox.getModel().setSelectedItem(CommentEditorDialog.resolveInitialAssociation(document, initialComment));
		}

		final Holder holder = new Holder();

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		form.add(CommentEditorDialog.row("Association", associationBox));
		form.add(CommentEditorDialog.row("Text", new JScrollPane(textArea)));

		final JPanel visibilityRow = new JPanel(new GridLayout(1, 3, 8, 0));
		visibilityRow.add(conceptualBox);
		visibilityRow.add(logicalBox);
		visibilityRow.add(physicalBox);
		form.add(CommentEditorDialog.row("Visible in", visibilityRow));

		final JPanel colorRow = new JPanel(new GridLayout(1, 3, 8, 0));
		colorRow.add(textColorButton);
		colorRow.add(backgroundColorButton);
		colorRow.add(borderColorButton);
		form.add(CommentEditorDialog.row("Colors", colorRow));

		final JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			final AssociationTarget target = (AssociationTarget) associationBox.getSelectedItem();

			holder.result = new Result(textArea.getText(),
					textColorButton.getSelectedColor(),
					backgroundColorButton.getSelectedColor(),
					borderColorButton.getSelectedColor(),
					target == null ? CommentKind.STANDALONE : target.kind(),
					target == null ? null : target.binding(),
					conceptualBox.isSelected(),
					logicalBox.isSelected(),
					physicalBox.isSelected());
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		buttons.add(saveButton);
		buttons.add(cancelButton);

		dialog.getRootPane().setDefaultButton(saveButton);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		dialog.getRootPane().getActionMap().put("cancel", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(form, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return holder.result;
	}

	private static AssociationTarget resolveInitialAssociation(final ModelDocument document, final CommentModel initialComment) {
		if (initialComment == null || initialComment.getKind() == CommentKind.STANDALONE || initialComment.getBinding() == null) {
			return AssociationTarget.standalone();
		}

		if (initialComment.getBinding().getTargetType() == BoundTargetType.CLASS) {
			for (final ClassModel classModel : document.getModel().getClasses()) {
				if (classModel.getId().equals(initialComment.getBinding().getTargetId())) {
					return AssociationTarget.forClass(classModel);
				}
			}
		}

		for (final LinkModel linkModel : document.getModel().getConceptualLinks()) {
			if (linkModel.getId().equals(initialComment.getBinding().getTargetId())) {
				return AssociationTarget.forLink(linkModel, true);
			}
		}
		for (final LinkModel linkModel : document.getModel().getTechnicalLinks()) {
			if (linkModel.getId().equals(initialComment.getBinding().getTargetId())) {
				return AssociationTarget.forLink(linkModel, false);
			}
		}

		return AssociationTarget.standalone();
	}

	private static JPanel row(final String labelText, final Component component) {
		final JPanel row = new JPanel(new BorderLayout(6, 6));
		row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		row.add(new JLabel(labelText), BorderLayout.NORTH);
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	private static String safe(final String value) {
		return value == null ? "" : value;
	}

	private CommentEditorDialog() {
	}

}
