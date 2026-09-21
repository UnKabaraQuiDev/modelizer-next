package lu.kbra.modelizer_next.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import lu.kbra.model_exporter.api.ExportUpdateCallback;

import lombok.Getter;

@Getter
public class DefaultExportUpdateCallback implements ExportUpdateCallback {

	private final DefaultExportUpdateCallback parent;
	private final String name;
	private final int depth;
	private final ExportSectionListener listener;
	private String endMessage;

	/*
	 * Only accessed while holding childrenLock.
	 */
	private final List<DefaultExportUpdateCallback> children = new ArrayList<>();

	private final Object childrenLock = new Object();

	private final AtomicBoolean closed = new AtomicBoolean(false);

	/*
	 * Volatile because these values may be read/written by different threads without needing to lock
	 * the tree.
	 */
	private volatile float progress;
	private volatile boolean aggregateChildProgress;

	/**
	 * Creates the root/main section.
	 */
	public static DefaultExportUpdateCallback create(final ExportSectionListener listener) {
		return new DefaultExportUpdateCallback(null, "main", Objects.requireNonNull(listener));
	}

	private DefaultExportUpdateCallback(final DefaultExportUpdateCallback parent, final String name, final ExportSectionListener listener) {
		this.parent = parent;
		this.name = Objects.requireNonNull(name);
		this.listener = listener;
		this.depth = parent != null ? parent.getDepth() + 1 : 0;
	}

	@Override
	public ExportUpdateCallback createSubSection(final String name) {
		Objects.requireNonNull(name, "name");

		final DefaultExportUpdateCallback child;

		synchronized (this.childrenLock) {
			this.ensureOpen();

			child = new DefaultExportUpdateCallback(this, name, this.listener);

			this.children.add(child);
		}

		/*
		 * Do not invoke user code while holding childrenLock.
		 */
		this.listener.sectionCreated(this, child);

		return child;
	}

	@Override
	public void setProgress(final float percentage) {
		if (percentage < 0.0f || percentage > 100.0f) {
			throw new IllegalArgumentException("percentage must be between 0 and 100");
		}

		this.ensureOpen();

		this.progress = percentage;

		this.listener.progressUpdated(this.parent, this);
	}

	@Override
	public ExportUpdateCallback endSubSection(String endMessage) {
		/*
		 * close() is idempotent. This prevents two threads from deleting the same section twice.
		 */
		if (!this.closed.compareAndSet(false, true)) {
			return this.parent;
		}

		this.endMessage = endMessage;

		/*
		 * The root has no parent. Ending it closes the entire transaction.
		 */
		if (this.parent == null) {
			this.listener.sectionDeleted(null, this);
			return null;
		}

		/*
		 * Remove this section from its parent.
		 */
		this.parent.removeChild(this);

		/*
		 * The callback happens after the tree has been updated.
		 */
		this.listener.sectionDeleted(this.parent, this);

		return this.parent;
	}

	private void removeChild(final DefaultExportUpdateCallback child) {
		synchronized (this.childrenLock) {
			this.children.remove(child);
		}
	}

	private void ensureOpen() {
		if (this.closed.get()) {
			throw new IllegalStateException("Section '" + this.name + "' has already been closed");
		}
	}

	/**
	 * Optional helper if callers need to inspect the current children.
	 */
	public List<ExportUpdateCallback> getChildren() {
		synchronized (this.childrenLock) {
			return List.copyOf(this.children);
		}
	}

}
