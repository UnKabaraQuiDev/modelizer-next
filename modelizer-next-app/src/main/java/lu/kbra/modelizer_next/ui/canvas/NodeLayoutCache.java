package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.util.Optional;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;

/**
 * Contains node layout lookup, creation, and bounds cache helpers.
 */
public interface NodeLayoutCache extends DiagramCanvasExt {

	default Optional<NodeLayout> findNodeLayout(final LayoutObjectType objectType, final String objectId) {
		if (objectId == null) {
			return Optional.empty();
		}

		for (final NodeLayout layout : this.getCanvas().getPanelState().getNodeLayouts()) {
			if (layout.getObjectType() == objectType && objectId.equals(layout.getObjectId())) {
				return Optional.of(layout);
			}
		}

		return Optional.empty();
	}

	default LinkLayout findOrCreateLinkLayout(final String linkId) {
		for (final LinkLayout linkLayout : this.getCanvas().getPanelState().getLinkLayouts()) {
			if (linkLayout.getLinkId().equals(linkId)) {
				return linkLayout;
			}
		}

		final LinkLayout linkLayout = new LinkLayout();
		linkLayout.setLinkId(linkId);
		this.getCanvas().getPanelState().getLinkLayouts().add(linkLayout);
		return linkLayout;
	}

	default NodeLayout findOrCreateNodeLayout(final LayoutObjectType objectType, final String objectId) {
		for (final NodeLayout layout : this.getCanvas().getPanelState().getNodeLayouts()) {
			if (layout.getObjectType() == objectType && layout.getObjectId().equals(objectId)) {
				return layout;
			}
		}

		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(objectType);
		layout.setObjectId(objectId);
		layout.setPosition(new Point2D.Double(80 + this.getCanvas().getPanelState().getNodeLayouts().size() * 30,
				80 + this.getCanvas().getPanelState().getNodeLayouts().size() * 30));
		layout.setSize(new Size2D(0, 0));
		this.getCanvas().getPanelState().getNodeLayouts().add(layout);
		return layout;
	}

	default PanelState getPanelState() {
		return this.getDocument().getWorkspace().getPanels().get(this.getPanelType());
	}

}
