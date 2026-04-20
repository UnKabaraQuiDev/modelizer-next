package lu.kbra.modelizer_next.update;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class UpdateInstallerLauncher {

	private UpdateInstallerLauncher() {
	}

	public static void launch(final Path downloadedFile) throws IOException {
		if (downloadedFile == null) {
			throw new IllegalArgumentException("downloadedFile cannot be null.");
		}

		final File file = downloadedFile.toFile();
		if (!file.isFile()) {
			throw new IOException("Downloaded update file is missing: " + downloadedFile);
		}

		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().open(file);
			return;
		}

		throw new IOException("Desktop integration is not supported on this platform.");
	}
}
