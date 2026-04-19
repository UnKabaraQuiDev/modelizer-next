package lu.kbra.modelizer_next.document;

import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.layout.WorkspaceState;

public class ModelDocument {

	private int schemaVersion;
	private DocumentMeta meta;
	private DiagramModel model;
	private WorkspaceState workspace;

	public ModelDocument() {
		this.schemaVersion = 1;
		this.meta = new DocumentMeta();
		this.model = new DiagramModel();
		this.workspace = WorkspaceState.createDefault();
	}

	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	public void setSchemaVersion(final int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public DocumentMeta getMeta() {
		return this.meta;
	}

	public void setMeta(final DocumentMeta meta) {
		this.meta = meta;
	}

	public DiagramModel getModel() {
		return this.model;
	}

	public void setModel(final DiagramModel model) {
		this.model = model;
	}

	public WorkspaceState getWorkspace() {
		return this.workspace;
	}

	public void setWorkspace(final WorkspaceState workspace) {
		this.workspace = workspace;
	}

	@Override
	public String toString() {
		return "ModelDocument@" + System.identityHashCode(this) + " [schemaVersion=" + schemaVersion + ", meta=" + meta
				+ ", model=" + model + ", workspace=" + workspace + "]";
	}

}
