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
import lu.kbra.modelizer_next.bootstrap.UpdateRuntime;
import lu.kbra.modelizer_next.bootstrap.UpdateRuntimes;
import lu.kbra.modelizer_next.cmdline.CommandLineExportParser;
import lu.kbra.modelizer_next.cmdline.CommandLineExporter;
import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.common.FileOpenBridge;
import lu.kbra.modelizer_next.common.LicensePrinter;
import lu.kbra.modelizer_next.common.SampleDocumentFactory;
import lu.kbra.modelizer_next.common.UnsupportedBootstrapVersionException;
import lu.kbra.modelizer_next.ui.frame.DocumentSession;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.pclib.PCUtils;

/**
 * Bootstrap-facing entry point that starts the normal Modelizer Next application from an external
 * runtime.
 */
public class ModelizerAppEntryPoint implements AppMain {

	public static final String BOOTSTRAP_VERSION_CHECK_PROPERTY = ModelizerAppEntryPoint.class.getSimpleName() + ".boostrap_version_check";
	public static boolean BOOSTRAP_VERSION_CHECK = PCUtils.getBoolean(BOOTSTRAP_VERSION_CHECK_PROPERTY, true);

	/**
	 * Starts the application entry point.
	 *
	 * @param args command-line arguments supplied by the launcher
	 */
	@Override
	public void start(final String[] args) {
		LicensePrinter.print(this.getClass().getClassLoader());

		if (BOOSTRAP_VERSION_CHECK) {
			try {
				PCUtils.readPackagedBytesFile("/app.json");
				throw new UnsupportedBootstrapVersionException("Bootstrap loader is too old, use >= v8.");
			} catch (final Exception e) {
				// ok
			}

			if (!App.PORTABLE && UpdateRuntimes.isActive()) {
				final File updatesDir = new File(App.getConfigDirectory(), "updates");
				if (updatesDir.exists()) {
					throw new UnsupportedBootstrapVersionException("Bootstrap loader is too old, use >= v10.");
				}
			}
		}

		try {
			App.init();
			System.out.println(App.NAME + " / " + App.VERSION + " [" + App.DISTRIBUTOR + "]");
			System.out.println("App dir: " + App.getConfigDirectory());
			System.out.println();
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Uh uh ! It seems like this app's manifest is malformed, try\nrestarting it, redownloading it or updating it.\nIf nothing works, please report to: "
							+ App.ISSUES_URL,
					"Manifest error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (CommandLineExportParser.isExportRequest(args)) {
			final int exitCode = CommandLineExporter.run(args);
			System.exit(exitCode);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			System.out.println(
					"Applying laf took: " + (double) PCUtils.millisTime((Runnable) MNMain::applyConfiguredLookAndFeel) / 1_000 + "s");
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
				while (frame.getDocument() == null || SampleDocumentFactory.META_NAME.equals(frame.getDocument().getMeta().getName())) {
					final File f = FileOpenBridge.TO_BE_OPENED.poll();
					System.out.println("Got open event for: " + f);
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

	public static Optional<UpdateRuntime> bootstrapRuntime() {
		return UpdateRuntimes.isActive() ? Optional.of(UpdateRuntimes.getInstance()) : Optional.empty();
	}

}
