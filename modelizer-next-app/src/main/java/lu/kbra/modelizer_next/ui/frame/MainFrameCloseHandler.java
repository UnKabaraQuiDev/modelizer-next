package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Close and unsaved-changes handling for the main frame.
 */
final class MainFrameCloseHandler extends WindowAdapter {

	private final MainFrame frame;

	/**
	 * Creates a main frame close handler instance.
	 * @param frame frame that owns the created UI component
	 */
	MainFrameCloseHandler(final MainFrame frame) {
		this.frame = frame;
	}

	/**
	 * Handles the window closing event.
	 * @param e event object supplied by Swing
	 */
	@Override
	public void windowClosing(final WindowEvent e) {
		if (this.frame.confirmCloseWithSave("Do you want to save changes before closing?")) {
			this.frame.dispose();
		}
	}

}
