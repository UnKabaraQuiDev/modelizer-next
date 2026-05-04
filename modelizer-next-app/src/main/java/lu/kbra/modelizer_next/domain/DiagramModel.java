package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class DiagramModel {

	private List<ClassModel> classes;
	private final List<LinkModel> conceptualLinks;
	private final List<LinkModel> technicalLinks;
	private List<CommentModel> comments;

	public DiagramModel() {
		this.classes = new ArrayList<>();
		this.conceptualLinks = new ArrayList<>();
		this.technicalLinks = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	public Collection<LinkModel> getAllLinks() {
		final Collection<LinkModel> all = new HashSet<>(this.conceptualLinks);
		all.addAll(this.technicalLinks);
		return all;
	}

	public List<ClassModel> getClasses() {
		return this.classes;
	}

	public List<CommentModel> getComments() {
		return this.comments;
	}

	public List<LinkModel> getConceptualLinks() {
		return this.conceptualLinks;
	}

	public List<LinkModel> getTechnicalLinks() {
		return this.technicalLinks;
	}

	@JsonAnyGetter
	public void postConstruct() {
		this.validateData();
	}

	@JsonAnySetter
	public void preDeconstruct() {
		this.validateData();
	}

	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
	}

	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "DiagramModel@" + System.identityHashCode(this) + " [classes=" + this.classes + ", conceptualLinks=" + this.conceptualLinks
				+ ", technicalLinks=" + this.technicalLinks + ", comments=" + this.comments + "]";
	}

	public void validateData() {
		this.getClasses().removeIf(Objects::isNull);
		this.getComments().removeIf(Objects::isNull);
		this.getConceptualLinks().removeIf(Objects::isNull);
		this.getTechnicalLinks().removeIf(Objects::isNull);
	}

}
