package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.app.Docking;

/**
 * Dockable Swing panel that wraps a diagram canvas.
 */
final class DockableDiagramPanel extends JPanel implements Dockable {
	private static final long serialVersionUID = 1L;

	private final String persistentID;
	private final String tabText;

	/**
	 * Creates a dockable diagram panel instance.
	 * @param persistentID id of the element to read or modify
	 * @param tabText text value for tab text
	 * @param canvas canvas instance that owns the operation
	 * @param activate activate value used by the operation
	 */
	DockableDiagramPanel(final String persistentID, final String tabText, final DiagramCanvas canvas, final Runnable activate) {
		super(new BorderLayout());
		this.persistentID = persistentID;
		this.tabText = tabText;
		this.add(canvas, BorderLayout.CENTER);

		canvas.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mousePressed(final MouseEvent event) {
				activate.run();
			}
		});
		canvas.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(final FocusEvent event) {
				activate.run();
			}
		});
		this.addHierarchyListener(event -> {
			if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && this.isShowing()) {
				activate.run();
			}
		});

		Docking.registerDockable(this);
	}

	/**
	 * Returns the persistent ID.
	 * @return the persistent ID
	 */
	@Override
	public String getPersistentID() {
		return this.persistentID;
	}

	/**
	 * Returns the tab text.
	 * @return the tab text
	 */
	@Override
	public String getTabText() {
		return this.tabText;
	}

	/**
	 * Checks whether closable is enabled or applies.
	 * @return {@code true} if closable is enabled or applies; otherwise {@code false}
	 */
	@Override
	public boolean isClosable() {
		return false;
	}

	/**
	 * Checks whether wrappable in scrollpane is enabled or applies.
	 * @return {@code true} if wrappable in scrollpane is enabled or applies; otherwise {@code false}
	 */
	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

}
