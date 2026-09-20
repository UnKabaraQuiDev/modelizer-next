package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.modelizer_next.common.ExportSectionListener;

public final class ExportProgressDialog extends JDialog implements ExportSectionListener {

	private static final long serialVersionUID = 1886964503177147603L;

	private static final int PROGRESS_HEIGHT = 20;
	private static final int SECTION_INDENT = 20;

	private final JPanel sectionsPanel = new JPanel();

	private ExportUpdateCallback rootCallback;
	private SectionView rootView;

	/*
	 * IdentityHashMap is intentional. Callback instances identify sections.
	 */
	private final Map<ExportUpdateCallback, SectionView> sections = new IdentityHashMap<>();

	public ExportProgressDialog(final Window owner) {
		super(owner, "Exporting", ModalityType.MODELESS);

		this.sectionsPanel.setLayout(new BoxLayout(this.sectionsPanel, BoxLayout.Y_AXIS));

		this.sectionsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		final JScrollPane scrollPane = new JScrollPane(this.sectionsPanel);

		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(500, 350));

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.pack();
		this.setLocationRelativeTo(owner);
	}

	/**
	 * Creates the permanent root progress bar.
	 */
	public void setRoot(final ExportUpdateCallback root) {
		SwingUtilities.invokeLater(() -> {
			if (this.rootCallback != null) {
				return;
			}

			this.rootCallback = root;

			/*
			 * The root is always depth 0 and is never stored in the normal sections map.
			 */
			this.rootView = new SectionView(root, 0);

			this.sectionsPanel.add(this.rootView);

			this.sectionsPanel.revalidate();
			this.sectionsPanel.repaint();
		});
	}

	@Override
	public void sectionCreated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		SwingUtilities.invokeLater(() -> {
			final int depth = this.getDepth(parent) + 1;

			final SectionView view = new SectionView(section, depth);

			this.sections.put(section, view);

			/*
			 * Insert after the complete subtree of the parent.
			 */
			final int index = this.findInsertionIndex(parent);

			this.sectionsPanel.add(view, index);

			this.sectionsPanel.revalidate();
			this.sectionsPanel.repaint();

			this.scrollToBottom();
		});
	}

	@Override
	public void sectionDeleted(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		/*
		 * The root being deleted means the entire export has finished.
		 */
		if (parent == null) {
			SwingUtilities.invokeLater(this::dispose);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			final SectionView view = this.sections.get(section);

			if (view == null) {
				return;
			}

			/*
			 * IMPORTANT:
			 *
			 * Do NOT remove the section here.
			 *
			 * sectionDeleted means that the section's work has finished, not that its UI should disappear.
			 */
			view.setCompleted();
		});
	}

	@Override
	public void progressUpdated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		SwingUtilities.invokeLater(() -> {
			final SectionView view;

			if (section == this.rootCallback) {
				view = this.rootView;
			} else {
				view = this.sections.get(section);
			}

			if (view == null) {
				return;
			}

			view.setProgress(section.getProgress());
		});
	}

	/**
	 * Called when a section is deleted.
	 *
	 * The section itself stays visible, but when its parent is deleted, the parent's complete subtree
	 * is removed.
	 *
	 * Example:
	 *
	 * A B C
	 *
	 * B deleted -> B stays visible. C deleted -> C stays visible. A deleted -> A, B and C disappear.
	 */
	private void removeSubtree(final ExportUpdateCallback section) {
		final SectionView sectionView = this.sections.remove(section);

		if (sectionView != null) {
			this.sectionsPanel.remove(sectionView);
		}

		/*
		 * Find every remaining section whose ancestor is this section.
		 *
		 * Copy the entries first because we're modifying the map.
		 */
		final List<ExportUpdateCallback> descendants = new ArrayList<>();

		for (final ExportUpdateCallback callback : this.sections.keySet()) {

			if (this.isDescendant(callback, section)) {
				descendants.add(callback);
			}
		}

		for (final ExportUpdateCallback descendant : descendants) {
			final SectionView descendantView = this.sections.remove(descendant);

			if (descendantView != null) {
				this.sectionsPanel.remove(descendantView);
			}
		}
	}

	private boolean isDescendant(final ExportUpdateCallback callback, final ExportUpdateCallback ancestor) {
		ExportUpdateCallback current = callback.getParent();

		while (current != null) {
			if (current == ancestor) {
				return true;
			}

			current = current.getParent();
		}

		return false;
	}

	/**
	 * Returns the depth of a callback.
	 *
	 * Root = 0 Child = 1 Grandchild = 2
	 */
	private int getDepth(final ExportUpdateCallback callback) {

		int depth = 0;

		ExportUpdateCallback current = callback;

		while (current != null && current != this.rootCallback) {

			current = current.getParent();
			depth++;
		}

		return depth;
	}

	/**
	 * Finds the position after the complete subtree of the parent.
	 *
	 * Example:
	 *
	 * A B C D
	 *
	 * D is inserted after C.
	 */
	private int findInsertionIndex(final ExportUpdateCallback parent) {

		final SectionView parentView = parent == this.rootCallback ? this.rootView : this.sections.get(parent);

		if (parentView == null) {
			return this.sectionsPanel.getComponentCount();
		}

		final int parentIndex = this.sectionsPanel.getComponentZOrder(parentView);

		final int parentDepth = parentView.depth;

		int index = parentIndex + 1;

		while (index < this.sectionsPanel.getComponentCount()) {

			final SectionView view = (SectionView) this.sectionsPanel.getComponent(index);

			if (view.depth <= parentDepth) {
				break;
			}

			index++;
		}

		return index;
	}

	private void scrollToBottom() {
		/*
		 * This method is already called from the EDT. There is no need for another invokeLater().
		 */
		this.sectionsPanel.revalidate();

		if (this.sectionsPanel.getParent() instanceof final JViewport viewport) {

			viewport.setViewPosition(new Point(0, Math.max(0, this.sectionsPanel.getHeight() - viewport.getHeight())));
		}
	}

	private static final class SectionView extends JPanel {

		private static final long serialVersionUID = 1158958088846805634L;

		private final int depth;
		private final JProgressBar progressBar;

		SectionView(final ExportUpdateCallback section, final int depth) {

			this.depth = depth;

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.setBorder(BorderFactory.createEmptyBorder(5, depth * ExportProgressDialog.SECTION_INDENT, 5, 5));

			final JLabel label = new JLabel(section.getName());
			label.setAlignmentX(Component.LEFT_ALIGNMENT);

			this.progressBar = new JProgressBar(0, 100);
			this.progressBar.setStringPainted(true);

			/*
			 * Keep the progress bar from growing vertically.
			 */
			final Dimension minimumSize = new Dimension(50, ExportProgressDialog.PROGRESS_HEIGHT);
			final Dimension preferredSize = new Dimension(400, ExportProgressDialog.PROGRESS_HEIGHT);
			final Dimension maximumSize = new Dimension(Integer.MAX_VALUE, ExportProgressDialog.PROGRESS_HEIGHT);

			this.progressBar.setMinimumSize(minimumSize);
			this.progressBar.setPreferredSize(preferredSize);
			this.progressBar.setMaximumSize(maximumSize);

			this.progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

			this.progressBar.setValue(Math.round(section.getProgress()));

			this.add(label);
			this.add(Box.createVerticalStrut(3));
			this.add(this.progressBar);

			/*
			 * Prevent the whole SectionView from expanding vertically.
			 */
			this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height));
		}

		void setProgress(final float progress) {
			this.progressBar.setValue(Math.round(progress));
		}

		void setCompleted() {
			this.progressBar.setValue(100);
		}
	}

}
