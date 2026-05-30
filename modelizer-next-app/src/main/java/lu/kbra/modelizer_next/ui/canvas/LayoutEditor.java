package lu.kbra.modelizer_next.ui.canvas;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;

public interface LayoutEditor extends DiagramCanvasExt {

	default void synchronizePosition(final boolean selectionOnly, final PanelType otherPanelType) {
		if (otherPanelType == null) {
			return;
		}

		final DiagramCanvas otherPanel = this.getFrame().getCanvas(otherPanelType);

		for (final ClassModel classModel : this.getDocument().getModel().getClasses()) {
			if (classModel == null || !classModel.isVisible(this.getPanelType())
					|| (selectionOnly && !this.getCanvas().isClassSelected(classModel.getId()))) {
				continue;
			}

			final NodeLayout otherLayout = otherPanel.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			final NodeLayout thisLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			thisLayout.setLayout(otherLayout);
		}

		for (final CommentModel commentModel : this.getDocument().getModel().getComments()) {
			if (commentModel == null || !commentModel.isVisible(this.getPanelType())
					|| (selectionOnly && !this.getCanvas().isCommentSelected(commentModel.getId()))) {
				continue;
			}

			final NodeLayout otherLayout = otherPanel.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			final NodeLayout thisLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			thisLayout.setLayout(otherLayout);
		}

		this.getCanvas().notifyDocumentChanged();
	}

}
