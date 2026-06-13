package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

public abstract class LivePopupMenu extends JPanel {

	public abstract void invokeConfirm(final ActionEvent e);

}
