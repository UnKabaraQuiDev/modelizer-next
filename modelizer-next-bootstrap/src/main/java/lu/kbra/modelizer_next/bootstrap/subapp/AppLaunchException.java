package lu.kbra.modelizer_next.bootstrap.subapp;

/**
 * Exception thrown when an installed application cannot be launched.
 */
public final class AppLaunchException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an application launch exception instance.
	 *
	 * @param message message shown to the caller or user
	 */
	public AppLaunchException(final String message) {
		super(message);
	}

	/**
	 * Creates an application launch exception instance.
	 *
	 * @param message message shown to the caller or user
	 * @param cause   cause to attach to the created exception
	 */
	public AppLaunchException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
