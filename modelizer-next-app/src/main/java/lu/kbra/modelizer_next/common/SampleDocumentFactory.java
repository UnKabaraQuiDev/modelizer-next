package lu.kbra.modelizer_next.common;

import java.io.IOException;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.json.ModernModelizerImporter;
import lu.kbra.pclib.PCUtils;

/**
 * Factory for the built-in sample document shown when the user creates or opens an example
 * document.
 */
public final class SampleDocumentFactory {

	public static final String META_NAME = "Demo model";

	/**
	 * Creates a new default instance for this factory.
	 *
	 * @return the created value
	 */
	public static ModelDocument create() {
		try {
			final ModelDocument md = ModernModelizerImporter.importString(PCUtils.readPackagedStringFile(MNMain.class, "/sample.mn"));
			md.getMeta().setName(SampleDocumentFactory.META_NAME);
			return md;
		} catch (final IOException e) {
			return null;
		}
	}

	/**
	 * Creates a sample document factory instance.
	 */
	private SampleDocumentFactory() {
	}

}
