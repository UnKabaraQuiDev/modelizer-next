package lu.kbra.modelizer_next.document;

import java.time.Instant;
import java.util.UUID;

import lu.kbra.modelizer_next.common.App;

/**
 * Metadata attached to a Modelizer document, such as version information and document-level
 * attributes.
 */
public class DocumentMeta {

	private String id;
	@Deprecated
	private String name;
	private Instant createdAt;
	private Instant updatedAt;
	private String applicationVersion;

	/**
	 * Creates a document meta instance.
	 */
	public DocumentMeta() {
		this.id = UUID.randomUUID().toString();
		this.name = "Untitled";
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		this.applicationVersion = App.VERSION;
	}

	/**
	 * Returns the application version.
	 *
	 * @return the application version
	 */
	public String getApplicationVersion() {
		return this.applicationVersion;
	}

	/**
	 * Returns the created at.
	 *
	 * @return the created at
	 */
	public Instant getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Returns the ID.
	 *
	 * @return the ID
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the name.
	 *
	 * @return the name
	 */
	@Deprecated
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the updated at.
	 *
	 * @return the updated at
	 */
	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	/**
	 * Sets the application version.
	 *
	 * @param applicationVersion text value for application version
	 */
	public void setApplicationVersion(final String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	/**
	 * Sets the created at.
	 *
	 * @param createdAt created at value used by the operation
	 */
	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id stable id of the model element
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the name.
	 *
	 * @param name name value to read, write, or display
	 */
	@Deprecated
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Sets the updated at.
	 *
	 * @param updatedAt updated at value used by the operation
	 */
	public void setUpdatedAt(final Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Builds a debug string for this document meta.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "DocumentMeta@" + System.identityHashCode(this) + " [id=" + this.id + ", name=" + this.name + ", createdAt=" + this.createdAt
				+ ", updatedAt=" + this.updatedAt + ", applicationVersion=" + this.applicationVersion + "]";
	}

}
