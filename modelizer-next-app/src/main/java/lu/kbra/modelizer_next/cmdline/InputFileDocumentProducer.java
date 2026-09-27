package lu.kbra.modelizer_next.cmdline;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lu.kbra.modelizer_next.ui.frame.DocumentSession;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

/**
 * Represents an input file document producer in the command-line export part of the application.
 */
@Getter
final class InputFileDocumentProducer implements ModelDocumentProducer {

	private final Iterator<URI> inputFiles;
	private final ConsoleDocumentLoadHandler loadHandler;
	private final int expectedCount;

	/**
	 * Creates an input file document producer instance.
	 *
	 * @param inputFiles values for input files
	 * @param force      whether force is enabled
	 */
	InputFileDocumentProducer(final List<URI> inputFiles, final boolean force) {
		this.expectedCount = inputFiles.size();
		this.inputFiles = inputFiles.iterator();
		this.loadHandler = new ConsoleDocumentLoadHandler(force);
	}

	/**
	 * Returns the next value from this producer or iterator.
	 *
	 * @return an optional result when a matching value is available
	 * @throws IOException if the operation cannot be completed
	 */
	@Override
	public Optional<LoadedDocument> next() throws IOException {
		if (!this.inputFiles.hasNext()) {
			return Optional.empty();
		}

		final URI inputFile = this.inputFiles.next();
		final Optional<DocumentSession> session = MainFrame.createDocument(inputFile, this.loadHandler);

		if (session.isEmpty()) {
			throw new ExportAbortedException(inputFile);
		}

		return Optional.of(new LoadedDocument(inputFile, session.get().getDocument()));
	}

}