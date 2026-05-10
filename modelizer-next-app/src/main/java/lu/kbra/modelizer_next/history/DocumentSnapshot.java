package lu.kbra.modelizer_next.history;

import java.io.IOException;
import java.io.UncheckedIOException;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;

/**
 * Immutable undo/redo snapshot containing a serialized document state and a user-facing
 * description.
 *
 * @param json JSON text or node to read
 */
public record DocumentSnapshot(String json) {

	/**
	 * Creates a value from the supplied input.
	 *
	 * @param document document to read or modify
	 * @return the from result
	 */
	public static DocumentSnapshot from(final ModelDocument document) {
		try {
			return new DocumentSnapshot(MNMain.OBJECT_MAPPER.writeValueAsString(document));
		} catch (final IOException ex) {
			throw new UncheckedIOException("Failed to create document snapshot", ex);
		}
	}

	/**
	 * Checks whether the document state is the same.
	 *
	 * @param document document to read or modify
	 * @return {@code true} when the condition is met; otherwise {@code false}
	 */
	public boolean sameDocumentState(final ModelDocument document) {
		return this.equals(DocumentSnapshot.from(document));
	}

	/**
	 * Restores the into.
	 *
	 * @param target target value used by the operation
	 */
	public void restoreInto(final ModelDocument target) {
		try {
			final ModelDocument restored = MNMain.OBJECT_MAPPER.readValue(this.json, ModelDocument.class);

			target.setSchemaVersion(restored.getSchemaVersion());
			target.setMeta(restored.getMeta());
			target.setModel(restored.getModel());
			target.setWorkspace(restored.getWorkspace());
		} catch (final IOException ex) {
			throw new UncheckedIOException("Failed to restore document snapshot", ex);
		}
	}
}
