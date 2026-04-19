package lu.kbra.modelizer_next;

import java.awt.Graphics;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConceptualPanel extends UMLClassContainerPanel {

	protected WeakReference<UMLFile> file;

	protected List<ConceptualClassPanel> classes = new ArrayList<>();
	protected List<ConceptualClassLink> links = new ArrayList<>();

	public ConceptualPanel(UMLFile file) {
		this.file = new WeakReference<>(file);
		file.classes.stream().map(UMLClass::asConceptualPanel).forEach(panel -> {
			this.classes.add(panel);
			this.add(panel);
		});

		setLayout(null);
	}

	public Optional<ConceptualClassPanel> getUMLClassPanel(UMLClass c) {
		return classes.stream().filter(b -> b.getModel() == c).findFirst();
	}

	public void updateModel() {
		final UMLFile obj = file.get();

		for (int i = 0; i < obj.classes.size(); i++) {
			final UMLClass c = obj.classes.get(i);
			if (i < classes.size()) {
				final ConceptualClassPanel panel = classes.get(i);
				if (panel.getModel() != c) {
					panel.setModel(c);
					panel.updateModel();
				}
			} else {
				final ConceptualClassPanel nPanel = c.asConceptualPanel();
				classes.add(nPanel);
				this.add(nPanel);
			}
		}

		while (classes.size() > obj.classes.size()) {
			this.remove(classes.remove(classes.size() - 1));
		}
		
		for (int i = 0; i < obj.logicalLinks.size(); i++) {
			final UMLLogicalLink c = obj.logicalLinks.get(i);
			if (i < classes.size()) {
				final UMLConceptualLink panel = classes.get(i);
				if (panel.getModel() != c) {
					panel.setModel(c);
					panel.updateModel();
				}
			} else {
				final ConceptualClassPanel nPanel = c.asConceptualPanel();
				classes.add(nPanel);
				this.add(nPanel);
			}
		}

		while (classes.size() > obj.classes.size()) {
			this.remove(classes.remove(classes.size() - 1));
		}

		validate();
		repaint();
	}

	public UMLFile getFile() {
		return file.get();
	}

	public void setFile(UMLFile file) {
		this.file = new WeakReference<>(file);
	}

	@Override
	public String toString() {
		return "ConceptualPanel@" + System.identityHashCode(this) + " [file=" + file + "]";
	}

}
