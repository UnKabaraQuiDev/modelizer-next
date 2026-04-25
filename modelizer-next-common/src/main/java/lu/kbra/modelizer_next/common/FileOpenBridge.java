package lu.kbra.modelizer_next.common;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

public final class FileOpenBridge {

	public static final Queue<File> TO_BE_OPENED = new ArrayDeque<>();

	public static void installFileHandler() {
		if (!Desktop.isDesktopSupported()) {
			return;
		}

		final Desktop desktop = Desktop.getDesktop();

		desktop.setOpenFileHandler(e -> FileOpenBridge.TO_BE_OPENED.addAll(e.getFiles()));
	}

}