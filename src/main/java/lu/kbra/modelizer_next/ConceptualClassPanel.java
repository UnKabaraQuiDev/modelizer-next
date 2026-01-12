package lu.kbra.modelizer_next;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConceptualClassPanel extends JPanel {

	protected WeakReference<UMLClass> model;

	protected JLabel title;
	protected List<ConceptualFieldLabel> fieldLabels = new ArrayList<>();

	public ConceptualClassPanel(UMLClass model) {
		this.model = new WeakReference<>(model);

		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		super.setLocation((int) model.getPosition().getX(), (int) model.getPosition().getY());
		super.setBorder(ClassPanel.NORMAL_BORDER);
		super.setFocusable(true);

		title = new JLabel(model.getConceptualName());
		super.add(title);

//		super.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				requestFocusInWindow();
//			}
//		});

		super.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				ConceptualClassPanel.this.setBorder(ClassPanel.FOCUS_BORDER);
				getParent().repaint();
			}

			@Override
			public void focusLost(FocusEvent e) {
				ConceptualClassPanel.this.setBorder(ClassPanel.NORMAL_BORDER);
				getParent().repaint();
			}
		});

//		final DragListener drag = new DragListener(this);
//		title.addMouseListener(drag);
//		title.addMouseMotionListener(drag);

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

		while (obj.fields.size() > fieldLabels.size()) {
			this.remove(fieldLabels.remove(fieldLabels.size() - 1));
		}

		setLocation((int) obj.getPosition().getX(), (int) obj.getPosition().getY());
		pack();

//		repaint();
	}

	public void pack() {
		this.setSize(this.getPreferredSize());
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
