package lu.kbra.modelizer_next;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;

public class ConceptualFieldLabel extends JLabel {

	protected WeakReference<UMLField> model;

	public ConceptualFieldLabel(UMLField model) {
		this.model = new WeakReference<>(model);

		this.setText(model.getName());
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

}
