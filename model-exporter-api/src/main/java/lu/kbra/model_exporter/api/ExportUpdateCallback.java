package lu.kbra.model_exporter.api;

public interface ExportUpdateCallback {

	ExportUpdateCallback createSubSection(String name);

	String getName();

	void setProgress(float percentage);

	float getProgress();

	/**
	 * @return returns the parent
	 */
	ExportUpdateCallback endSubSection();

	ExportUpdateCallback getParent();

	/**
	 * @return parent == 0
	 */
	int getDepth();

}
