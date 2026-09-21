package lu.kbra.model_exporter.api;

public class ExportFailedException extends Exception {

	private static final long serialVersionUID = -87194428997063258L;

	public ExportFailedException() {
	}

	public ExportFailedException(
			final String message,
			final Throwable cause,
			final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExportFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ExportFailedException(final String message) {
		super(message);
	}

	public ExportFailedException(final Throwable cause) {
		super(cause);
	}

}
