package lu.kbra.modelizer_next.common;

public class UnsupportedBootstrapVersionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnsupportedBootstrapVersionException() {
	}

	public UnsupportedBootstrapVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedBootstrapVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedBootstrapVersionException(String message) {
		super(message);
	}

	public UnsupportedBootstrapVersionException(Throwable cause) {
		super(cause);
	}

}
