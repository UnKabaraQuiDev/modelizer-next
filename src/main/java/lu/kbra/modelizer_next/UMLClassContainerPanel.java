package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class UMLClassContainerPanel extends ZoomPanPanel {

	public static final Border NORMAL_BORDER = BorderFactory
			.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(3, 3, 3, 3));
	public static final Border FOCUS_BORDER = BorderFactory
			.createCompoundBorder(BorderFactory.createLineBorder(Color.BLUE, 2), BorderFactory.createEmptyBorder(2, 2, 2, 2));

//	protected List<Component> components = new ArrayList<>();

	public UMLClassContainerPanel() {
		final ClassDragListener drag = new ClassDragListener();
		super.addMouseListener(drag);
		super.addMouseMotionListener(drag);
	}

//	@Override
//	protected void paintChildren(Graphics g) {
//		components.forEach(p -> p.paint(g));
//	}

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

			Point2D model = toModel(e.getPoint());

			Component top = findTopPanel(model);
			if (!(top instanceof ConceptualClassPanel panel)) {
				return;
			}

			Component deepest = findDeepest(panel, model);

			if (deepest == null || deepest == panel || deepest == panel.getTitle()) {
				return;
			}

			panel.onClick(deepest);
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
				((ConceptualClassPanel) dragged).updateModel();
//			dragged.setLocation(newX, newY);
//			repaint();
			}

			dragged = null;
			offset = null;
		}

	}

	public Point2D toModel(Point p) {
		return new Point2D.Double((p.x - translateX) / scale, (p.y - translateY) / scale);
	}

	private Component findTopPanel(Point2D model) {
		Component[] comps = getComponents();
		for (int i = comps.length - 1; i >= 0; i--) {
			Component c = comps[i];
			if (c instanceof ConceptualClassPanel panel && panel.getBounds().contains(model)) {
				return panel;
			}
		}
		return null;
	}

	private Component findDeepest(Component panel, Point2D model) {
		int x = (int) (model.getX() - panel.getX());
		int y = (int) (model.getY() - panel.getY());
		return SwingUtilities.getDeepestComponentAt(panel, x, y);
	}

//	@Override
//	public Component add(Component comp, int index) {
//		// TODO Auto-generated method stub
//		return super.add(comp, index);
//	}
//
//	@Override
//	public void add(Component comp, Object constraints, int index) {
//		components.add(index, comp);
//	}
//
//	@Override
//	public void add(Component comp, Object constraints) {
//		components.add(comp);
//	}
//
//	@Override
//	public Component add(Component comp) {
//		components.add(comp);
//		return comp;
//	}
//
//	@Override
//	public Component add(String name, Component comp) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	protected void addImpl(Component comp, Object constraints, int index) {
//		components.add(index, comp);
//	}

}
