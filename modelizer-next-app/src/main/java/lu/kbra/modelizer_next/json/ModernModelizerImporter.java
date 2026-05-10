package lu.kbra.modelizer_next.json;

import java.io.File;
import java.io.IOException;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;

/**
 * Imports the current Modelizer Next JSON document format.
 */
public final class ModernModelizerImporter {

	/**
	 * Imports the file.
	 * @param file file to read or write
	 * @return the import file result
	 * @throws IOException if the operation cannot be completed
	 */
	public static ModelDocument importFile(final File file) throws IOException {
		return MNMain.OBJECT_MAPPER.readValue(file, ModelDocument.class);
	}

	/**
	 * Imports the string.
	 * @param file file to read or write
	 * @return the import string result
	 * @throws IOException if the operation cannot be completed
	 */
	public static ModelDocument importString(final String file) throws IOException {
		return MNMain.OBJECT_MAPPER.readValue(file, ModelDocument.class);
	}

	/**
	 * Creates a modern modelizer importer instance.
	 */
	private ModernModelizerImporter() {
	}
}
