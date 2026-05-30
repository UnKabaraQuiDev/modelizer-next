package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.impl.PostConstructOwner;
import lu.kbra.modelizer_next.ui.impl.PreDeconstructOwner;

/**
 * Root model for diagram elements. It stores classes, links, comments, and their relationships.
 */
public class DiagramModel implements PostConstructOwner, PreDeconstructOwner {

	private List<ClassModel> classes;
	private List<LinkModel> conceptualLinks;
	private List<LinkModel> technicalLinks;
	private List<CommentModel> comments;

	@JsonIgnore
	private final Map<String, ClassModel> classById = new HashMap<>();
	@JsonIgnore
	private final Map<String, LinkModel> conceptualLinkById = new HashMap<>();
	@JsonIgnore
	private final Map<String, LinkModel> technicalLinkById = new HashMap<>();
	@JsonIgnore
	private final Map<String, CommentModel> commentById = new HashMap<>();
	@JsonIgnore
	private final Map<String, LinkModel> linkByAssociationClassId = new HashMap<>();
	@JsonIgnore
	private final Map<String, LinkModel> linkById = new HashMap<>();
	@JsonIgnore
	private final Map<PanelType, Set<ClassModel>> classesByPanel = new EnumMap<>(PanelType.class);

	/**
	 * Creates a diagram model instance.
	 */
	public DiagramModel() {
		this.classes = new ArrayList<>();
		this.conceptualLinks = new ArrayList<>();
		this.technicalLinks = new ArrayList<>();
		this.comments = new ArrayList<>();
	}

	public void addClass(final ClassModel classModel) {
		if (classModel == null) {
			return;
		}
		this.classes.add(classModel);
		this.classById.put(classModel.getId(), classModel);
	}

	public void addComment(final CommentModel commentModel) {
		if (commentModel == null) {
			return;
		}
		this.comments.add(commentModel);
		this.commentById.put(commentModel.getId(), commentModel);
	}

	public void addConceptualLink(final LinkModel linkModel) {
		if (linkModel == null) {
			return;
		}
		this.conceptualLinks.add(linkModel);
		this.conceptualLinkById.put(linkModel.getId(), linkModel);
		this.linkById.put(linkModel.getId(), linkModel);
		this.linkByAssociationClassId.put(linkModel.getAssociationClassId(), linkModel);
	}

	public void addTechnicalLink(final LinkModel linkModel) {
		if (linkModel == null) {
			return;
		}
		this.technicalLinks.add(linkModel);
		this.technicalLinkById.put(linkModel.getId(), linkModel);
		this.linkById.put(linkModel.getId(), linkModel);
//		linkByAssociationClassId.put(linkModel.getAssociationClassId(), linkModel);
	}

	public Map<String, ClassModel> buildClassByIdIndex() {
		this.classById.clear();
		this.classes.stream().forEach(f -> this.classById.put(f.getId(), f));
		return this.classById;
	}

	public Map<PanelType, Set<ClassModel>> buildClassesByPanelIndex() {
		this.classesByPanel.clear();
		final Set<ClassModel> conceptualList = new HashSet<>();
		final Set<ClassModel> logicalList = new HashSet<>();
		final Set<ClassModel> physicalList = new HashSet<>();
		for (final ClassModel classModel : this.classes) {
			if (classModel.isVisibleInConceptual()) {
				conceptualList.add(classModel);
			}
			if (classModel.isVisibleInLogical()) {
				logicalList.add(classModel);
			}
			if (classModel.isVisibleInPhysical()) {
				physicalList.add(classModel);
			}
		}
		this.classesByPanel.put(PanelType.CONCEPTUAL, conceptualList);
		this.classesByPanel.put(PanelType.LOGICAL, logicalList);
		this.classesByPanel.put(PanelType.PHYSICAL, physicalList);
		return this.classesByPanel;
	}

