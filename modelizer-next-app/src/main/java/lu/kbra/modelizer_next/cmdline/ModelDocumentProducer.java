package lu.kbra.modelizer_next.cmdline;

import java.io.IOException;
import java.util.Optional;

/**
 * Defines operations for model document producer behavior.
 */
interface ModelDocumentProducer {

	/**
	 * Returns the next value from this producer or iterator.
	 *
	 * @return an optional result when a matching value is available
	 * @throws IOException if the operation cannot be completed
	 */
	Optional<LoadedDocument> next() throws IOException;
	
	int getExpectedCount();

}