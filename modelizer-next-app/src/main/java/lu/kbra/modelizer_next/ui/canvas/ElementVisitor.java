package lu.kbra.modelizer_next.ui.canvas;

import java.util.Optional;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;

/**
 * Hook used by copy/paste operations to alter or reject elements.
 */
public interface ElementVisitor {

	/**
	 * Visits the whole snapshot before the per-element hooks run.
	 *
	 * @param snapshot copied snapshot value
	 * @return snapshot to continue with
	 */
	default ClipboardSnapshot visitClipboardSnapshot(final ClipboardSnapshot snapshot) {
		return snapshot;
	}

	default Optional<CopiedClass> visitCopiedClass(final CopiedClass copiedClass) {
		return Optional.of(copiedClass);
	}

	default Optional<CopiedField> visitCopiedField(final CopiedField copiedField) {
		return Optional.of(copiedField);
	}

	default Optional<CopiedComment> visitCopiedComment(final CopiedComment copiedComment) {
		return Optional.of(copiedComment);
	}

	default Optional<CopiedLink> visitCopiedLink(final CopiedLink copiedLink) {
		return Optional.of(copiedLink);
	}

	default Optional<ClassModel> visitPastedClass(final ClassModel classModel) {
		return Optional.of(classModel);
	}

	default Optional<FieldModel> visitPastedField(final FieldModel fieldModel) {
		return Optional.of(fieldModel);
	}

	default Optional<CommentModel> visitPastedComment(final CommentModel commentModel) {
		return Optional.of(commentModel);
	}

	default Optional<LinkModel> visitPastedLink(final LinkModel linkModel) {
		return Optional.of(linkModel);
	}

}