	public Map<String, CommentModel> buildCommentByIdIndex() {
		this.commentById.clear();
		this.comments.stream().forEach(f -> this.commentById.put(f.getId(), f));
		return this.commentById;
	}

	public Map<String, LinkModel> buildConceptualLinkByIdIndex() {
		this.conceptualLinkById.clear();
		this.conceptualLinks.stream().forEach(f -> this.conceptualLinkById.put(f.getId(), f));
		return this.conceptualLinkById;
	}

	public Map<String, LinkModel> buildLinkByAssociationClassIdIndex() {
		this.linkByAssociationClassId.clear();
		this.conceptualLinks.stream().filter(LinkModel::hasAssociationClass).forEach(f -> this.linkByAssociationClassId.put(f.getId(), f));
		return this.linkByAssociationClassId;
	}

	public Map<String, LinkModel> buildLinkByIdIndex() {
		this.linkById.clear();
		this.conceptualLinks.stream().forEach(f -> this.linkById.put(f.getId(), f));
		this.technicalLinks.stream().forEach(f -> this.linkById.put(f.getId(), f));
		return this.linkById;
	}

	public Map<String, LinkModel> buildTechnicalLinkByIdIndex() {
		this.technicalLinkById.clear();
		this.technicalLinks.stream().forEach(f -> this.technicalLinkById.put(f.getId(), f));
		return this.technicalLinkById;
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

	public Map<String, ClassModel> getClassById() {
		return this.classById;
	}

	/**
	 * Returns the classes.
	 *
	 * @return the classes
	 */
	public List<ClassModel> getClasses() {
		return this.classes;
	}

	public Set<ClassModel> getClasses(final PanelType panelType) {
		return this.classesByPanel.get(panelType);
	}

	public Map<PanelType, Set<ClassModel>> getClassesByPanel() {
		return this.classesByPanel;
	}

	public Map<String, CommentModel> getCommentById() {
		return this.commentById;
	}

	/**
	 * Returns the comments.
	 *
	 * @return the comments
	 */
	public List<CommentModel> getComments() {
		return this.comments;
	}

	public Map<String, LinkModel> getConceptualLinkById() {
		return this.conceptualLinkById;
	}

	/**
	 * Returns the conceptual links.
	 *
	 * @return the conceptual links
	 */
	public List<LinkModel> getConceptualLinks() {
		return this.conceptualLinks;
	}

	public Map<String, LinkModel> getLinkByAssociationClassId() {
		return this.linkByAssociationClassId;
	}

	public Map<String, LinkModel> getLinkById() {
		return this.linkById;
	}

	public Map<String, LinkModel> getTechnicalLinkById() {
		return this.technicalLinkById;
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
	 * Restores derived model state after JSON deserialization.
	 */
	@Override
	public void postConstruct() {
		this.validateData();

		this.buildClassByIdIndex();
		this.buildCommentByIdIndex();
		this.buildConceptualLinkByIdIndex();
		this.buildTechnicalLinkByIdIndex();
		this.buildLinkByAssociationClassIdIndex();
		this.buildLinkByIdIndex();
		this.buildClassesByPanelIndex();
	}

	/**
	 * Prepares the model before it is serialized.
	 */
	@Override
	public void preDeconstruct() {
		this.validateData();
	}

	public void removeClass(final ClassModel classModel) {
		this.classes.remove(classModel);
		if (classModel == null) {
			return;
		}
		this.classById.remove(classModel.getId());
	}

	public void removeComment(final CommentModel commentModel) {
		this.comments.remove(commentModel);
		if (commentModel == null) {
			return;
		}
		this.commentById.remove(commentModel.getId());
	}

	public void removeConceptualLink(final LinkModel linkModel) {
		this.conceptualLinks.remove(linkModel);
		if (linkModel == null) {
			return;
		}
		this.conceptualLinkById.remove(linkModel.getId());
		this.linkById.remove(linkModel.getId());
		this.linkByAssociationClassId.remove(linkModel.getAssociationClassId());
	}

	public void removeTechnicalLink(final LinkModel linkModel) {
		this.technicalLinks.remove(linkModel);
		if (linkModel == null) {
			return;
		}
		this.technicalLinkById.remove(linkModel.getId());
		this.linkById.remove(linkModel.getId());
//		linkByAssociationClassId.remove(linkModel.getAssociationClassId());
	}

	/**
	 * Sets the classes.
	 *
	 * @param classes values for classes
	 */
	@JsonProperty("classes")
	public void setClasses(final List<ClassModel> classes) {
		this.classes = classes;
		this.buildClassByIdIndex();
	}

	/**
	 * Sets the comments.
	 *
	 * @param comments values for comments
	 */
	@JsonProperty("comments")
	public void setComments(final List<CommentModel> comments) {
		this.comments = comments;
		this.buildCommentByIdIndex();
	}

	@JsonProperty("conceptualLinks")
	public void setConceptualLinks(final List<LinkModel> conceptualLinks) {
		this.conceptualLinks = conceptualLinks;
		this.buildConceptualLinkByIdIndex();
	}

	@JsonProperty("technicalLinks")
	public void setTechnicalLinks(final List<LinkModel> technicalLinks) {
		this.technicalLinks = technicalLinks;
		this.buildTechnicalLinkByIdIndex();
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

	public Map<String, ClassModel> validateClassByIdIndex() {
		if (this.classById.size() != this.classes.size()) {
			this.buildClassByIdIndex();
		}
		return this.classById;
	}

	public void validateClassByPanel(final ClassModel classModel) {
		if (classModel.isVisibleInConceptual()) {
			this.classesByPanel.get(PanelType.CONCEPTUAL).add(classModel);
		} else {
			this.classesByPanel.get(PanelType.CONCEPTUAL).remove(classModel);
		}
		if (classModel.isVisibleInLogical()) {
			this.classesByPanel.get(PanelType.LOGICAL).add(classModel);
		} else {
			this.classesByPanel.get(PanelType.LOGICAL).remove(classModel);
		}
		if (classModel.isVisibleInPhysical()) {
			this.classesByPanel.get(PanelType.PHYSICAL).add(classModel);
		} else {
			this.classesByPanel.get(PanelType.PHYSICAL).remove(classModel);
		}
	}

	public Map<String, CommentModel> validateCommentsByIdIndex() {
		if (this.commentById.size() != this.comments.size()) {
			this.buildCommentByIdIndex();
		}
		return this.commentById;
	}

	public Map<String, LinkModel> validateConceptualLinkByIdIndex() {
		if (this.conceptualLinkById.size() != this.conceptualLinks.size()) {
			this.buildConceptualLinkByIdIndex();
		}
		return this.conceptualLinkById;
	}

	/**
	 * Validates the data before it is used.
	 */
	public void validateData() {
		this.getClasses().removeIf(c -> c == null || c.getId() == null || c.getId().isBlank());
		this.getComments().removeIf(c -> c == null || c.getId() == null || c.getId().isBlank());
		this.getConceptualLinks().removeIf(c -> c == null || c.getId() == null || c.getId().isBlank());
		this.getTechnicalLinks().removeIf(c -> c == null || c.getId() == null || c.getId().isBlank());
	}

	public Map<String, LinkModel> validateLinkByAssociationClassIdIndex() {
		this.buildLinkByAssociationClassIdIndex();
		return this.linkByAssociationClassId;
	}

	public Map<String, LinkModel> validateLinkByIdIndex() {
		if (this.linkById.size() != this.technicalLinks.size() + this.conceptualLinks.size()) {
			this.buildLinkByIdIndex();
		}
		return this.linkById;
	}

	public Map<String, LinkModel> validateTechnicalLinkByIdIndex() {
		if (this.technicalLinkById.size() != this.technicalLinks.size()) {
			this.buildTechnicalLinkByIdIndex();
		}
		return this.technicalLinkById;
	}

}
