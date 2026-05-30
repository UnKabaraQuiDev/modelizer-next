package lu.kbra.modelizer_next.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.ui.impl.PostConstructOwner;
import lu.kbra.modelizer_next.ui.impl.PreDeconstructOwner;

/**
 * Persistent state for one workspace panel.
 */
public class PanelState implements PostConstructOwner, PreDeconstructOwner {

	private double zoom;
	private double panX;
	private double panY;
	@JsonIgnore
	private List<LinkLayout> linkLayouts;
	private List<NodeLayout> classLayouts;
	private List<NodeLayout> commentLayouts;

	@JsonIgnore
	private final Map<String, LinkLayout> linkLayoutByLinkId = new HashMap<>();
	@JsonIgnore
	private final Map<String, NodeLayout> classLayoutByObjectId = new HashMap<>();
	@JsonIgnore
	private final Map<String, NodeLayout> commentLayoutByObjectId = new HashMap<>();

	/**
	 * Creates a panel state instance.
	 */
	public PanelState() {
		this.zoom = 1.0;
		this.panX = 0.0;
		this.panY = 0.0;
		this.linkLayouts = new ArrayList<>();
		this.classLayouts = new ArrayList<>();
		this.commentLayouts = new ArrayList<>();
	}

	public void addClassLayout(final NodeLayout layout) {
		if (layout.getObjectType() != LayoutObjectType.CLASS) {
			throw new IllegalArgumentException("Unexpected LayoutObjectType: " + layout.getObjectType());
		}
		this.classLayouts.add(layout);
		this.classLayoutByObjectId.put(layout.getObjectId(), layout);
	}

	public void addCommentLayout(final NodeLayout layout) {
		if (layout.getObjectType() != LayoutObjectType.COMMENT) {
			throw new IllegalArgumentException("Unexpected LayoutObjectType: " + layout.getObjectType());
		}
		this.commentLayouts.add(layout);
		this.commentLayoutByObjectId.put(layout.getObjectId(), layout);
	}

	public void addLinkLayout(final LinkLayout layout) {
		this.linkLayouts.add(layout);
		this.linkLayoutByLinkId.put(layout.getLinkId(), layout);
	}

	public Map<String, NodeLayout> buildClassLayoutByObjectIdIndex() {
		this.commentLayoutByObjectId.clear();
		this.commentLayouts.parallelStream().forEach(c -> this.commentLayoutByObjectId.put(c.getObjectId(), c));
		return this.commentLayoutByObjectId;
	}

	public Map<String, NodeLayout> buildCommentLayoutByObjectIdIndex() {
		this.classLayoutByObjectId.clear();
		this.classLayouts.parallelStream().forEach(c -> this.classLayoutByObjectId.put(c.getObjectId(), c));
		return this.classLayoutByObjectId;
	}

	public Map<String, LinkLayout> buildLinkLayoutByLinkIdIndex() {
		this.linkLayoutByLinkId.clear();
		this.linkLayouts.parallelStream().forEach(c -> this.linkLayoutByLinkId.put(c.getLinkId(), c));
		return this.linkLayoutByLinkId;
	}

	public Map<String, NodeLayout> getClassLayoutByObjectId() {
		return this.classLayoutByObjectId;
	}

	public List<NodeLayout> getClassLayouts() {
		return this.classLayouts;
	}

	public Map<String, NodeLayout> getCommentLayoutByObjectId() {
		return this.commentLayoutByObjectId;
	}

	public List<NodeLayout> getCommentLayouts() {
		return this.commentLayouts;
	}

	public Map<String, LinkLayout> getLinkLayoutByLinkId() {
		return this.linkLayoutByLinkId;
	}

	/**
	 * Returns the link layouts.
	 *
	 * @return the link layouts
	 */
	public List<LinkLayout> getLinkLayouts() {
		return this.linkLayouts;
	}

	/**
	 * Returns the pan x.
	 *
	 * @return the pan x
	 */
	public double getPanX() {
		return this.panX;
	}

	/**
	 * Returns the pan y.
	 *
	 * @return the pan y
	 */
	public double getPanY() {
		return this.panY;
	}

	/**
	 * Returns the zoom.
	 *
	 * @return the zoom
	 */
	public double getZoom() {
		return this.zoom;
	}

	@Override
	public void postConstruct() {
		this.validateData();

		this.buildLinkLayoutByLinkIdIndex();
		this.buildClassLayoutByObjectIdIndex();
		this.buildCommentLayoutByObjectIdIndex();
	}

	@Override
	public void preDeconstruct() {
		this.validateData();
	}

