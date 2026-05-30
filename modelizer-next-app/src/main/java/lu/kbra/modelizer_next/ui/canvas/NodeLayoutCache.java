package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;

/**
 * Contains node layout lookup, creation, and bounds cache helpers.
 */
public interface NodeLayoutCache extends DiagramCanvasExt {

	default Optional<LinkLayout> findLinkLayout(final String linkId) {
		if (linkId == null) {
			return Optional.empty();
		}

		final LinkLayout ll = this.getCanvas().getPanelState().validateLinkLayoutByLinkIdIndex().get(linkId);

		return Optional.ofNullable(ll);
	}

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

		final NodeLayout nl = (switch (objectType) {
		case CLASS -> this.getCanvas().getPanelState().validateClassLayoutByObjectIdIndex();
		case COMMENT -> this.getCanvas().getPanelState().validateCommentLayoutByObjectIdIndex();
		}).get(objectId);

		return Optional.ofNullable(nl);
	}

	/**
	 * Finds the existing link layout, or creates one when none exists.
	 *
	 * @param linkId id of the link to look up or modify
	 * @return the matching or create link layout, or {@code null} when no match exists
	 */
	default LinkLayout findOrCreateLinkLayout(final String linkId) {
		final LinkLayout ll = this.getCanvas().getPanelState().validateLinkLayoutByLinkIdIndex().get(linkId);
		if (ll != null) {
			return ll;
		}

		final LinkLayout linkLayout = new LinkLayout();
		linkLayout.setLinkId(linkId);
		this.getCanvas().getPanelState().addLinkLayout(linkLayout);
		return linkLayout;
	}

	default NodeLayout findOrCreateNodeLayout(final ClassModel classModel) {
		return this.findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
	}

	default NodeLayout findOrCreateNodeLayout(final CommentModel commentModel) {
		return this.findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
	}

	/**
	 * Finds the existing node layout, or creates one when none exists.
	 *
	 * @param objectType type value to use
	 * @param objectId   id of the element to read or modify
	 * @return the matching or create node layout, or {@code null} when no match exists
	 */
	default NodeLayout findOrCreateNodeLayout(final LayoutObjectType objectType, final String objectId) {
		final NodeLayout nl = (switch (objectType) {
		case CLASS -> this.getCanvas().getPanelState().validateClassLayoutByObjectIdIndex();
		case COMMENT -> this.getCanvas().getPanelState().validateCommentLayoutByObjectIdIndex();
		}).get(objectId);
		if (nl != null) {
			return nl;
		}

		final List<NodeLayout> list = switch (objectType) {
		case CLASS -> this.getCanvas().getPanelState().getClassLayouts();
		case COMMENT -> this.getCanvas().getPanelState().getCommentLayouts();
		};
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(objectType);
		layout.setObjectId(objectId);
		layout.setPosition(new Point2D.Double(80 + list.size() * 30, 80 + list.size() * 30));
		layout.setSize(new Size2D(0, 0));
		switch (objectType) {
		case CLASS -> this.getCanvas().getPanelState().addClassLayout(layout);
		case COMMENT -> this.getCanvas().getPanelState().addCommentLayout(layout);
		}
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
