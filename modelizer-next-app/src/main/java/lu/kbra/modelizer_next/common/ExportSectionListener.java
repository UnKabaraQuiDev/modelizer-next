package lu.kbra.modelizer_next.common;

import lu.kbra.model_exporter.api.ExportUpdateCallback;

public interface ExportSectionListener {

	/**
	 * Called after a section has been created.
	 */
	void sectionCreated(ExportUpdateCallback parent, ExportUpdateCallback section);

	/**
	 * Called when a section is closed/deleted. This is called for sections at any depth.
	 */
	void sectionDeleted(ExportUpdateCallback parent, ExportUpdateCallback section);

	/**
	 * Called when the main/root section is closed.
	 */
	void progressUpdated(ExportUpdateCallback parent, ExportUpdateCallback section);

}
