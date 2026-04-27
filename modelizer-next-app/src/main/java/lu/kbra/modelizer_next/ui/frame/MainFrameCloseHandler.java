package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

final class MainFrameCloseHandler extends WindowAdapter {

	private final MainFrame frame;

	MainFrameCloseHandler(final MainFrame frame) {
		this.frame = frame;
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		if (this.frame.confirmCloseWithSave("Do you want to save changes before closing?")) {
			this.frame.dispose();
		}
	}

}
