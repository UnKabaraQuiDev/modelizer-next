package lu.kbra.modelizer_next.bootstrap;

/**
 * Contract implemented by applications that can be launched by the bootstrap runtime.
 */
public interface AppMain {

	/**
	 * Starts the application entry point.
	 *
	 * @param args command-line arguments supplied by the launcher
	 */
	void start(String[] args);

}
