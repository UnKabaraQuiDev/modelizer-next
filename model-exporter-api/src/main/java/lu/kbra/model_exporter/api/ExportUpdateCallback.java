package lu.kbra.model_exporter.api;

public interface ExportUpdateCallback {

	ExportUpdateCallback createSubSection(String name);

	String getName();

	String getEndMessage();

	void setProgress(float percentage);

	float getProgress();

	/**
	 * @return returns the parent
	 */
	ExportUpdateCallback endSubSection(String message);

	ExportUpdateCallback getParent();

	int getExpectedChildCount();

	int getDoneChildCount();

	boolean isAggregateChildProgress();

	void setExpectedChildCount(int expectedChildCount);

	void setAggregateChildProgress(boolean aggregateChildProgress);

	/**
	 * @return parent == 0
	 */
	int getDepth();

}
