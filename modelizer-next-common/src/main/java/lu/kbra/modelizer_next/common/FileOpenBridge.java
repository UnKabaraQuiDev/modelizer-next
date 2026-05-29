package lu.kbra.modelizer_next.common;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Stores a file-open callback used when the operating system asks the app to open a file.
 */
public final class FileOpenBridge {

	public static final Queue<File> TO_BE_OPENED = new ArrayDeque<>();
	protected static Runnable PING;

	/**
	 * Clears the callback.
	 */
	public static void clearCallback() {
		FileOpenBridge.PING = null;
	}

	/**
	 * Installs the file handler.
	 */
	public static void installFileHandler() {
		if (!Desktop.isDesktopSupported()) {
			return;
		}

		final Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Action.APP_OPEN_FILE)) {
			return;
		}

		desktop.setOpenFileHandler(e -> {
			FileOpenBridge.TO_BE_OPENED.addAll(e.getFiles());
			if (FileOpenBridge.PING != null) {
				FileOpenBridge.PING.run();
			}
		});
	}

	/**
	 * Sets the callback.
	 *
	 * @param ping ping value used by the operation
	 */
	public static void setCallback(final Runnable ping) {
		FileOpenBridge.PING = ping;
		ping.run();
	}

}
