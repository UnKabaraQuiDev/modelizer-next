package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;

/**
 * Contains deletion actions for the current canvas selection.
 */
public interface ElementDeleter extends DiagramCanvasExt {

	default void deleteClass(final String classId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		this.getCanvas().document.getModel().getClasses().remove(classModel);
		this.getCanvas()
				.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.CLASS && layout.getObjectId().equals(classId));

		this.getCanvas()
				.getActiveLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.getCanvas().document.getModel()
				.getConceptualLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));
		this.getCanvas().document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		this.getCanvas()
				.getPanelState()
				.getLinkLayouts()
				.removeIf(linkLayout -> this.getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	default void deleteComment(final String commentId) {
		this.getCanvas().document.getModel().getComments().removeIf(comment -> comment.getId().equals(commentId));
		this.getCanvas()
				.getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	default void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		classModel.getFields().removeIf(field -> field.getId().equals(fieldId));
		this.getCanvas().document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()));
		this.getCanvas()
				.getPanelState()
				.getLinkLayouts()
				.removeIf(linkLayout -> this.getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	default void deleteLink(final String linkId) {
		this.getCanvas().getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		this.getCanvas().document.getModel().getConceptualLinks().removeIf(link -> link.getId().equals(linkId));
		this.getCanvas().document.getModel().getTechnicalLinks().removeIf(link -> link.getId().equals(linkId));
		this.getCanvas().getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

}
