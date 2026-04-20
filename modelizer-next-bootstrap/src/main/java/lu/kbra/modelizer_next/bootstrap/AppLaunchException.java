package lu.kbra.modelizer_next.bootstrap;

final class AppLaunchException extends Exception {

	private static final long serialVersionUID = 1L;

	AppLaunchException(final String message) {
		super(message);
	}

	AppLaunchException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
