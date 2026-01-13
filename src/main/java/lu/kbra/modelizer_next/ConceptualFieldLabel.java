package lu.kbra.modelizer_next;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class ConceptualFieldLabel extends JLabel implements UMLClassChild {

	private class RenameFieldListener extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			final String value = JOptionPane.showInputDialog(ConceptualFieldLabel.this, "Enter name:",
					model.get().getConceptualName());
			if (value != null) {
				System.out.println("User entered: " + value);
				model.get().setConceptualName(value);
				updateModel();
				getParent().pack();
				forceRedraw();
			}
		}

	}

	private class BorderFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			ConceptualFieldLabel.this.setBorder(UMLClassContainerPanel.FOCUS_FIELD_BORDER);
			forceRedraw();
		}

		@Override
		public void focusLost(FocusEvent e) {
			ConceptualFieldLabel.this.setBorder(UMLClassContainerPanel.NORMAL_FIELD_BORDER);
			forceRedraw();
		}
	}

	protected WeakReference<UMLField> model;

	public ConceptualFieldLabel(UMLField model) {
		this.model = new WeakReference<>(model);

		super.addFocusListener(new BorderFocusListener());
		super.setText(model.getConceptualName());
		super.setBorder(UMLClassContainerPanel.NORMAL_FIELD_BORDER);
		super.setFocusable(true);
		super.addFocusListener(new BorderFocusListener());

		final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "ctrlF");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");

		actionMap.put("F2", new RenameFieldListener());
	}

	public void updateModel() {
		final UMLField obj = model.get();
		if (obj == null) {
			System.err.println("Model got OOS, removing " + toString());
			getParent().remove(this);
			return;
		}
		this.setText(obj.conceptualName);
	}

	@Override
	public ConceptualClassPanel getParent() {
		return (ConceptualClassPanel) super.getParent();
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

	public WeakReference<UMLField> getModelRef() {
		return model;
	}

}
