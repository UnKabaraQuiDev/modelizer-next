package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.util.Map;

import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.HitResult;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

/**
 * Contains creation actions for tables, fields, comments, and links.
 */
public interface ElementCreator extends DiagramCanvasExt {

	default void createConceptualLink(final String fromClassId, final String toClassId) {
		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(fromClassId, null));
		linkModel.setTo(new LinkEnd(toClassId, null));
		linkModel.setCardinalityFrom(Cardinality.ONE);
		linkModel.setCardinalityTo(Cardinality.ZERO_OR_MANY);
		this.getCanvas().applyDefaultPaletteToLink(linkModel);
		this.getCanvas().document.getModel().getConceptualLinks().add(linkModel);

		this.getCanvas().findOrCreateLinkLayout(linkModel.getId());
		this.getCanvas().select(SelectedElement.forLink(linkModel.getId()));
		this.getCanvas().notifyDocumentChanged();
	}

	default FieldModel createFieldFromClipboard(final CopiedField copiedField, final boolean rename) {
		final FieldModel fieldCopy = new FieldModel();

		fieldCopy.setConceptualName(rename ? this.getCanvas().appendSuffix(copiedField.name(), " Copy") : copiedField.name());
		fieldCopy.getNames()
				.setTechnicalName(
						rename ? this.getCanvas().appendSuffix(copiedField.technicalName(), "_COPY") : copiedField.technicalName());

		fieldCopy.setNotConceptual(copiedField.notConceptual());
		fieldCopy.setPrimaryKey(copiedField.primaryKey());
		fieldCopy.setUnique(copiedField.unique());
		fieldCopy.setNotNull(copiedField.notNull());
		fieldCopy.setType(copiedField.type());

		fieldCopy.setTextColor(copiedField.textColor());
		fieldCopy.setBackgroundColor(copiedField.backgroundColor());

		return fieldCopy;
	}

	default LinkModel createLinkFromClipboard(
			final CopiedLink copiedLink,
			final Map<String, String> classIdMap,
			final Map<String, String> fieldIdMap) {

		final String fromClassId = this.getCanvas().mapId(classIdMap, copiedLink.fromClassId());
		final String fromFieldId = this.getCanvas().mapId(fieldIdMap, copiedLink.fromFieldId());
		final String toClassId = this.getCanvas().mapId(classIdMap, copiedLink.toClassId());
		final String toFieldId = this.getCanvas().mapId(fieldIdMap, copiedLink.toFieldId());

		if (!this.getCanvas().linkEndpointExists(fromClassId, fromFieldId) || !this.getCanvas().linkEndpointExists(toClassId, toFieldId)) {
			return null;
		}

		String associationClassId = this.getCanvas().mapId(classIdMap, copiedLink.associationClassId());
		if (associationClassId != null && this.getCanvas().findClassById(associationClassId) == null) {
			associationClassId = null;
		}

		final LinkModel linkCopy = new LinkModel();

		linkCopy.setName(copiedLink.name());
		linkCopy.setLineColor(copiedLink.lineColor());
		linkCopy.setAssociationClassId(associationClassId);
		linkCopy.setFrom(new LinkEnd(fromClassId, fromFieldId));
		linkCopy.setTo(new LinkEnd(toClassId, toFieldId));
		linkCopy.setCardinalityFrom(copiedLink.cardinalityFrom());
		linkCopy.setCardinalityTo(copiedLink.cardinalityTo());
		linkCopy.setLabelFrom(copiedLink.labelFrom());
		linkCopy.setLabelTo(copiedLink.labelTo());

		return linkCopy;
	}

	default CommentBinding createRemappedCommentBinding(
			final CopiedComment copiedComment,
			final Map<String, String> classIdMap,
			final Map<String, String> linkIdMap) {

		if (copiedComment.bindingTargetType() == null || copiedComment.bindingTargetId() == null) {
			return null;
		}

		final String targetId = switch (copiedComment.bindingTargetType()) {
		case CLASS -> this.getCanvas().mapId(classIdMap, copiedComment.bindingTargetId());
		case LINK -> this.getCanvas().mapId(linkIdMap, copiedComment.bindingTargetId());
		};

		if (copiedComment.bindingTargetType() == BoundTargetType.CLASS && this.getCanvas().findClassById(targetId) == null
				|| copiedComment.bindingTargetType() == BoundTargetType.LINK && this.getCanvas().findLinkById(targetId) == null) {
			return null;
		}

		return new CommentBinding(copiedComment.bindingTargetType(), targetId);
	}

	default void createTechnicalLink(final SelectedElement fromEndpoint, final SelectedElement toEndpoint) {
		final LinkModel linkModel = new LinkModel();
		linkModel.setFrom(new LinkEnd(fromEndpoint.classId(), fromEndpoint.fieldId()));
		linkModel.setTo(new LinkEnd(toEndpoint.classId(), toEndpoint.fieldId()));
		linkModel.setCardinalityFrom(null);
		linkModel.setCardinalityTo(null);
		this.getCanvas().applyDefaultPaletteToLink(linkModel);
		this.getCanvas().document.getModel().getTechnicalLinks().add(linkModel);

		this.getCanvas().findOrCreateLinkLayout(linkModel.getId());
		this.getCanvas().select(SelectedElement.forLink(linkModel.getId()));
		this.getCanvas().notifyDocumentChanged();
	}

	default void finishLinkCreation(final Point2D.Double worldPoint) {
		if (this.getCanvas().linkCreationState == null) {
			return;
		}

		final SelectedElement source = this.getCanvas().getLinkCreationSource();
		if (source == null) {
			return;
		}

		SelectedElement target = this.getCanvas().linkPreviewTarget;
		if (target == null) {
			final HitResult hitResult = this.getCanvas().findTopmostHit(worldPoint);
			target = hitResult == null ? null : this.getCanvas().normalizeConnectionTargetSelection(hitResult.selection());
		}

		if (!this.getCanvas().isValidPreviewTarget(target)) {
			return;
		}

		if (source.type() == SelectedType.COMMENT) {
			this.getCanvas().bindCommentToTarget(source.commentId(), target);
			return;
		}

		if (source.type() == SelectedType.CLASS && target.type() == SelectedType.LINK) {
			this.getCanvas().setAssociationClassForLink(source.classId(), target.linkId());
			return;
		}

		if (this.getPanelType() == PanelType.CONCEPTUAL) {
			this.getCanvas().createConceptualLink(source.classId(), target.classId());
			return;
		}

		final SelectedElement fromEndpoint = this.getCanvas().resolveTechnicalSourceEndpoint(source, target);
		final SelectedElement toEndpoint = this.getCanvas().resolveTechnicalTargetEndpoint(target);
		if (fromEndpoint == null || toEndpoint == null) {
			return;
		}

		this.getCanvas().createTechnicalLink(fromEndpoint, toEndpoint);
	}

}
