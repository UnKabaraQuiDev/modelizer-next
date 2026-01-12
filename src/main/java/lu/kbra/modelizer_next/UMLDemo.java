package lu.kbra.modelizer_next;

import javax.swing.SwingUtilities;

public class UMLDemo {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new MainFrame().setVisible(true);
		});
	}
}
