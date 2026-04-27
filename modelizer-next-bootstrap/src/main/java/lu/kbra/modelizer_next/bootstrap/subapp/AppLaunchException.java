package lu.kbra.modelizer_next.bootstrap.subapp;

public final class AppLaunchException extends Exception {

	private static final long serialVersionUID = 1L;

	public AppLaunchException(final String message) {
		super(message);
	}

	public AppLaunchException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
