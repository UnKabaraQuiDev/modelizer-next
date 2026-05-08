package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.domain.data.DisplayValueOwner;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingComponents;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingContext;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingElement.RenamingType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.pclib.PCUtils;

/**
 * Contains document editing helpers for model changes made from the canvas.
 */
interface DiagramModelEditor extends DiagramCanvasExt {

	default void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");
		commentModel.setVisibility(this.getPanelType());
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
		fieldModel.setConceptualName("New field");
		this.getCanvas().applyDefaultPaletteToField(fieldModel);
		targetClass.getFields().add(fieldModel);
		fieldModel.setTechnicalOnly(this.getPanelType().isTechnical());

		if (this.getCanvas().isRenamingElement() && this.getCanvas().renamingElement.type().isClass()
				&& this.getCanvas().renamingElement.classId().equals(targetClass.getId())) {
			this.getCanvas().confirmRenamingElement(0, false);
		}
		this.getCanvas().select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
		this.getCanvas().invokeRenamingElement(RenamingElement.forField(targetClass.getId(), fieldModel.getId()));
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
			linkModel.setLabel("new relation");
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		} else {
			linkModel.setLabel("NEW_LINK");
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(this.getCanvas(), this.getCanvas().document, linkModel, this.getPanelType());
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		final LinkModel createdLink = new LinkModel();
		createdLink.setLabel(result.name());
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

//		confirmRenamingElement(0);
		this.getCanvas().findOrCreateLinkLayout(createdLink.getId());
		this.getCanvas().select(SelectedElement.forLink(createdLink.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.setConceptualName("New table");
		this.getCanvas().applyDefaultPaletteToClass(classModel);

		this.getDocument().getModel().getClasses().add(classModel);

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Point2D.Double center = this.getCanvas().mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, DiagramCanvas.CLASS_HEADER_HEIGHT));

//		confirmRenamingElement(0);
		this.getCanvas().select(SelectedElement.forClass(classModel.getId()));
		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
		this.getCanvas().invokeRenamingElement(RenamingElement.forClass(classModel.getId()));
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

	default void renameSelection(final boolean alternative) {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		switch (this.getCanvas().selectedElement.type()) {
		case CLASS, FIELD, COMMENT, LINK -> {
			this.getCanvas().invokeRenamingElement(this.getCanvas().selectedElement.asRenamingElement(alternative));
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + this.getCanvas().selectedElement);
		}
	}

}
