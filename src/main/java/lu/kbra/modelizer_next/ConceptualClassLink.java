package lu.kbra.modelizer_next;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;

public class ConceptualClassLink extends JComponent {

	protected WeakReference<UMLLogicalLink> model;

	protected WeakReference<Component> fromComponent;
	protected WeakReference<Component> toComponent;

	public ConceptualClassLink(UMLLogicalLink model, Component fromComponent, Component toComponent) {
		this.model = new WeakReference<>(model);
		this.fromComponent = new WeakReference<>(fromComponent);
		this.toComponent = new WeakReference<>(toComponent);
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;

		final UMLLink obj = model.get();
		if (obj == null) {
			System.err.println("Model got OOS, removing " + toString());
			getParent().remove(this);
			return;
		}

		// Midpoint
		double mx = (x1 + x2) / 2.0;
		double my = (y1 + y2) / 2.0;

		// Angle of the line
		double angle = Math.atan2(y2 - y1, x2 - x1);

		// Save transform
		AffineTransform old = g2.getTransform();

		// Move to midpoint and rotate
		g2.translate(mx, my);
		g2.rotate(angle);

		// Center text on line
		FontMetrics fm = g2.getFontMetrics();
		int textWidth = fm.stringWidth(text);
		int textHeight = fm.getAscent();

		g2.drawString(text, -textWidth / 2, -2);

		// Restore transform
		g2.setTransform(old);
	}

	public UMLLogicalLink getModel() {
		return model.get();
	}

	public void setModel(UMLLogicalLink l) {
		this.model = new WeakReference<>(l);
	}

	public Component getFromComponent() {
		return fromComponent.get();
	}

	public void setFromComponent(Component l) {
		this.fromComponent = new WeakReference<>(l);
	}

	public Component getToComponent() {
		return toComponent.get();
	}

	public void setToComponent(Component l) {
		this.toComponent = new WeakReference<>(l);
	}

	public WeakReference<UMLLogicalLink> getModelRef() {
		return model;
	}

	public void setModelRef(WeakReference<UMLLogicalLink> model) {
		this.model = model;
	}

	public WeakReference<Component> getFromComponentRef() {
		return fromComponent;
	}

	public void setFromComponentRef(WeakReference<Component> fromComponent) {
		this.fromComponent = fromComponent;
	}

	public WeakReference<Component> getToComponentRef() {
		return toComponent;
	}

	public void setToComponentRef(WeakReference<Component> toComponent) {
		this.toComponent = toComponent;
	}

	@Override
	public String toString() {
		return "ConceptualClassLink@" + System.identityHashCode(this) + " [model=" + model + ", fromComponent="
				+ fromComponent + ", toComponent=" + toComponent + "]";
	}

}
