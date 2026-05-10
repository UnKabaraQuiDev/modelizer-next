package lu.kbra.modelizer_next.bootstrap;

/**
 * Listener used by bootstrap tasks to report progress text and numeric progress values.
 */
@FunctionalInterface
public interface ProgressListener {
	/**
	 * Handles the progress event during bootstrap/update processing.
	 * @param message message shown to the caller or user
	 * @param value value to process
	 * @param max numeric max value
	 */
	public void onProgress(String message, int value, int max);
}
