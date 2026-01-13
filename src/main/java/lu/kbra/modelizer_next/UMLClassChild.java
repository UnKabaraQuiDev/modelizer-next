package lu.kbra.modelizer_next;

import java.awt.Component;
import java.util.Optional;

import javax.swing.SwingUtilities;

public interface UMLClassChild {

	default Optional<UMLClassContainerPanel> findParent() {
		if (!(this instanceof Component comp)) {
			return Optional.empty();
		}
		return Optional.ofNullable(
				(UMLClassContainerPanel) SwingUtilities.getAncestorOfClass(UMLClassContainerPanel.class, comp));
	}

	default void forceRedraw() {
		findParent().ifPresent(UMLClassContainerPanel::repaint);
	}

}
