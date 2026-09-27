package lu.kbra.modelizer_next.cmdline;

import lu.kbra.model_exporter.api.ExportUpdateCallback;

public final class BatchProgressRenderer implements ProgressRenderer {

	@Override
	public synchronized void sectionCreated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		System.out.println("=".repeat(section.getDepth()) + " Started " + this.describe(section, false));
	}

	@Override
	public synchronized void progressUpdated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		// Do not log progress.
	}

	@Override
	public synchronized void sectionDeleted(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		System.out.println("=".repeat(section.getDepth()) + " Done " + this.describe(section, true));
	}

	private String describe(final ExportUpdateCallback section, final boolean ended) {
		if (section == null) {
			return "<root>";
		}

		final String name = (ended ? section.getEndMessage() == null ? section.getName() : section.getEndMessage() : section.getName());

		if (name == null || name.isEmpty()) {
			return "<unnamed>";
		}

		return name;
	}

	@Override
	public void close() {
		// Nothing to close.
	}

}
