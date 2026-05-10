package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Root model for diagram elements. It stores classes, links, comments, and their relationships.
 */
public class DiagramModel {

	private List<ClassModel> classes;
	private final List<LinkModel> conceptualLinks;
	private final List<LinkModel> technicalLinks;
	private List<CommentModel> comments;

	/**
	 * Creates a diagram model instance.
	 */
	public DiagramModel() {
		this.classes = new ArrayList<>();
		this.conceptualLinks = new ArrayList<>();
		this.technicalLinks = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	/**
	 * Returns the all links.
	 *
	 * @return the all links
	 */
	public Collection<LinkModel> getAllLinks() {
		final Collection<LinkModel> all = new HashSet<>(this.conceptualLinks);
		all.addAll(this.technicalLinks);
		return all;
	}

	/**
	 * Returns the classes.
	 *
	 * @return the classes
	 */
	public List<ClassModel> getClasses() {
		return this.classes;
	}

	/**
	 * Returns the comments.
	 *
	 * @return the comments
	 */
	public List<CommentModel> getComments() {
		return this.comments;
	}

	/**
	 * Returns the conceptual links.
	 *
	 * @return the conceptual links
	 */
	public List<LinkModel> getConceptualLinks() {
		return this.conceptualLinks;
	}

	/**
	 * Returns the technical links.
	 *
	 * @return the technical links
	 */
	public List<LinkModel> getTechnicalLinks() {
		return this.technicalLinks;
	}

	/**
	 * Restores derived model state after JSON construction.
	 */
	@JsonAnyGetter
	public void postConstruct() {
		this.validateData();
	}

	/**
	 * Prepares the model before it is serialized or deconstructed.
	 */
	@JsonAnySetter
	public void preDeconstruct() {
		this.validateData();
	}

	/**
	 * Sets the classes.
	 *
	 * @param classes values for classes
	 */
	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
	}

	/**
	 * Sets the comments.
	 *
	 * @param comments values for comments
	 */
	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
	}

	/**
	 * Builds a debug string for this diagram model.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "DiagramModel@" + System.identityHashCode(this) + " [classes=" + this.classes + ", conceptualLinks=" + this.conceptualLinks
				+ ", technicalLinks=" + this.technicalLinks + ", comments=" + this.comments + "]";
	}

	/**
	 * Validates the data before it is used.
	 */
	public void validateData() {
		this.getClasses().removeIf(Objects::isNull);
		this.getComments().removeIf(Objects::isNull);
		this.getConceptualLinks().removeIf(Objects::isNull);
		this.getTechnicalLinks().removeIf(Objects::isNull);
	}

}
