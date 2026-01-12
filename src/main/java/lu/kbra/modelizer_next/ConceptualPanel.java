package lu.kbra.modelizer_next;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ConceptualPanel extends ClassPanel {

	protected WeakReference<UMLFile> file;

	protected List<ConceptualClassPanel> classes = new ArrayList<>();

	public ConceptualPanel(UMLFile file) {
		this.file = new WeakReference<>(file);
		file.classes.stream().map(UMLClass::asConceptualPanel).forEach(panel -> {
			this.classes.add(panel);
			this.add(panel);
		});

		setLayout(null);
	}

	public void updateModel() {
		final UMLFile obj = file.get();

		for (int i = 0; i < obj.classes.size(); i++) {
			final UMLClass c = obj.classes.get(i);
			if (i < classes.size()) {
				final ConceptualClassPanel panel = classes.get(i);
				if (panel.getModel() != c) {
					panel.setModel(c);
				}
			} else {
				final ConceptualClassPanel nPanel = c.asConceptualPanel();
				classes.add(nPanel);
				this.add(nPanel);
			}
		}

		while (obj.classes.size() > classes.size()) {
			this.remove(classes.remove(classes.size() - 1));
		}
	}

	public WeakReference<UMLFile> getFile() {
		return file;
	}

	public void setFile(WeakReference<UMLFile> file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "ConceptualPanel@" + System.identityHashCode(this) + " [file=" + file + "]";
	}

}
