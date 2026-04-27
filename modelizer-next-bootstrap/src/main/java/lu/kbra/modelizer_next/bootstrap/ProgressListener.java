package lu.kbra.modelizer_next.bootstrap;

@FunctionalInterface
public interface ProgressListener {
	public void onProgress(String message, int value, int max);
}
