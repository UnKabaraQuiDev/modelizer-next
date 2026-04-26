package lu.kbra.modelizer_next.cmdline;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.export.ViewExportRequest;
import lu.kbra.modelizer_next.ui.export.ViewExporter;
import lu.kbra.modelizer_next.ui.frame.DocumentSession;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;

public final class CommandLineExporter {

	private CommandLineExporter() {
	}

	public static int run(final String[] args) {
		System.setProperty("java.awt.headless", "true");

		try {
			final CommandLineExportOptions options = CommandLineExportParser.parse(args);

			final Optional<DocumentSession> session = MainFrame.createDocument(options.inputFile(),
					new ConsoleDocumentLoadHandler(options.force()));

			if (session.isEmpty()) {
				System.err.println("Export aborted.");
				return 2;
			}

			final ModelDocument document = session.get().getDocument();

			final Map<PanelType, DiagramCanvas> canvases = createCanvases(document, options.panelTypes());

			if (canvases.isEmpty()) {
				System.err.println("Nothing to export.");
				return 3;
			}

			final ViewExportRequest request = new ViewExportRequest(options
					.format(), options.scope(), options.panelTypes(), options.outputDirectory(), options.fileNamePattern());

			final List<File> exportedFiles = ViewExporter.exportViews(canvases, request, stripExtension(options.inputFile().getName()));

			for (final File exportedFile : exportedFiles) {
				System.out.println("Exported: " + exportedFile.getAbsolutePath());
			}

			return 0;
		} catch (final CommandLineExportParser.HelpRequestedException ex) {
			return 0;
		} catch (final Exception ex) {
			System.err.println("Export failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
			return 1;
		}
	}

	private static Map<PanelType, DiagramCanvas> createCanvases(final ModelDocument document, final List<PanelType> requestedPanelTypes) {
		final Map<PanelType, DiagramCanvas> canvases = new LinkedHashMap<>();

		final List<PanelType> panelTypes = requestedPanelTypes == null || requestedPanelTypes.isEmpty() ? List.of(PanelType.values())
				: requestedPanelTypes;

		for (final PanelType panelType : panelTypes) {
			final DiagramCanvas canvas = new DiagramCanvas(document, panelType, DocumentChangeListener.NOOP);

			if (canvas != null) {
				canvas.revalidate();
				canvas.doLayout();

				canvases.put(panelType, canvas);
			}
		}

		return canvases;
	}

	private static String stripExtension(final String fileName) {
		final int index = fileName.lastIndexOf('.');

		if (index <= 0) {
			return fileName;
		}

		return fileName.substring(0, index);
	}

}