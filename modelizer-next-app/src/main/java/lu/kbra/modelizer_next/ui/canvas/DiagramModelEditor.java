package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.ElementStyle;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.RenameDialog;

/**
 * Contains document editing helpers for model changes made from the canvas.
 */
interface DiagramModelEditor extends DiagramCanvasExt {

	default JTextField createRenamingField() {
		final JTextField renamingField = new JTextField("editing");
		renamingField.setVisible(false);
		renamingField.setFocusTraversalKeysEnabled(false);
		renamingField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(final FocusEvent e) {
				if (!e.isTemporary() && renamingField.isVisible() && e.getOppositeComponent() != renamingField) {
					SwingUtilities.invokeLater(() -> {
						if (!renamingField.hasFocus()) {
							DiagramModelEditor.this.cancelRenamingElement();
						}
					});
				}
			}

		});
		renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
		renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "submit-next");
		renamingField.getInputMap(JComponent.WHEN_FOCUSED)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "submit-previous");
		renamingField.getActionMap().put("cancel", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramModelEditor.this.cancelRenamingElement();
			}

		});
		renamingField.getActionMap().put("submit", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramModelEditor.this.confirmRenamingElement(0);
			}

		});
		renamingField.getActionMap().put("submit-next", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramModelEditor.this.confirmRenamingElement(1);
			}

		});
		renamingField.getActionMap().put("submit-previous", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				DiagramModelEditor.this.confirmRenamingElement(-1);
			}

		});

		return renamingField;
	}

	default void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");
		commentModel.getVisibility().set(this.getPanelType());
		this.getCanvas().applyDefaultPaletteToComment(commentModel);

		if (this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() == SelectedType.COMMENT) {
			final CommentModel cm = this.getCanvas().findCommentById(this.getCanvas().selectedElement.commentId());
			final CommentBinding cb = cm.getBinding();
			if (cm.getKind() != CommentKind.STANDALONE) {
				commentModel.setKind(CommentKind.BOUND);
				commentModel.setBinding(new CommentBinding(cb.getTargetType(), cb.getTargetId()));
			}
		} else if (this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() != SelectedType.COMMENT
				&& this.getCanvas().selectedElement.type() != SelectedType.NONE) {
			commentModel.setKind(CommentKind.BOUND);
			commentModel.setBinding(switch (this.getCanvas().selectedElement.type()) {
			case CLASS -> new CommentBinding(BoundTargetType.CLASS, this.getCanvas().selectedElement.classId());
			case LINK -> new CommentBinding(BoundTargetType.LINK, this.getCanvas().selectedElement.linkId());
			case FIELD -> new CommentBinding(BoundTargetType.CLASS, this.getCanvas().selectedElement.classId());
			default -> throw new IllegalStateException("Cannot bind comment to: " + this.getCanvas().selectedElement);
			});
		}

		this.getCanvas().document.getModel().getComments().add(commentModel);

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Point2D.Double center = this.getCanvas().mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2D(220, 80));

		this.getCanvas().select(SelectedElement.forComment(commentModel.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void addField() {
		final ClassModel targetClass;

		if (this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() == SelectedType.CLASS
				|| this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() == SelectedType.FIELD) {
			targetClass = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
		} else {
			return;
		}

		if (targetClass == null) {
			return;
		}

		final FieldModel fieldModel = new FieldModel();
		fieldModel.getNames().setConceptualName("New field");
		this.getCanvas().applyDefaultPaletteToField(fieldModel);
		targetClass.getFields().add(fieldModel);

		this.getCanvas().select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
		this.getCanvas().invokeRenamingElement(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
	}

	default void addLink() {
		final LinkModel linkModel = new LinkModel();
		if (this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() == SelectedType.CLASS) {
			linkModel.setFrom(new LinkEnd(this.getCanvas().selectedElement.classId(), null));
		} else {
			linkModel.setFrom(new LinkEnd(null, null));
		}
		linkModel.setTo(new LinkEnd(null, null));

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			linkModel.setName("new relation");
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		} else {
			linkModel.setName("NEW_LINK");
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(this.getCanvas(), this.getCanvas().document, linkModel, this.getPanelType());
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		final LinkModel createdLink = new LinkModel();
		createdLink.setName(result.name());
		createdLink.setLineColor(result.lineColor());
		createdLink.setAssociationClassId(result.associationClassId());
		createdLink.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		createdLink.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			createdLink.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			createdLink.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
			this.getCanvas().document.getModel().getConceptualLinks().add(createdLink);
		} else {
			createdLink.setCardinalityFrom(null);
			createdLink.setCardinalityTo(null);
			this.getCanvas().document.getModel().getTechnicalLinks().add(createdLink);
		}
		this.getCanvas().applyDefaultPaletteToLink(createdLink);

		this.getCanvas().findOrCreateLinkLayout(createdLink.getId());
		this.getCanvas().select(SelectedElement.forLink(createdLink.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		this.getCanvas().applyDefaultPaletteToClass(classModel);

		this.getDocument().getModel().getClasses().add(classModel);

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Point2D.Double center = this.getCanvas().mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, DiagramCanvas.CLASS_HEADER_HEIGHT));

		this.getCanvas().select(SelectedElement.forClass(classModel.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
		this.getCanvas().invokeRenamingElement(SelectedElement.forClass(classModel.getId()));
	}

	default void deleteSelection() {
		if (this.getCanvas().selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.getCanvas().selectedElements);

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case LINK -> this.getCanvas().deleteLink(element.linkId());
			case COMMENT -> this.getCanvas().deleteComment(element.commentId());
			case FIELD -> this.getCanvas().deleteField(element.classId(), element.fieldId());
			case CLASS -> this.getCanvas().deleteClass(element.classId());
			default -> {
			}
			}
		}

		this.getCanvas().clearSelection();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	@SuppressWarnings("incomplete-switch")
	default void editSelected() {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}
		switch (this.getCanvas().selectedElement.type()) {
		case CLASS -> this.getCanvas().editClass(this.getCanvas().selectedElement.classId());
		case FIELD -> this.getCanvas().editField(this.getCanvas().selectedElement.classId(), this.getCanvas().selectedElement.fieldId());
		case COMMENT -> this.getCanvas().editComment(this.getCanvas().selectedElement.commentId());
		case LINK -> this.getCanvas().editLink(this.getCanvas().selectedElement.linkId());
		}
	}

	default void moveFieldSelection(final int delta) {
		if (this.getCanvas().selectedElement != null && this.getCanvas().selectedElement.type() == SelectedType.CLASS) {
			final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
			if (classModel.getFields().isEmpty()) {
				return;
			}
			this.getCanvas()
					.select(SelectedElement.forField(this.getCanvas().selectedElement.classId(), classModel.getFields().get(0).getId()));
			return;
		}

		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
		if (classModel == null) {
			return;
		}

		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
		int currentIndex = -1;
		for (int i = 0; i < visibleFields.size(); i++) {
			if (Objects.equals(visibleFields.get(i).getId(), this.getCanvas().selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex == 0 && delta == -1) {
			this.getCanvas().select(SelectedElement.forClass(classModel.getId()));
			return;
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= visibleFields.size()) {
			return;
		}

		this.getCanvas().select(SelectedElement.forField(classModel.getId(), visibleFields.get(newIndex).getId()));
	}

	default void moveSelectedFieldInList(final int delta) {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().selectedElement.classId());
		if (classModel == null) {
			return;
		}

		int currentIndex = -1;
		for (int i = 0; i < classModel.getFields().size(); i++) {
			if (Objects.equals(classModel.getFields().get(i).getId(), this.getCanvas().selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= classModel.getFields().size()) {
			return;
		}

		final FieldModel moved = classModel.getFields().remove(currentIndex);
		classModel.getFields().add(newIndex, moved);

		this.getCanvas().select(SelectedElement.forField(classModel.getId(), moved.getId()));
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void renameSelection() {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		final String title;
		final String currentValue;

		switch (this.getCanvas().selectedElement.type()) {
		case CLASS, FIELD -> {
			this.getCanvas().invokeRenamingElement(this.getCanvas().selectedElement);
			return;
		}
		case COMMENT -> {
			title = "Rename comment";
			currentValue = this.getCanvas().getEditableCommentText(this.getCanvas().selectedElement.commentId());
		}
		case LINK -> {
			if (this.getPanelType() != PanelType.CONCEPTUAL) {
				return;
			}
			final LinkModel linkModel = this.getCanvas().findLinkById(this.getCanvas().selectedElement.linkId());
			if (linkModel == null) {
				return;
			}
			title = "Rename link";
			currentValue = linkModel.getName();
		}
		default -> {
			return;
		}
		}

		final String newValue = RenameDialog.showDialog(this.getCanvas(), title, currentValue);
		if (newValue == null) {
			return;
		}

		switch (this.getCanvas().selectedElement.type()) {
		case CLASS ->
			this.getCanvas().setEditableClassName(this.getCanvas().findClassById(this.getCanvas().selectedElement.classId()), newValue);
		case FIELD -> this.getCanvas()
				.setEditableFieldName(
						this.getCanvas()
								.findFieldById(this.getCanvas().selectedElement.classId(), this.getCanvas().selectedElement.fieldId()),
						newValue);
		case COMMENT -> this.getCanvas().setEditableCommentText(this.getCanvas().selectedElement.commentId(), newValue);
		case LINK -> this.getCanvas().findLinkById(this.getCanvas().selectedElement.linkId()).setName(newValue);
		default -> {
		}
		}

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void invokeRenamingElement(final SelectedElement newRenamingElement) {
		if (this.getCanvas().renamingElement != null) {
			this.getCanvas().renamingField.setVisible(false);
			this.getCanvas().renamingElement = null;
		}

		this.getCanvas().renamingElement = newRenamingElement;
		getCanvas().selectedElements.clear();
		getCanvas().select(newRenamingElement);
		final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().renamingElement.classId());
		final NodeLayout nl = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, this.getCanvas().renamingElement.classId());

		final Point2D pos;
		final Point2D size;
		final String currentValue;
		final ElementStyle style;

		if (this.getCanvas().renamingElement.type() == SelectedType.CLASS) {
			pos = this.getCanvas().worldToScreen(nl.getPosition());
			size = this.getCanvas().worldToScreenZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_HEADER_HEIGHT));
			currentValue = classModel.getNames().get(this.getCanvas().panelType);
			style = classModel.getStyle();
		} else {
			final FieldModel fieldModel = this.getCanvas()
					.findFieldById(this.getCanvas().renamingElement.classId(), this.getCanvas().renamingElement.fieldId());
			final Point2D fieldPos = new Point2D.Double(nl.getPosition().getX(),
					nl.getPosition().getY() + DiagramCanvas.CLASS_ROW_HEIGHT * (classModel.getFieldIndex(fieldModel.getId()) + 1) + 6);
			pos = this.getCanvas().worldToScreen(fieldPos);
			size = this.getCanvas().worldToScreenZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_ROW_HEIGHT));
			currentValue = fieldModel.getNames().get(this.getCanvas().panelType);
			style = fieldModel.getStyle();
		}

		this.getCanvas().renamingField.setBounds((int) pos.getX(), (int) pos.getY(), (int) size.getX(), (int) size.getY());

		this.getCanvas().renamingField.setBackground(style.getBackgroundColor());
		this.getCanvas().renamingField.setForeground(style.getTextColor());
		this.getCanvas().renamingField.setBorder(new CompoundBorder(new LineBorder(style.getBorderColor()),
				new EmptyBorder(0, DiagramCanvas.PADDING, 0, DiagramCanvas.PADDING)));

		SwingUtilities.invokeLater(() -> {
			this.getCanvas().renamingField.setText(currentValue);
			this.getCanvas().renamingField.setVisible(true);
			this.getCanvas().renamingField.requestFocusInWindow();
			this.getCanvas().renamingField.selectAll();
			this.getCanvas().repaint();
		});
	}

	default void cancelRenamingElement() {
		if (this.getCanvas().renamingElement == null) {
			return;
		}
		this.getCanvas().renamingField.setVisible(false);
		this.getCanvas().renamingElement = null;
		this.getCanvas().repaint();
	}

	@SuppressWarnings("incomplete-switch")
	default void confirmRenamingElement(final int nextDir) {
		if (this.getCanvas().renamingElement == null) {
			this.getCanvas().renamingField.setVisible(false);
			this.getCanvas().repaint();
			return;
		}

		boolean next = nextDir != 0;
		switch (this.getCanvas().renamingElement.type()) {
		case CLASS -> {
			final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().renamingElement.classId());
			classModel.getNames().set(this.getCanvas().getPanelType(), this.getCanvas().renamingField.getText());

			if (next) {
				if (classModel.getFields().size() > 0) {
					this.getCanvas()
							.invokeRenamingElement(SelectedElement.forField(classModel.getId(),
									(nextDir < 0 ? classModel.getFields().getLast() : classModel.getFields().getFirst()).getId()));
				} else {
					next = false;
				}
			}
		}
		case FIELD -> {
			final FieldModel fieldModel = this.getCanvas()
					.findFieldById(this.getCanvas().renamingElement.classId(), this.getCanvas().renamingElement.fieldId());
			fieldModel.getNames().set(this.getCanvas().getPanelType(), this.getCanvas().renamingField.getText());

			if (next) {
				final ClassModel classModel = this.getCanvas().findClassById(this.getCanvas().renamingElement.classId());
				final int idx = classModel.getFieldIndex(fieldModel.getId());
				if (idx + nextDir < 0 || idx + nextDir > classModel.getFields().size() - 1) {
					this.getCanvas().invokeRenamingElement(SelectedElement.forClass(classModel.getId()));
				} else {
					this.getCanvas()
							.invokeRenamingElement(
									SelectedElement.forField(classModel.getId(), classModel.getFields().get(idx + nextDir).getId()));
				}
			}
		}
		}

		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().notifySelectionChanged();

		if (!next) {
			this.getCanvas().renamingField.setVisible(false);
			this.getCanvas().renamingElement = null;
			SwingUtilities.invokeLater(this.getCanvas()::requestFocusInWindow);
			this.getCanvas().repaint();
		}
	}

}
