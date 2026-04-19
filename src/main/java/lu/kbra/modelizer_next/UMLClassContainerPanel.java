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

public abstract class UMLClassContainerPanel extends ZoomPanPanel {

	public static final Border NORMAL_CLASS_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(3, 3, 3, 3));
	public static final Border FOCUS_CLASS_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLUE, 2), BorderFactory.createEmptyBorder(2, 2, 2, 2));
	public static final Border NORMAL_FIELD_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2));
	public static final Border FOCUS_FIELD_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLUE, 2), BorderFactory.createEmptyBorder(1, 1, 1, 1));

	public UMLClassContainerPanel() {
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

			final Point2D model = toModel(e.getPoint());
			dragged = null;

			final Component top = findTopPanel(model);
			if (!(top instanceof ConceptualClassPanel panel)) {
				requestFocusInWindow();
				return;
			}

			final Component deepest = findDeepest(panel, model);

			final boolean dragAllowed = deepest == panel || deepest == panel.getTitle();

			if (!dragAllowed) {
				return;
			}

			dragged = panel;
			offset = new Point2D.Double(model.getX() - panel.getX(), model.getY() - panel.getY());

			panel.requestFocusInWindow();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			final Point2D model = toModel(e.getPoint());

			final Component top = findTopPanel(model);
			if (!(top instanceof ConceptualClassPanel panel)) {
				return;
			}

			final Component deepest = findDeepest(panel, model);

			if (deepest == null || deepest == panel || deepest == panel.getTitle()) {
				panel.requestFocusInWindow();
				return;
			} else {
				deepest.requestFocusInWindow();
				panel.pack();
				repaint();
			}
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
			if (dragged != null && offset != null) {
				final Point2D mouseModel = toModel(e.getPoint());

				final int newX = (int) (mouseModel.getX() - offset.getX());
				final int newY = (int) (mouseModel.getY() - offset.getY());

				((ConceptualClassPanel) dragged).getModel().setPosition(new Point2D.Float(newX, newY));
//				((ConceptualClassPanel) dragged).updateModel();
				dragged.setLocation(newX, newY);
				
				repaint();
			}

			dragged = null;
			offset = null;
		}

	}

	public Point2D toModel(Point p) {
		return new Point2D.Double((p.x - translateX) / scale, (p.y - translateY) / scale);
	}

	private Component findTopPanel(Point2D model) {
		final Component[] comps = getComponents();
		for (int i = comps.length - 1; i >= 0; i--) {
			final Component c = comps[i];
			if (c instanceof ConceptualClassPanel panel && panel.getBounds().contains(model)) {
				return panel;
			}
		}
		return null;
	}

	private Component findDeepest(Component panel, Point2D model) {
		final int x = (int) (model.getX() - panel.getX());
		final int y = (int) (model.getY() - panel.getY());
		return SwingUtilities.getDeepestComponentAt(panel, x, y);
	}

}
