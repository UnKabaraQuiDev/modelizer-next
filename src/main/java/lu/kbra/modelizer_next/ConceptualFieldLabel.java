package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.ref.WeakReference;

import javax.swing.JLabel;

public class ConceptualFieldLabel extends JLabel {

	protected WeakReference<UMLField> model;

	public ConceptualFieldLabel(UMLField model) {
		this.model = new WeakReference<>(model);
		
		super.addFocusListener(new BorderFocusListener());
		super.setText(model.getName());
		super.setFocusable(true);
	}

	public void updateModel() {
		final UMLField obj = model.get();
		if (obj == null) {
			System.err.println("Model got OOS, removing " + toString());
			getParent().remove(this);
			return;
		}
		this.setText(obj.name);
	}

	public UMLField getModel() {
		return model.get();
	}

	public void setModel(UMLField model) {
		this.model = new WeakReference<>(model);
	}

	@Override
	public String toString() {
		return "ConceptualLabel@" + System.identityHashCode(this) + " [model=" + model + "]";
	}

	public class BorderFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
//			ConceptualFieldLabel.this.setBorder(UMLClassContainerPanel.FOCUS_BORDER);
			ConceptualFieldLabel.this.setBackground(Color.LIGHT_GRAY);
			getParent().repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
//			ConceptualFieldLabel.this.setBorder(UMLClassContainerPanel.NORMAL_BORDER);
			getParent().repaint();
		}
	}

	public WeakReference<UMLField> getModelRef() {
		return model;
	}

}
