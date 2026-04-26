package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
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

final class DiagramModelEditor {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	DiagramModelEditor(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setModelEditor(this);
	}

	void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");
		commentModel.setVisibility(this.canvas.panelType);
		this.canvas.applyDefaultPaletteToComment(commentModel);

		if (this.canvas.selectedElement != null && this.canvas.selectedElement.type() == SelectedType.COMMENT) {
			final CommentModel cm = this.canvas.findCommentById(this.canvas.selectedElement.commentId());
			final CommentBinding cb = cm.getBinding();
			if (cm.getKind() != CommentKind.STANDALONE) {
				commentModel.setKind(CommentKind.BOUND);
				commentModel.setBinding(new CommentBinding(cb.getTargetType(), cb.getTargetId()));
			}
		} else if (this.canvas.selectedElement != null && this.canvas.selectedElement.type() != SelectedType.COMMENT
				&& this.canvas.selectedElement.type() != SelectedType.NONE) {
			commentModel.setKind(CommentKind.BOUND);
			commentModel.setBinding(switch (this.canvas.selectedElement.type()) {
			case CLASS -> new CommentBinding(BoundTargetType.CLASS, this.canvas.selectedElement.classId());
			case LINK -> new CommentBinding(BoundTargetType.LINK, this.canvas.selectedElement.linkId());
			case FIELD -> new CommentBinding(BoundTargetType.CLASS, this.canvas.selectedElement.classId());
			default -> throw new IllegalStateException("Cannot bind comment to: " + this.canvas.selectedElement);
			});
		}

		this.canvas.document.getModel().getComments().add(commentModel);

