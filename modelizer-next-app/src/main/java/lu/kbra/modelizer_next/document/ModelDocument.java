package lu.kbra.modelizer_next.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.layout.WorkspaceState;

public class ModelDocument {

	private int schemaVersion;
	private DocumentMeta meta;
	private DiagramModel model;
	private WorkspaceState workspace;
	@JsonIgnore
	private String source;

	public ModelDocument() {
		this.schemaVersion = 1;
		this.meta = new DocumentMeta();
		this.model = new DiagramModel();
		this.workspace = WorkspaceState.createDefault();
	}

	public DocumentMeta getMeta() {
		return this.meta;
	}

	public DiagramModel getModel() {
		return this.model;
	}

	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	public String getSource() {
		return this.source;
	}

	public WorkspaceState getWorkspace() {
		return this.workspace;
	}

	public void setMeta(final DocumentMeta meta) {
		this.meta = meta;
	}

	public void setModel(final DiagramModel model) {
		this.model = model;
	}

	public void setSchemaVersion(final int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public void setWorkspace(final WorkspaceState workspace) {
		this.workspace = workspace;
	}

	@Override
	public String toString() {
		return "ModelDocument@" + System.identityHashCode(this) + " [schemaVersion=" + this.schemaVersion + ", meta=" + this.meta
				+ ", model=" + this.model + ", workspace=" + this.workspace + ", source=" + this.source + "]";
	}

}
