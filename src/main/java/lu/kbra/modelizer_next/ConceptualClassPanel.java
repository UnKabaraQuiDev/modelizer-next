package lu.kbra.modelizer_next;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class ConceptualClassPanel extends JPanel {

	public class RenameFieldListener extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (currentField == null) {
				System.err.println("rename class");
			} else {
				currentField.getModel().setName("qsd");
				currentField.updateModel();
				getParent().repaint();
			}
		}

	}

	private class BorderFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			ConceptualClassPanel.this.setBorder(UMLClassContainerPanel.FOCUS_BORDER);
			getParent().repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			ConceptualClassPanel.this.setBorder(UMLClassContainerPanel.NORMAL_BORDER);
			getParent().repaint();
		}
	}

	private class NewFieldListener extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			final UMLClass obj = model.get();
			if (obj != null) {
				final UMLField field = obj.createField();
			}
			updateModel();
		}

	}

	protected WeakReference<UMLClass> model;

	protected JLabel title;
	protected List<ConceptualFieldLabel> fieldLabels = new ArrayList<>();

	public ConceptualClassPanel(UMLClass model) {
		this.model = new WeakReference<>(model);

		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		super.setLocation((int) model.getPosition().getX(), (int) model.getPosition().getY());
		super.setBorder(UMLClassContainerPanel.NORMAL_BORDER);
		super.setFocusable(true);

		title = new JLabel(model.getConceptualName());
		title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
		super.add(title);

		super.addFocusListener(new BorderFocusListener());

		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.err.println(e);
				System.err.println(e.getComponent());
			}
		});

		final InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "ctrlF");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");

		actionMap.put("ctrlF", new NewFieldListener());
		actionMap.put("F2", new RenameFieldListener());

		for (UMLField field : model.getFields()) {
			final ConceptualFieldLabel f = field.asConceptualLabel();
			fieldLabels.add(f);
			this.add(f);
		}

		pack();
	}

	public void updateModel() {
		final UMLClass obj = model.get();
		if (obj == null) {
			System.err.println("Model got OOS, removing " + toString());
			getParent().remove(this);
			return;
		}

		title.setText(obj.conceptualName);

		for (int i = 0; i < obj.fields.size(); i++) {
			final UMLField f = obj.fields.get(i);
			if (i < fieldLabels.size()) {
				final ConceptualFieldLabel label = fieldLabels.get(i);
				if (label.getModel() != f) {
					label.setModel(f);
				}
			} else {
				final ConceptualFieldLabel nLabel = f.asConceptualLabel();
				fieldLabels.add(nLabel);
				this.add(nLabel);
			}
		}

		while (fieldLabels.size() > obj.fields.size()) {
			this.remove(fieldLabels.remove(fieldLabels.size() - 1));
		}

		setLocation((int) obj.getPosition().getX(), (int) obj.getPosition().getY());
		pack();

		getParent().repaint();
	}

	public void pack() {
		validate();
		this.setSize(this.getPreferredSize());
	}

	public JLabel getTitle() {
		return title;
	}

	public UMLClass getModel() {
		return model.get();
	}

	public void setModel(UMLClass model) {
		this.model = new WeakReference<>(model);
	}

	@Override
	public String toString() {
		return "ConceptualPanel@" + System.identityHashCode(this) + " [model=" + model + "]";
	}

	protected ConceptualFieldLabel currentField;

	public void onClick(Component c) {
		this.requestFocusInWindow();
//		if (c == this || c == title) {
//			return;
//		}
		if (c instanceof ConceptualFieldLabel f) {
			currentField = f;
//			c.requestFocusInWindow();
		}
	}

//	private static class DragListener extends MouseAdapter {
//
//		private final JPanel panel;
//		private Point offset;
//
//		public DragListener(JPanel panel) {
//			this.panel = panel;
//		}
//
//		@Override
//		public void mousePressed(MouseEvent e) {
//			offset = e.getPoint();
//		}
//
//		@Override
//		public void mouseDragged(MouseEvent e) {
//			final Point parentPoint = SwingUtilities.convertPoint(panel, e.getPoint(), panel.getParent());
//			final int newX = parentPoint.x - offset.x;
//			final int newY = parentPoint.y - offset.y;
//			panel.setLocation(newX, newY);
//		}
//	}

}