		final NodeLayout layout = this.canvas
				.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Point2D.Double center = this.canvas.mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2D(220, 80));

		this.canvas.select(SelectedElement.forComment(commentModel.getId()));
		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void addField() {
		final ClassModel targetClass;

		if (this.canvas.selectedElement != null && this.canvas.selectedElement.type() == SelectedType.CLASS
				|| this.canvas.selectedElement != null && this.canvas.selectedElement.type() == SelectedType.FIELD) {
			targetClass = this.canvas.findClassById(this.canvas.selectedElement.classId());
		} else {
			return;
		}

		if (targetClass == null) {
			return;
		}

		final FieldModel fieldModel = new FieldModel();
		fieldModel.getNames().setConceptualName("New field");
		this.canvas.applyDefaultPaletteToField(fieldModel);
		targetClass.getFields().add(fieldModel);

		this.canvas.select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void addLink() {
		final LinkModel linkModel = new LinkModel();
		if (this.canvas.selectedElement != null && this.canvas.selectedElement.type() == SelectedType.CLASS) {
			linkModel.setFrom(new LinkEnd(this.canvas.selectedElement.classId(), null));
		} else {
			linkModel.setFrom(new LinkEnd(null, null));
		}
		linkModel.setTo(new LinkEnd(null, null));

		if (this.canvas.panelType == PanelType.CONCEPTUAL) {
			linkModel.setName("new relation");
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		} else {
			linkModel.setName("NEW_LINK");
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(this.canvas, this.canvas.document, linkModel, this.canvas.panelType);
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		final LinkModel createdLink = new LinkModel();
		createdLink.setName(result.name());
		createdLink.setLineColor(result.lineColor());
		createdLink.setAssociationClassId(result.associationClassId());
		createdLink.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		createdLink.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));

		if (this.canvas.panelType == PanelType.CONCEPTUAL) {
			createdLink.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			createdLink.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
			this.canvas.document.getModel().getConceptualLinks().add(createdLink);
		} else {
			createdLink.setCardinalityFrom(null);
			createdLink.setCardinalityTo(null);
			this.canvas.document.getModel().getTechnicalLinks().add(createdLink);
		}
		this.canvas.applyDefaultPaletteToLink(createdLink);

		this.canvas.findOrCreateLinkLayout(createdLink.getId());
		this.canvas.select(SelectedElement.forLink(createdLink.getId()));
		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		this.canvas.applyDefaultPaletteToClass(classModel);

		this.canvas.document.getModel().getClasses().add(classModel);

		final NodeLayout layout = this.canvas
				.resolveRenderLayout(this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Point2D.Double center = this.canvas.mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, 0));

		this.canvas.select(SelectedElement.forClass(classModel.getId()));
		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void deleteSelection() {
		if (this.canvas.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.canvas.selectedElements);

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case LINK -> this.canvas.deleteLink(element.linkId());
			case COMMENT -> this.canvas.deleteComment(element.commentId());
			case FIELD -> this.canvas.deleteField(element.classId(), element.fieldId());
			case CLASS -> this.canvas.deleteClass(element.classId());
			default -> {
			}
			}
		}

		this.canvas.clearSelection();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void editSelected() {
		if (this.canvas.selectedElement == null || this.canvas.selectedElement.type() == SelectedType.NONE) {
			return;
		}
		switch (this.canvas.selectedElement.type()) {
		case CLASS -> this.canvas.editClass(this.canvas.selectedElement.classId());
		case FIELD -> this.canvas.editField(this.canvas.selectedElement.classId(), this.canvas.selectedElement.fieldId());
		case COMMENT -> this.canvas.editComment(this.canvas.selectedElement.commentId());
		case LINK -> this.canvas.editLink(this.canvas.selectedElement.linkId());
		}
	}

	void moveFieldSelection(final int delta) {
		if (this.canvas.selectedElement != null && this.canvas.selectedElement.type() == SelectedType.CLASS) {
			final ClassModel classModel = this.canvas.findClassById(this.canvas.selectedElement.classId());
			if (classModel.getFields().isEmpty()) {
				return;
			}
			this.canvas.select(SelectedElement.forField(this.canvas.selectedElement.classId(), classModel.getFields().get(0).getId()));
			return;
		}

		if (this.canvas.selectedElement == null || this.canvas.selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.canvas.findClassById(this.canvas.selectedElement.classId());
		if (classModel == null) {
			return;
		}

		final List<FieldModel> visibleFields = this.canvas.getVisibleFields(classModel);
		int currentIndex = -1;
		for (int i = 0; i < visibleFields.size(); i++) {
			if (Objects.equals(visibleFields.get(i).getId(), this.canvas.selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex == 0 && delta == -1) {
			this.canvas.select(SelectedElement.forClass(classModel.getId()));
			return;
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= visibleFields.size()) {
			return;
		}

		this.canvas.select(SelectedElement.forField(classModel.getId(), visibleFields.get(newIndex).getId()));
	}

	void moveSelectedFieldInList(final int delta) {
		if (this.canvas.selectedElement == null || this.canvas.selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = this.canvas.findClassById(this.canvas.selectedElement.classId());
		if (classModel == null) {
			return;
		}

		int currentIndex = -1;
		for (int i = 0; i < classModel.getFields().size(); i++) {
			if (Objects.equals(classModel.getFields().get(i).getId(), this.canvas.selectedElement.fieldId())) {
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

		this.canvas.select(SelectedElement.forField(classModel.getId(), moved.getId()));
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void renameSelection() {
		if (this.canvas.selectedElement == null || this.canvas.selectedElement.type() == SelectedType.NONE) {
			return;
		}

		final String title;
		final String currentValue;

		switch (this.canvas.selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = this.canvas.findClassById(this.canvas.selectedElement.classId());
			if (classModel == null) {
				return;
			}
			title = "Rename class";
			currentValue = this.canvas.getEditableClassName(classModel);
		}
		case FIELD -> {
			final FieldModel fieldModel = this.canvas.findFieldById(this.canvas.selectedElement.classId(),
					this.canvas.selectedElement.fieldId());
			if (fieldModel == null) {
				return;
			}
			title = "Rename field";
			currentValue = this.canvas.getEditableFieldName(fieldModel);
		}
		case COMMENT -> {
			title = "Rename comment";
			currentValue = this.canvas.getEditableCommentText(this.canvas.selectedElement.commentId());
		}
		case LINK -> {
			if (this.canvas.panelType != PanelType.CONCEPTUAL) {
				return;
			}
			final LinkModel linkModel = this.canvas.findLinkById(this.canvas.selectedElement.linkId());
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

		final String newValue = RenameDialog.showDialog(this.canvas, title, currentValue);
		if (newValue == null) {
			return;
		}

		switch (this.canvas.selectedElement.type()) {
		case CLASS -> this.canvas.setEditableClassName(this.canvas.findClassById(this.canvas.selectedElement.classId()), newValue);
		case FIELD -> this.canvas.setEditableFieldName(
				this.canvas.findFieldById(this.canvas.selectedElement.classId(), this.canvas.selectedElement.fieldId()),
				newValue);
		case COMMENT -> this.canvas.setEditableCommentText(this.canvas.selectedElement.commentId(), newValue);
		case LINK -> this.canvas.findLinkById(this.canvas.selectedElement.linkId()).setName(newValue);
		default -> {
		}
		}

		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}
}
