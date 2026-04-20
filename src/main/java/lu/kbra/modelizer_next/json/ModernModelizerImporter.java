package lu.kbra.modelizer_next.json;

import java.io.File;
import java.io.IOException;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;

public final class ModernModelizerImporter {

	private ModernModelizerImporter() {
	}

	public static ModelDocument importFile(final File file) throws IOException {
		return MNMain.OBJECT_MAPPER.readValue(file, ModelDocument.class);
	}
}
