package lu.kbra.modelizer_next.bootstrap;

@FunctionalInterface
interface ProgressListener {
	void onProgress(String message, int value, int max);
}
