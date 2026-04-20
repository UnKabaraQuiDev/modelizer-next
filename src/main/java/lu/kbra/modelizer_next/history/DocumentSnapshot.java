package lu.kbra.modelizer_next.history;

import java.io.IOException;
import java.io.UncheckedIOException;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;

public record DocumentSnapshot(String json) {

	public static DocumentSnapshot from(final ModelDocument document) {
		try {
			return new DocumentSnapshot(MNMain.OBJECT_MAPPER.writeValueAsString(document));
		} catch (final IOException ex) {
			throw new UncheckedIOException("Failed to create document snapshot", ex);
		}
	}

	public boolean sameDocumentState(final ModelDocument document) {
		return this.equals(DocumentSnapshot.from(document));
	}

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
