package lu.kbra.modelizer_next.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.layout.WorkspaceState;

/**
 * Top-level persistent document that owns the diagram model, metadata, workspace layout, and style
 * palettes.
 */
public class ModelDocument {

	private int schemaVersion;
	private DocumentMeta meta;
	private DiagramModel model;
	private WorkspaceState workspace;
	@JsonIgnore
	private String source;

	/**
	 * Creates a model document instance.
	 */
	public ModelDocument() {
		this.schemaVersion = 1;
		this.meta = new DocumentMeta();
		this.model = new DiagramModel();
		this.workspace = WorkspaceState.createDefault();
	}

	/**
	 * Returns the meta.
	 *
	 * @return the meta
	 */
	public DocumentMeta getMeta() {
		return this.meta;
	}

	/**
	 * Returns the model.
	 *
	 * @return the model
	 */
	public DiagramModel getModel() {
		return this.model;
	}

	/**
	 * Returns the schema version.
	 *
	 * @return the schema version
	 */
	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	/**
	 * Returns the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * Returns the workspace.
	 *
	 * @return the workspace
	 */
	public WorkspaceState getWorkspace() {
		return this.workspace;
	}

	/**
	 * Sets the meta.
	 *
	 * @param meta meta value used by the operation
	 */
	public void setMeta(final DocumentMeta meta) {
		this.meta = meta;
	}

	/**
	 * Sets the model.
	 *
	 * @param model diagram model to read or modify
	 */
	public void setModel(final DiagramModel model) {
		this.model = model;
	}

	/**
	 * Sets the schema version.
	 *
	 * @param schemaVersion numeric schema version value
	 */
	public void setSchemaVersion(final int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/**
	 * Sets the source.
	 *
	 * @param source source object used by the operation
	 */
	public void setSource(final String source) {
		this.source = source;
	}

	/**
	 * Sets the workspace.
	 *
	 * @param workspace workspace value used by the operation
	 */
	public void setWorkspace(final WorkspaceState workspace) {
		this.workspace = workspace;
	}

	/**
	 * Builds a debug string for this model document.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "ModelDocument@" + System.identityHashCode(this) + " [schemaVersion=" + this.schemaVersion + ", meta=" + this.meta
				+ ", model=" + this.model + ", workspace=" + this.workspace + ", source=" + this.source + "]";
	}

}
