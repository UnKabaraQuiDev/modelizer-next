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

interface DiagramModelEditor extends DiagramCanvasExt {

	default void addComment() {
		final CommentModel commentModel = new CommentModel();
		commentModel.setKind(CommentKind.STANDALONE);
		commentModel.setText("New comment");
		commentModel.setVisibility(getCanvas().panelType);
		getCanvas().applyDefaultPaletteToComment(commentModel);

		if (getCanvas().selectedElement != null && getCanvas().selectedElement.type() == SelectedType.COMMENT) {
			final CommentModel cm = getCanvas().findCommentById(getCanvas().selectedElement.commentId());
			final CommentBinding cb = cm.getBinding();
			if (cm.getKind() != CommentKind.STANDALONE) {
				commentModel.setKind(CommentKind.BOUND);
				commentModel.setBinding(new CommentBinding(cb.getTargetType(), cb.getTargetId()));
			}
		} else if (getCanvas().selectedElement != null && getCanvas().selectedElement.type() != SelectedType.COMMENT
				&& getCanvas().selectedElement.type() != SelectedType.NONE) {
			commentModel.setKind(CommentKind.BOUND);
			commentModel.setBinding(switch (getCanvas().selectedElement.type()) {
			case CLASS -> new CommentBinding(BoundTargetType.CLASS, getCanvas().selectedElement.classId());
			case LINK -> new CommentBinding(BoundTargetType.LINK, getCanvas().selectedElement.linkId());
			case FIELD -> new CommentBinding(BoundTargetType.CLASS, getCanvas().selectedElement.classId());
			default -> throw new IllegalStateException("Cannot bind comment to: " + getCanvas().selectedElement);
			});
		}

		getCanvas().document.getModel().getComments().add(commentModel);

		final NodeLayout layout = getCanvas()
				.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
		final Point2D.Double center = getCanvas().mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 30));
		layout.setSize(new Size2D(220, 80));

		getCanvas().select(SelectedElement.forComment(commentModel.getId()));
		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void addField() {
		final ClassModel targetClass;

		if (getCanvas().selectedElement != null && getCanvas().selectedElement.type() == SelectedType.CLASS
				|| getCanvas().selectedElement != null && getCanvas().selectedElement.type() == SelectedType.FIELD) {
			targetClass = getCanvas().findClassById(getCanvas().selectedElement.classId());
		} else {
			return;
		}

		if (targetClass == null) {
			return;
		}

		final FieldModel fieldModel = new FieldModel();
		fieldModel.getNames().setConceptualName("New field");
		getCanvas().applyDefaultPaletteToField(fieldModel);
		targetClass.getFields().add(fieldModel);

		getCanvas().select(SelectedElement.forField(targetClass.getId(), fieldModel.getId()));
		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void addLink() {
		final LinkModel linkModel = new LinkModel();
		if (getCanvas().selectedElement != null && getCanvas().selectedElement.type() == SelectedType.CLASS) {
			linkModel.setFrom(new LinkEnd(getCanvas().selectedElement.classId(), null));
		} else {
			linkModel.setFrom(new LinkEnd(null, null));
		}
		linkModel.setTo(new LinkEnd(null, null));

		if (getCanvas().panelType == PanelType.CONCEPTUAL) {
			linkModel.setName("new relation");
			linkModel.setCardinalityFrom(Cardinality.ONE);
			linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		} else {
			linkModel.setName("NEW_LINK");
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(getCanvas(), getCanvas().document, linkModel, getCanvas().panelType);
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		final LinkModel createdLink = new LinkModel();
		createdLink.setName(result.name());
		createdLink.setLineColor(result.lineColor());
		createdLink.setAssociationClassId(result.associationClassId());
		createdLink.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		createdLink.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));

		if (getCanvas().panelType == PanelType.CONCEPTUAL) {
			createdLink.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			createdLink.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
			getCanvas().document.getModel().getConceptualLinks().add(createdLink);
		} else {
			createdLink.setCardinalityFrom(null);
			createdLink.setCardinalityTo(null);
			getCanvas().document.getModel().getTechnicalLinks().add(createdLink);
		}
		getCanvas().applyDefaultPaletteToLink(createdLink);

		getCanvas().findOrCreateLinkLayout(createdLink.getId());
		getCanvas().select(SelectedElement.forLink(createdLink.getId()));
		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void addTable() {
		final ClassModel classModel = new ClassModel();
		classModel.getNames().setConceptualName("New table");
		getCanvas().applyDefaultPaletteToClass(classModel);

		getCanvas().document.getModel().getClasses().add(classModel);

		final NodeLayout layout = getCanvas()
				.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
		final Point2D.Double center = getCanvas().mouseWorldOrViewportCenter();
		layout.setPosition(new Point2D.Double(center.getX() - 100, center.getY() - 40));
		layout.setSize(new Size2D(180, 0));

		getCanvas().select(SelectedElement.forClass(classModel.getId()));
		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void deleteSelection() {
		if (getCanvas().selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(getCanvas().selectedElements);

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case LINK -> getCanvas().deleteLink(element.linkId());
			case COMMENT -> getCanvas().deleteComment(element.commentId());
			case FIELD -> getCanvas().deleteField(element.classId(), element.fieldId());
			case CLASS -> getCanvas().deleteClass(element.classId());
			default -> {
			}
			}
		}

		getCanvas().clearSelection();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void editSelected() {
		if (getCanvas().selectedElement == null || getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}
		switch (getCanvas().selectedElement.type()) {
		case CLASS -> getCanvas().editClass(getCanvas().selectedElement.classId());
		case FIELD -> getCanvas().editField(getCanvas().selectedElement.classId(), getCanvas().selectedElement.fieldId());
		case COMMENT -> getCanvas().editComment(getCanvas().selectedElement.commentId());
		case LINK -> getCanvas().editLink(getCanvas().selectedElement.linkId());
		}
	}

	default void moveFieldSelection(final int delta) {
		if (getCanvas().selectedElement != null && getCanvas().selectedElement.type() == SelectedType.CLASS) {
			final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
			if (classModel.getFields().isEmpty()) {
				return;
			}
			getCanvas().select(SelectedElement.forField(getCanvas().selectedElement.classId(), classModel.getFields().get(0).getId()));
			return;
		}

		if (getCanvas().selectedElement == null || getCanvas().selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
		if (classModel == null) {
			return;
		}

		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
		int currentIndex = -1;
		for (int i = 0; i < visibleFields.size(); i++) {
			if (Objects.equals(visibleFields.get(i).getId(), getCanvas().selectedElement.fieldId())) {
				currentIndex = i;
				break;
			}
		}

		if (currentIndex == 0 && delta == -1) {
			getCanvas().select(SelectedElement.forClass(classModel.getId()));
			return;
		}

		if (currentIndex < 0) {
			return;
		}

		final int newIndex = currentIndex + delta;
		if (newIndex < 0 || newIndex >= visibleFields.size()) {
			return;
		}

		getCanvas().select(SelectedElement.forField(classModel.getId(), visibleFields.get(newIndex).getId()));
	}

	default void moveSelectedFieldInList(final int delta) {
		if (getCanvas().selectedElement == null || getCanvas().selectedElement.type() != SelectedType.FIELD) {
			return;
		}

		final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
		if (classModel == null) {
			return;
		}

		int currentIndex = -1;
		for (int i = 0; i < classModel.getFields().size(); i++) {
			if (Objects.equals(classModel.getFields().get(i).getId(), getCanvas().selectedElement.fieldId())) {
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

		getCanvas().select(SelectedElement.forField(classModel.getId(), moved.getId()));
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void renameSelection() {
		if (getCanvas().selectedElement == null || getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		final String title;
		final String currentValue;

		switch (getCanvas().selectedElement.type()) {
		case CLASS -> {
			final ClassModel classModel = getCanvas().findClassById(getCanvas().selectedElement.classId());
			if (classModel == null) {
				return;
			}
			title = "Rename class";
			currentValue = getCanvas().getEditableClassName(classModel);
		}
		case FIELD -> {
			final FieldModel fieldModel = getCanvas().findFieldById(getCanvas().selectedElement.classId(),
					getCanvas().selectedElement.fieldId());
			if (fieldModel == null) {
				return;
			}
			title = "Rename field";
			currentValue = getCanvas().getEditableFieldName(fieldModel);
		}
		case COMMENT -> {
			title = "Rename comment";
			currentValue = getCanvas().getEditableCommentText(getCanvas().selectedElement.commentId());
		}
		case LINK -> {
			if (getCanvas().panelType != PanelType.CONCEPTUAL) {
				return;
			}
			final LinkModel linkModel = getCanvas().findLinkById(getCanvas().selectedElement.linkId());
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

		final String newValue = RenameDialog.showDialog(getCanvas(), title, currentValue);
		if (newValue == null) {
			return;
		}

		switch (getCanvas().selectedElement.type()) {
		case CLASS -> getCanvas().setEditableClassName(getCanvas().findClassById(getCanvas().selectedElement.classId()), newValue);
		case FIELD -> getCanvas().setEditableFieldName(
				getCanvas().findFieldById(getCanvas().selectedElement.classId(), getCanvas().selectedElement.fieldId()),
				newValue);
		case COMMENT -> getCanvas().setEditableCommentText(getCanvas().selectedElement.commentId(), newValue);
		case LINK -> getCanvas().findLinkById(getCanvas().selectedElement.linkId()).setName(newValue);
		default -> {
		}
		}

		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	DiagramCanvas getCanvas();

}
