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

	/**
	 * Finds the node layout that matches the supplied input.
	 *
	 * @param objectType type value to use
	 * @param objectId   id of the element to read or modify
	 * @return the matching node layout, or {@code null} when no match exists
	 */
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

	/**
	 * Finds the existing link layout, or creates one when none exists.
	 *
	 * @param linkId id of the link to look up or modify
	 * @return the matching or create link layout, or {@code null} when no match exists
	 */
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

	/**
	 * Finds the existing node layout, or creates one when none exists.
	 *
	 * @param objectType type value to use
	 * @param objectId   id of the element to read or modify
	 * @return the matching or create node layout, or {@code null} when no match exists
	 */
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

	/**
	 * Returns the panel state on the active canvas.
	 *
	 * @return the panel state
	 */
	default PanelState getPanelState() {
		return this.getDocument().getWorkspace().getPanels().get(this.getPanelType());
	}

}
