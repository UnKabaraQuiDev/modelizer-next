package lu.kbra.modelizer_next.document;

import java.time.Instant;
import java.util.UUID;

import lu.kbra.modelizer_next.App;

public class DocumentMeta {

	private String id;
	@Deprecated
	private String name;
	private Instant createdAt;
	private Instant updatedAt;
	private String applicationVersion;

	public DocumentMeta() {
		this.id = UUID.randomUUID().toString();
		this.name = "Untitled";
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		this.applicationVersion = App.VERSION;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Deprecated
	public String getName() {
		return this.name;
	}

	@Deprecated
	public void setName(final String name) {
		this.name = name;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(final Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getApplicationVersion() {
		return this.applicationVersion;
	}

	public void setApplicationVersion(final String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	@Override
	public String toString() {
		return "DocumentMeta@" + System.identityHashCode(this) + " [id=" + this.id + ", name=" + this.name + ", createdAt=" + this.createdAt
				+ ", updatedAt=" + this.updatedAt + ", applicationVersion=" + this.applicationVersion + "]";
	}

}
