package lu.kbra.modelizer_next.common;

/**
 * Exception thrown when the app is started by an incompatible bootstrap runtime.
 */
public class UnsupportedBootstrapVersionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an unsupported bootstrap version exception instance.
	 */
	public UnsupportedBootstrapVersionException() {
	}

	/**
	 * Creates an unsupported bootstrap version exception instance.
	 * @param message message shown to the caller or user
	 * @param cause cause to attach to the created exception
	 * @param enableSuppression whether enable suppression is enabled
	 * @param writableStackTrace whether writable stack trace is enabled
	 */
	public UnsupportedBootstrapVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Creates an unsupported bootstrap version exception instance.
	 * @param message message shown to the caller or user
	 * @param cause cause to attach to the created exception
	 */
	public UnsupportedBootstrapVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an unsupported bootstrap version exception instance.
	 * @param message message shown to the caller or user
	 */
	public UnsupportedBootstrapVersionException(String message) {
		super(message);
	}

	/**
	 * Creates an unsupported bootstrap version exception instance.
	 * @param cause cause to attach to the created exception
	 */
	public UnsupportedBootstrapVersionException(Throwable cause) {
		super(cause);
	}

}
