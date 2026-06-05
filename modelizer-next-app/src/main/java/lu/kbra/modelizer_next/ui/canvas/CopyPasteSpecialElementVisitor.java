package lu.kbra.modelizer_next.ui.canvas;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopyPasteSpecialData;

/**
 * Visitor used by the copy/cut/paste-special popup.
 */
public final class CopyPasteSpecialElementVisitor implements ElementVisitor {

	private final CopyPasteSpecialData data;
	private final StylePalette defaultPalette;
	private final Set<String> copiedClassIds = new HashSet<>();
	private final Set<String> copiedFieldIds = new HashSet<>();

	public CopyPasteSpecialElementVisitor(final CopyPasteSpecialData data, final StylePalette defaultPalette) {
		this.data = data;
		this.defaultPalette = defaultPalette;
	}

	@Override
	public ClipboardSnapshot visitClipboardSnapshot(final ClipboardSnapshot snapshot) {
		this.copiedClassIds.clear();
		this.copiedFieldIds.clear();

		for (final CopiedClass copiedClass : snapshot.classes()) {
			this.copiedClassIds.add(copiedClass.sourceId());
			for (final CopiedField copiedField : copiedClass.fields()) {
				this.copiedFieldIds.add(copiedField.sourceId());
			}
		}

		for (final CopiedField copiedField : snapshot.fields()) {
			this.copiedFieldIds.add(copiedField.sourceId());
		}

		return snapshot;
	}

	@Override
	public Optional<CopiedLink> visitCopiedLink(final CopiedLink copiedLink) {
		if (!this.data.keepLinks()) {
			return Optional.empty();
		}

		final boolean fromCopied = this.isEndpointCopied(copiedLink.fromClassId(), copiedLink.fromFieldId());
		final boolean toCopied = this.isEndpointCopied(copiedLink.toClassId(), copiedLink.toFieldId());
		final boolean internal = fromCopied && toCopied;

		if (internal && !this.data.keepInternalLinks()) {
			return Optional.empty();
		}
		if (!internal && !this.data.keepOutgoingLinks()) {
			return Optional.empty();
		}

		return Optional.of(copiedLink);
	}

	@Override
	public Optional<ClassModel> visitPastedClass(final ClassModel classModel) {
		this.applyNameOverwrite(classModel::getConceptualName, classModel::setConceptualName);
		this.applyNameOverwrite(classModel::getTechnicalName, classModel::setTechnicalName);

		if (this.data.withDefaultStyle() && this.defaultPalette != null) {
			classModel.setTextColor(this.defaultPalette.getClassTextColor());
			classModel.setBackgroundColor(this.defaultPalette.getClassBackgroundColor());
			classModel.setBorderColor(this.defaultPalette.getClassBorderColor());
			classModel.setLastPaletteName(this.defaultPalette.getName());
		}

		return Optional.of(classModel);
	}

	@Override
	public Optional<FieldModel> visitPastedField(final FieldModel fieldModel) {
		this.applyNameOverwrite(fieldModel::getConceptualName, fieldModel::setConceptualName);
		this.applyNameOverwrite(fieldModel::getTechnicalName, fieldModel::setTechnicalName);

		if (this.data.withDefaultStyle() && this.defaultPalette != null) {
			fieldModel.setTextColor(this.defaultPalette.getFieldTextColor());
			fieldModel.setBackgroundColor(this.defaultPalette.getFieldBackgroundColor());
			fieldModel.setLastPaletteName(this.defaultPalette.getName());
		}

		return Optional.of(fieldModel);
	}

	@Override
	public Optional<CommentModel> visitPastedComment(final CommentModel commentModel) {
		if (this.hasNameOverwrite()) {
			commentModel.setText(this.formatName(commentModel.getText()));
		}

		if (this.data.withDefaultStyle() && this.defaultPalette != null) {
			commentModel.setTextColor(this.defaultPalette.getCommentTextColor());
			commentModel.setBackgroundColor(this.defaultPalette.getCommentBackgroundColor());
			commentModel.setBorderColor(this.defaultPalette.getCommentBorderColor());
			commentModel.setLastPaletteName(this.defaultPalette.getName());
		}

		return Optional.of(commentModel);
	}

	@Override
	public Optional<LinkModel> visitPastedLink(final LinkModel linkModel) {
		if (this.hasNameOverwrite()) {
			linkModel.setLabel(this.formatName(linkModel.getLabel()));
		}

		if (this.data.withDefaultStyle() && this.defaultPalette != null) {
			linkModel.setLineColor(this.defaultPalette.getLinkColor());
			linkModel.setLastPaletteName(this.defaultPalette.getName());
		}

		return Optional.of(linkModel);
	}

	private boolean isEndpointCopied(final String classId, final String fieldId) {
		return this.copiedClassIds.contains(classId) || fieldId != null && this.copiedFieldIds.contains(fieldId);
	}

	private boolean hasNameOverwrite() {
		return this.data.nameOverwrite() != null && !this.data.nameOverwrite().isBlank();
	}

	private String formatName(final String original) {
		final String safeOriginal = original == null ? "" : original;
		if (!this.hasNameOverwrite()) {
			return safeOriginal;
		}
		return this.data.nameOverwrite().replace("%%", safeOriginal);
	}

	private void applyNameOverwrite(final Supplier<String> getter, final Consumer<String> setter) {
		if (this.hasNameOverwrite()) {
			setter.accept(this.formatName(getter.get()));
		}
	}

}
