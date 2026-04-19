package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class ZoomPanPanel extends JPanel {

	protected double scale = 1.0;
	protected double translateX = 0;
	protected double translateY = 0;

	private final AffineTransform viewTransform = new AffineTransform();

	public ZoomPanPanel() {
		final ZoomPanDragListener drag = new ZoomPanDragListener();
		super.addMouseListener(drag);
		super.addMouseWheelListener(drag);
		super.addMouseMotionListener(drag);
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final AffineTransform old = g2.getTransform();

		viewTransform.setToIdentity();
		viewTransform.translate(translateX, translateY);
		viewTransform.scale(scale, scale);
		g2.setTransform(viewTransform);

		drawGrid(g2);

		paintChildren(g);
//		paintOverlay(g);

		g2.setTransform(old);
	}

//	protected abstract void paintOverlay(Graphics g);

	private void drawGrid(Graphics2D g2) {
		int gridSize = 50;
		int count = 100;

		g2.setColor(new Color(220, 220, 220));

		for (int i = -count; i <= count; i++) {
			int p = i * gridSize;

			g2.drawLine(p, -count * gridSize, p, count * gridSize);

			g2.drawLine(-count * gridSize, p, count * gridSize, p);
		}

		g2.setColor(Color.GRAY);
		g2.drawLine(-count * gridSize, 0, count * gridSize, 0);
		g2.drawLine(0, -count * gridSize, 0, count * gridSize);
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension d = super.getPreferredSize();
		return new Dimension((int) (d.width * scale + Math.abs(translateX)),
				(int) (d.height * scale + Math.abs(translateY)));
	}

	private class ZoomPanDragListener extends MouseAdapter {

		private Point offset;
		private boolean pan = false;

		@Override
		public void mousePressed(MouseEvent e) {
			offset = e.getPoint();
			pan |= SwingUtilities.isMiddleMouseButton(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (pan) {
				final Point p = e.getPoint();
				translateX += p.x - offset.x;
				translateY += p.y - offset.y;
				offset = null;
				pan = false;
				repaint();
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			final double oldScale = scale;
			final double factor = Consts.MOUSE_ZOOM_FACTOR;

			if (e.getWheelRotation() < 0) {
				scale *= factor;
			} else {
				scale /= factor;
			}
			scale = Math.max(Consts.MIN_ZOOM, Math.min(scale, Consts.MAX_ZOOM));

			final Point p = e.getPoint();

			translateX = p.x - (p.x - translateX) * (scale / oldScale);
			translateY = p.y - (p.y - translateY) * (scale / oldScale);

			revalidate();
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (pan) {
				final Point p = e.getPoint();
				translateX += p.x - offset.x;
				translateY += p.y - offset.y;
				offset = p;
				repaint();
			}
		}
	}

}
