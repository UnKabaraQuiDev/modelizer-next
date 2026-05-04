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

	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
	}

	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
	}

	public Collection<LinkModel> getAllLinks() {
		final Collection<LinkModel> all = new HashSet<>(conceptualLinks);
		all.addAll(technicalLinks);
		return all;
	}

	@JsonAnySetter
	public void preDeconstruct() {
		validateData();
	}

	@JsonAnyGetter
	public void postConstruct() {
		validateData();
	}

	public void validateData() {
		getClasses().removeIf(Objects::isNull);
		getComments().removeIf(Objects::isNull);
		getConceptualLinks().removeIf(Objects::isNull);
		getTechnicalLinks().removeIf(Objects::isNull);
	}

	@Override
	public String toString() {
		return "DiagramModel@" + System.identityHashCode(this) + " [classes=" + classes + ", conceptualLinks=" + conceptualLinks
				+ ", technicalLinks=" + technicalLinks + ", comments=" + comments + "]";
	}

}
