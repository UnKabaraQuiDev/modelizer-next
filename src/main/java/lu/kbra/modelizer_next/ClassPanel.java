package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class ClassPanel extends ZoomPanPanel {

	public static final Border NORMAL_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(3, 3, 3, 3));
	public static final Border FOCUS_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLUE, 2), BorderFactory.createEmptyBorder(2, 2, 2, 2));

	protected JPanel focusedPanel;

	public ClassPanel() {
		final ClassDragListener drag = new ClassDragListener();
		super.addMouseListener(drag);
		super.addMouseMotionListener(drag);
	}

	private class ClassDragListener extends MouseAdapter {

		private Point2D offset; // model-space offset
		private JPanel dragged;

		@Override
		public void mousePressed(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			final Point2D mouseModel = toModel(e.getPoint());

			dragged = null;

			for (Component c : getComponents()) {
				if (c instanceof JPanel panel && panel.getBounds().contains(mouseModel)) {
					dragged = panel;
					offset = new Point2D.Double(mouseModel.getX() - panel.getX(), mouseModel.getY() - panel.getY());
					break;
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			final Point2D mouseModel = toModel(e.getPoint());

			JPanel newFocus = null;
			for (Component c : getComponents()) {
				if (c instanceof JPanel panel && panel.getBounds().contains(mouseModel)) {
//						panel.requestFocusInWindow();
//					panel.setBorder(FOCUS_BORDER);
					newFocus = panel;
					break;
				}
			}
			if (newFocus == null) {
				requestFocusInWindow();
			} else {
				newFocus.requestFocusInWindow();
			}
//			if (newFocus == null && focusedPanel != null) {
//				focusedPanel.setBorder(NORMAL_BORDER);
//			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragged == null) {
				return;
			}

			final Point2D mouseModel = toModel(e.getPoint());

			final int newX = (int) (mouseModel.getX() - offset.getX());
			final int newY = (int) (mouseModel.getY() - offset.getY());

			dragged.setLocation(newX, newY);
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dragged = null;
			offset = null;
		}

		private Point2D toModel(Point p) {
			return new Point2D.Double((p.x - translateX) / scale, (p.y - translateY) / scale);
		}
	}
}
