package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;

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

	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
	}

	public List<LinkModel> getConceptualLinks() {
		return this.conceptualLinks;
	}

	public List<LinkModel> getTechnicalLinks() {
		return this.technicalLinks;
	}

	public List<CommentModel> getComments() {
		return this.comments;
	}

	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "DiagramModel@" + System.identityHashCode(this) + " [classes=" + this.classes + ", conceptualLinks="
				+ this.conceptualLinks + ", technicalLinks=" + this.technicalLinks + ", comments=" + this.comments
				+ "]";
	}

}