	public void removeClassLayout(final NodeLayout classLayout) {
		this.classLayoutByObjectId.remove(classLayout.getObjectId());
		this.classLayouts.remove(classLayout);
	}

	public void removeClassLayout(final String classId) {
		final NodeLayout classLayout = this.classLayoutByObjectId.remove(classId);
		if (classLayout != null) {
			this.classLayouts.remove(classLayout);
		} else {
			this.classLayouts.removeIf(t -> Objects.equals(classId, t.getObjectId()));
		}
	}

	public void removeCommentLayout(final NodeLayout commentLayout) {
		this.commentLayoutByObjectId.remove(commentLayout.getObjectId());
		this.commentLayouts.remove(commentLayout);
	}

	public void removeCommentLayout(final String commentId) {
		final NodeLayout commentLayout = this.commentLayoutByObjectId.remove(commentId);
		if (commentLayout != null) {
			this.commentLayouts.remove(commentLayout);
		} else {
			this.commentLayouts.removeIf(t -> Objects.equals(commentId, t.getObjectId()));
		}
	}

	public void removeLinkLayout(final LinkLayout linkLayout) {
		this.linkLayouts.remove(linkLayout);
		this.linkLayoutByLinkId.remove(linkLayout.getLinkId());
	}

	public void removeLinkLayout(final String linkId) {
		final LinkLayout linkLayout = this.linkLayoutByLinkId.remove(linkId);
		if (linkLayout != null) {
			this.linkLayouts.remove(linkLayout);
		} else {
			this.linkLayouts.removeIf(t -> Objects.equals(linkId, t.getLinkId()));
		}
	}

	public void setClassLayouts(final List<NodeLayout> classLayouts) {
		this.classLayouts = classLayouts;
	}

	public void setCommentLayouts(final List<NodeLayout> commentLayouts) {
		this.commentLayouts = commentLayouts;
	}

	/**
	 * Sets the link layouts.
	 *
	 * @param linkLayouts layout objects to read or modify
	 */
	public void setLinkLayouts(final List<LinkLayout> linkLayouts) {
		this.linkLayouts = linkLayouts;
	}

	@Deprecated
	@JsonProperty("nodeLayouts")
	public void setNodeLayouts(final List<NodeLayout> nodeLayouts) {
		nodeLayouts.parallelStream().forEach(c -> (switch (c.getObjectType()) {
		case CLASS -> this.classLayouts;
		case COMMENT -> this.commentLayouts;
		}).add(c));

	}

	/**
	 * Sets the pan x.
	 *
	 * @param panX numeric pan x value
	 */
	public void setPanX(final double panX) {
		this.panX = panX;
	}

	/**
	 * Sets the pan y.
	 *
	 * @param panY numeric pan y value
	 */
	public void setPanY(final double panY) {
		this.panY = panY;
	}

	/**
	 * Sets the zoom.
	 *
	 * @param zoom numeric zoom value
	 */
	public void setZoom(final double zoom) {
		this.zoom = zoom;
	}

	@Override
	public String toString() {
		return "PanelState@" + System.identityHashCode(this) + " [zoom=" + this.zoom + ", panX=" + this.panX + ", panY=" + this.panY
				+ ", linkLayouts=" + this.linkLayouts + ", classLayouts=" + this.classLayouts + ", commentLayouts=" + this.commentLayouts
				+ ", classLayoutByObjectId=" + this.classLayoutByObjectId + ", commentLayoutByObjectId=" + this.commentLayoutByObjectId
				+ "]";
	}

	public Map<String, NodeLayout> validateClassLayoutByObjectIdIndex() {
		if (this.classLayoutByObjectId.size() != this.classLayouts.size()) {
			this.buildClassLayoutByObjectIdIndex();
		}
		return this.classLayoutByObjectId;
	}

	public Map<String, NodeLayout> validateCommentLayoutByObjectIdIndex() {
		if (this.commentLayoutByObjectId.size() != this.commentLayouts.size()) {
			this.buildCommentLayoutByObjectIdIndex();
		}
		return this.commentLayoutByObjectId;
	}

	public void validateData() {
		this.linkLayouts.removeIf(c -> c == null || c.getLinkId() == null || c.getLinkId().isBlank());
		this.classLayouts.removeIf(c -> c == null || c.getObjectId() == null || c.getObjectId().isBlank());
		this.commentLayouts.removeIf(c -> c == null || c.getObjectId() == null || c.getObjectId().isBlank());
	}

	public Map<String, LinkLayout> validateLinkLayoutByLinkIdIndex() {
		if (this.linkLayoutByLinkId.size() != this.linkLayouts.size()) {
			this.buildLinkLayoutByLinkIdIndex();
		}
		return this.linkLayoutByLinkId;
	}

}
