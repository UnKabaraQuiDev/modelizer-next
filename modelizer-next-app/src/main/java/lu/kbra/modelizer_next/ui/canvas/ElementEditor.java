package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.dialogs.ClassEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.CommentEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.FieldEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;

public interface ElementEditor extends DiagramCanvasExt {

	default void editClass(final String classId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		final ClassEditorDialog.Result result = ClassEditorDialog.showDialog(getCanvas(), classModel);
		if (result == null) {
			return;
		}

		classModel.getNames().setConceptualName(result.conceptualName());
		classModel.getNames().setTechnicalName(result.technicalName());
		classModel.getStyle().setTextColor(result.textColor());
		classModel.getStyle().setBackgroundColor(result.backgroundColor());
		classModel.getStyle().setBorderColor(result.borderColor());
		classModel.getVisibility().set(result.visibleInConceptual(), result.visibleInLogical(), result.visibleInPhysical());

		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void editComment(final String commentId) {
		final CommentModel commentModel = getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		final CommentEditorDialog.Result result = CommentEditorDialog
				.showDialog(getCanvas(), getCanvas().document, commentModel, getPanelType());
		if (result == null) {
			return;
		}

		commentModel.setText(result.text());
		commentModel.setTextColor(result.textColor());
		commentModel.setBackgroundColor(result.backgroundColor());
		commentModel.setBorderColor(result.borderColor());
		commentModel.setKind(result.kind());
		commentModel.setBinding(result.binding());
		commentModel.getVisibility().set(result.visibleInConceptual(), result.visibleInLogical(), result.visibleInPhysical());

		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void editField(final String classId, final String fieldId) {
		final FieldModel fieldModel = getCanvas().findFieldById(classId, fieldId);
		if (fieldModel == null) {
			return;
		}

		final FieldEditorDialog.Result result = FieldEditorDialog.showDialog(getCanvas(), fieldModel, getCanvas()::moveSelectedFieldInList);
		if (result == null) {
			return;
		}

		fieldModel.getNames().setConceptualName(result.name());
		fieldModel.getNames().setTechnicalName(result.technicalName());
		fieldModel.setPrimaryKey(result.primaryKey());
		fieldModel.setUnique(result.unique());
		fieldModel.setNotNull(result.notNull());
		fieldModel.getStyle().setTextColor(result.textColor());
		fieldModel.getStyle().setBackgroundColor(result.backgroundColor());
		fieldModel.setType(result.type());

		getCanvas().notifySelectionChanged();
		getCanvas().notifyDocumentChanged();
		getCanvas().repaint();
	}

	default void editLink(final String linkId) {
		final LinkModel linkModel = getCanvas().findLinkById(linkId);
		if (linkModel == null) {
			return;
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(getCanvas(), getCanvas().document, linkModel, getPanelType());
		if (result == null || result.fromClassId() == null || result.toClassId() == null) {
			return;
		}

		linkModel.setName(result.name());
		linkModel.setLineColor(result.lineColor());
		linkModel.setFrom(new LinkEnd(result.fromClassId(), result.fromFieldId()));
		linkModel.setTo(new LinkEnd(result.toClassId(), result.toFieldId()));
		linkModel.setAssociationClassId(result.associationClassId());
		linkModel.setLabelFrom(result.labelFrom());
		linkModel.setLabelTo(result.labelTo());

		if (getPanelType() == PanelType.CONCEPTUAL) {
			linkModel.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			linkModel.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
		} else {
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		getCanvas().notifySelectionChanged();
		getCanvas().repaint();
	}

	default void bindCommentToTarget(final String commentId, final SelectedElement target) {
		final CommentModel commentModel = getCanvas().findCommentById(commentId);
		if (commentModel == null || target == null) {
			return;
		}

		commentModel.setKind(CommentKind.BOUND);
		commentModel.setBinding(switch (target.type()) {
		case CLASS, FIELD -> new CommentBinding(BoundTargetType.CLASS, target.classId());
		case LINK -> new CommentBinding(BoundTargetType.LINK, target.linkId());
		default -> throw new IllegalStateException("Cannot bind comment to: " + target);
		});

		getCanvas().select(SelectedElement.forComment(commentId));
		getCanvas().notifyDocumentChanged();
	}

}
