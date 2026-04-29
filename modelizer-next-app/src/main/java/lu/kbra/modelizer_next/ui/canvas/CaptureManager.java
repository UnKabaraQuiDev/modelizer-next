package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLinkLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedNodeLayout;

public interface CaptureManager extends DiagramCanvasExt {

	default CopiedClass captureClass(final ClassModel classModel) {
		final List<CopiedField> fields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			fields.add(getCanvas().captureField(classModel.getId(), fieldModel));
		}

		return new CopiedClass(classModel.getId(),
				classModel.getNames().getConceptualName(),
				classModel.getNames().getTechnicalName(),
				classModel.getVisibility().isConceptual(),
				classModel.getVisibility().isLogical(),
				classModel.getVisibility().isPhysical(),
				classModel.getStyle().getTextColor(),
				classModel.getStyle().getBackgroundColor(),
				classModel.getStyle().getBorderColor(),
				List.copyOf(fields),
				getCanvas().captureNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
	}

	default CopiedComment captureComment(final CommentModel commentModel) {
		final CommentBinding binding = commentModel.getBinding();

		return new CopiedComment(commentModel.getId(),
				commentModel.getKind(),
				commentModel.getText(),
				commentModel.getTextColor(),
				commentModel.getBackgroundColor(),
				commentModel.getBorderColor(),
				commentModel.getVisibility().isConceptual(),
				commentModel.getVisibility().isLogical(),
				commentModel.getVisibility().isPhysical(),
				binding == null ? null : binding.getTargetType(),
				binding == null ? null : binding.getTargetId(),
				getCanvas().captureNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
	}

	default CopiedField captureField(final String ownerClassId, final FieldModel fieldModel) {
		return new CopiedField(ownerClassId,
				fieldModel.getId(),
				fieldModel.getNames().getConceptualName(),
				fieldModel.getNames().getTechnicalName(),
				fieldModel.isNotConceptual(),
				fieldModel.isPrimaryKey(),
				fieldModel.isUnique(),
				fieldModel.isNotNull(),
				fieldModel.getType(),
				fieldModel.getStyle().getTextColor(),
				fieldModel.getStyle().getBackgroundColor());
	}

	default CopiedLink captureLink(final LinkModel linkModel) {
		final LinkEnd from = linkModel.getFrom();
		final LinkEnd to = linkModel.getTo();

		return new CopiedLink(linkModel.getId(),
				linkModel.getName(),
				linkModel.getLineColor(),
				linkModel.getAssociationClassId(),
				from == null ? null : from.getClassId(),
				from == null ? null : from.getFieldId(),
				to == null ? null : to.getClassId(),
				to == null ? null : to.getFieldId(),
				linkModel.getCardinalityFrom(),
				linkModel
						.getCardinalityTo(),
				linkModel.getLabelFrom(),
				linkModel.getLabelTo(),
				getCanvas().captureLinkLayout(linkModel.getId()));
	}

	default CopiedLinkLayout captureLinkLayout(final String linkId) {
		final LinkLayout linkLayout = getCanvas().findOrCreateLinkLayout(linkId);
		final List<Point2D.Double> bendPoints = new ArrayList<>();

		for (final Point2D.Double bendPoint : linkLayout.getBendPoints()) {
			bendPoints.add(new Point2D.Double(bendPoint.getX(), bendPoint.getY()));
		}

		final Point2D.Double labelPosition = linkLayout.getNameLabelPosition() == null ? null
				: new Point2D.Double(linkLayout.getNameLabelPosition().getX(), linkLayout.getNameLabelPosition().getY());

		return new CopiedLinkLayout(List.copyOf(bendPoints), labelPosition);
	}

	default CopiedNodeLayout captureNodeLayout(final LayoutObjectType type, final String objectId) {
		final NodeLayout layout = getCanvas().findOrCreateNodeLayout(type, objectId);

		return new CopiedNodeLayout(layout.getPosition().getX(),
				layout.getPosition().getY(),
				layout.getSize().getWidth(),
				layout.getSize().getHeight());
	}

}
