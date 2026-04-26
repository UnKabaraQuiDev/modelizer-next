package lu.kbra.modelizer_next;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;

import lu.kbra.modelizer_next.bootstrap.AppMain;
import lu.kbra.modelizer_next.common.FileOpenBridge;
import lu.kbra.modelizer_next.common.SampleDocumentFactory;
import lu.kbra.modelizer_next.ui.DocumentSession;
import lu.kbra.modelizer_next.ui.MainFrame;

public class ModelizerAppEntryPoint implements AppMain {

	@Override
	public void start(final String[] args) {
		try {
			App.init();
			System.out.println(App.NAME + " / " + App.VERSION + " [" + App.DISTRIBUTOR + "]");
			System.out.println("App dir: " + App.getAppDirectory());
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Uh uh ! It seems like this app's manifest is malformed, try\nrestarting it, redownloading it or updating it.\nIf nothing works, please report to: "
							+ App.ISSUES_URL,
					"Manifest error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			MNMain.applyConfiguredLookAndFeel();
			System.out.println("Args: " + Arrays.toString(args));
			Optional<DocumentSession> document = Optional.empty();

			if (args.length > 0) {
				final Path file = Path.of(args[0]);

				if (Files.exists(file)) {
					document = MainFrame.createDocument(null, file.toFile());
				}
			}

			final MainFrame frame = new MainFrame(document.orElseGet(() -> new DocumentSession(SampleDocumentFactory.create(), null)));
			if (document.isEmpty()) {
				frame.applyDefaultPaletteToCanvases();
			}
			frame.setVisible(true);

			FileOpenBridge.setCallback(() -> {
				while (frame.getDocument() == null || frame.getDocument().getMeta().getName().equals(SampleDocumentFactory.META_NAME)) {
					final File f = FileOpenBridge.TO_BE_OPENED.poll();
					System.out.println("Got open event for: " + f);
//					JOptionPane.showMessageDialog(null, "Trying to open: " + f);
					if (f == null) {
						break;
					} else if (!f.exists()) {
						continue;
					}
					if (frame.loadDocument(f)) {
						FileOpenBridge.TO_BE_OPENED.clear();
						FileOpenBridge.clearCallback();
					}
				}
			});
		});
	}

}
