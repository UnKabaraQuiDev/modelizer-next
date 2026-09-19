package lu.kbra.model_exporter.api;

public class ExportFailedException extends Exception {

	public ExportFailedException() {
	}

	public ExportFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExportFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportFailedException(String message) {
		super(message);
	}

	public ExportFailedException(Throwable cause) {
		super(cause);
	}

}
