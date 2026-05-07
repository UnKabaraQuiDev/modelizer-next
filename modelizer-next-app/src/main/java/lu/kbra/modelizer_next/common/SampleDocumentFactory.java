package lu.kbra.modelizer_next.common;

import java.io.IOException;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.json.ModernModelizerImporter;
import lu.kbra.pclib.PCUtils;

public final class SampleDocumentFactory {

	public static final String META_NAME = "Demo model";

	public static ModelDocument create() {
		try {
			return ModernModelizerImporter.importString(PCUtils.readPackagedStringFile(SampleDocumentFactory.class, "/sample.mn"));
		} catch (final IOException e) {
			return null;
		}
	}

	private SampleDocumentFactory() {
	}

}
