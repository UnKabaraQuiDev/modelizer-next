package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.app.Docking;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

final class DockableDiagramPanel extends JPanel implements Dockable {
	private static final long serialVersionUID = 1L;

	private final String persistentID;
	private final String tabText;

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

	@Override
	public String getPersistentID() {
		return this.persistentID;
	}

	@Override
	public String getTabText() {
		return this.tabText;
	}

	@Override
	public boolean isClosable() {
		return false;
	}

	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

}