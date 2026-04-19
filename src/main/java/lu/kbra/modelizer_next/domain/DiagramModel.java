package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;

public class DiagramModel {

	private List<ClassModel> classes;
	private List<LinkModel> links;
	private List<CommentModel> comments;

	public DiagramModel() {
		this.classes = new ArrayList<>();
		this.links = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	public List<ClassModel> getClasses() {
		return this.classes;
	}

	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
	}

	public List<LinkModel> getLinks() {
		return this.links;
	}

	public void setLinks(final List<LinkModel> links) {
		this.links = links;
	}

	public List<CommentModel> getComments() {
		return this.comments;
	}

	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "DiagramModel@" + System.identityHashCode(this) + " [classes=" + classes + ", links=" + links
				+ ", comments=" + comments + "]";
	}

}
