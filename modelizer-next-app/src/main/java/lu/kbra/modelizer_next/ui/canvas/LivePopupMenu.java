package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

public abstract class LivePopupMenu extends JPanel {

	private static final long serialVersionUID = 3949854513508031698L;

	public abstract void invokeConfirm(final ActionEvent e);

}
