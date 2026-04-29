package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;

public interface ElementDeleter extends DiagramCanvasExt {

	default void deleteClass(final String classId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		getCanvas().document.getModel().getClasses().remove(classModel);
		getCanvas().getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.CLASS && layout.getObjectId().equals(classId));

		getCanvas().getActiveLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		getCanvas().document.getModel()
				.getConceptualLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));
		getCanvas().document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> classId.equals(link.getFrom().getClassId()) || classId.equals(link.getTo().getClassId())
						|| classId.equals(link.getAssociationClassId()));

		getCanvas().getPanelState().getLinkLayouts().removeIf(linkLayout -> getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	default void deleteComment(final String commentId) {
		getCanvas().document.getModel().getComments().removeIf(comment -> comment.getId().equals(commentId));
		getCanvas().getPanelState()
				.getNodeLayouts()
				.removeIf(layout -> layout.getObjectType() == LayoutObjectType.COMMENT && layout.getObjectId().equals(commentId));
	}

	default void deleteField(final String classId, final String fieldId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null) {
			return;
		}

		classModel.getFields().removeIf(field -> field.getId().equals(fieldId));
		getCanvas().document.getModel()
				.getTechnicalLinks()
				.removeIf(link -> fieldId.equals(link.getFrom().getFieldId()) || fieldId.equals(link.getTo().getFieldId()));
		getCanvas().getPanelState().getLinkLayouts().removeIf(linkLayout -> getCanvas().findLinkById(linkLayout.getLinkId()) == null);
	}

	default void deleteLink(final String linkId) {
		getCanvas().getActiveLinks().removeIf(link -> link.getId().equals(linkId));
		getCanvas().document.getModel().getConceptualLinks().removeIf(link -> link.getId().equals(linkId));
		getCanvas().document.getModel().getTechnicalLinks().removeIf(link -> link.getId().equals(linkId));
		getCanvas().getPanelState().getLinkLayouts().removeIf(linkLayout -> linkLayout.getLinkId().equals(linkId));
	}

}
