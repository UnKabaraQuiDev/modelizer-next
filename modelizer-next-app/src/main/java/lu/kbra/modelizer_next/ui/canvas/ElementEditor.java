package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.dialogs.ClassEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.CommentEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.FieldEditorDialog;
import lu.kbra.modelizer_next.ui.dialogs.LinkEditorDialog;

/**
 * Contains edit dialogs and update helpers for canvas elements.
 */
public interface ElementEditor extends DiagramCanvasExt {

	default void bindCommentToTarget(final String commentId, final SelectedElement target) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null || target == null) {
			return;
		}

		commentModel.setKind(CommentKind.BOUND);
		commentModel.setBinding(switch (target.type()) {
		case CLASS, FIELD -> new CommentBinding(BoundTargetType.CLASS, target.classId());
		case LINK -> new CommentBinding(BoundTargetType.LINK, target.linkId());
		default -> throw new IllegalStateException("Cannot bind comment to: " + target);
		});

		this.getCanvas().select(SelectedElement.forComment(commentId));
		this.getCanvas().notifyDocumentChanged();
	}

	default void editClass(final String classId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		final ClassEditorDialog.Result result = ClassEditorDialog.showDialog(this.getCanvas(), classModel);
		if (result == null) {
			return;
		}

		classModel.setConceptualName(result.conceptualName());
		classModel.setTechnicalName(result.technicalName());
		classModel.setTextColor(result.textColor());
		classModel.setBackgroundColor(result.backgroundColor());
		classModel.setBorderColor(result.borderColor());
		classModel.setVisibility(result.visibleInConceptual(), result.visibleInLogical(), result.visibleInPhysical());

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void editComment(final String commentId) {
		final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
		if (commentModel == null) {
			return;
		}

		final CommentEditorDialog.Result result = CommentEditorDialog
				.showDialog(this.getCanvas(), this.getCanvas().document, commentModel, this.getPanelType());
		if (result == null) {
			return;
		}

		commentModel.setText(result.text());
		commentModel.setTextColor(result.textColor());
		commentModel.setBackgroundColor(result.backgroundColor());
		commentModel.setBorderColor(result.borderColor());
		commentModel.setKind(result.kind());
		commentModel.setBinding(result.binding());
		commentModel.setVisibility(result.visibleInConceptual(), result.visibleInLogical(), result.visibleInPhysical());

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void editField(final String classId, final String fieldId) {
		final FieldModel fieldModel = this.getCanvas().findFieldById(classId, fieldId);
		if (fieldModel == null) {
			return;
		}

		final FieldEditorDialog.Result result = FieldEditorDialog
				.showDialog(this.getCanvas(), fieldModel, this.getCanvas()::moveSelectedFieldInList);
		if (result == null) {
			return;
		}

		fieldModel.setConceptualName(result.name());
		fieldModel.setTechnicalName(result.technicalName());
		fieldModel.setPrimaryKey(result.primaryKey());
		fieldModel.setUnique(result.unique());
		fieldModel.setNotNull(result.notNull());
		fieldModel.setTextColor(result.textColor());
		fieldModel.setBackgroundColor(result.backgroundColor());
		fieldModel.setType(result.type());

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void editLink(final String linkId) {
		final LinkModel linkModel = this.getCanvas().findLinkById(linkId);
		if (linkModel == null) {
			return;
		}

		final LinkEditorDialog.Result result = LinkEditorDialog
				.showDialog(this.getCanvas(), this.getCanvas().document, linkModel, this.getPanelType());
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

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			linkModel.setCardinalityFrom(result.cardinalityFrom() == null ? Cardinality.ONE : result.cardinalityFrom());
			linkModel.setCardinalityTo(result.cardinalityTo() == null ? Cardinality.ZERO_OR_MANY : result.cardinalityTo());
		} else {
			linkModel.setCardinalityFrom(null);
			linkModel.setCardinalityTo(null);
		}

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().repaint();
	}

}
