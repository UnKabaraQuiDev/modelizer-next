package lu.kbra.modelizer_next.common;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

public final class FileOpenBridge {

	public static final Queue<File> TO_BE_OPENED = new ArrayDeque<>();
	protected static Runnable PING;

	public static void setPing(Runnable ping) {
		PING = ping;
		ping.run();
	}

	public static void clearPing() {
		PING = null;
	}

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
			if (PING != null) {
				PING.run();
			}
		});
	}

}