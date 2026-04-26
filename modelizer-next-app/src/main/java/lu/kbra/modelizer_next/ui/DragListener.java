package lu.kbra.modelizer_next.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class DragListener extends MouseAdapter {

	private final JPanel parent;
	private Component dragged;

	public DragListener(final JPanel parent) {
		this.parent = parent;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (this.dragged == null) {
			return;
		}

		final Point pt = SwingUtilities.convertPoint(this.dragged, e.getPoint(), this.parent);
		int targetIndex = -1;
		if (e.getPoint().getX() <= this.parent.getX()) {
			targetIndex = 0;
		} else {
			for (int i = 0; i < this.parent.getComponentCount(); i++) {
				final Component comp = this.parent.getComponent(i);
				if (comp == this.dragged) {
					continue;
				}
				final Rectangle bounds = comp.getBounds();
				if (pt.x < bounds.x + bounds.width / 2) {
					targetIndex = i;
					break;
				}
			}
		}

		if (targetIndex == -1) {
			targetIndex = this.parent.getComponentCount() - 1;
			return;
		}

		this.parent.remove(this.dragged);
		this.parent.add(this.dragged, targetIndex);
		this.parent.revalidate();
		this.parent.repaint();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		this.dragged = e.getComponent();
		this.dragged.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		this.dragged.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.dragged = null;
		((Runnable) this.parent.getClientProperty("savePalettes")).run();
	}

}
