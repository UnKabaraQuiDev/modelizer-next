package lu.kbra.modelizer_next.ui.frame;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;

public interface MainFrameDocumentController {

	default boolean confirmCloseWithSave(final String prompt) {
		if (!this.getSession().isDirty()) {
			return true;
		}

		final int choice = JOptionPane.showConfirmDialog((Component) this,
				prompt,
				"Unsaved changes",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
			return false;
		}

		return choice != JOptionPane.YES_OPTION || this.saveDocument();
	}

	default JFileChooser createOpenFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Model files (*.mn, *.mod, *.mdlz)", "mn", "mod", "mdlz"));
		return chooser;
	}

	default JFileChooser createSaveFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Modelizer Next (*.mn)", "mn"));
		return chooser;
	}

	ModelDocument getDocument();

	DocumentSession getSession();

	default void installCloseHandling() {
		final MainFrame frame = (MainFrame) this;
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new MainFrameCloseHandler(frame));
	}

	default void installFileDropSupport() {
		final MainFrame frame = (MainFrame) this;
		final TransferHandler fileDropHandler = new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(final TransferSupport support) {
				return support.isDrop() && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			public boolean importData(final TransferSupport support) {
				if (!this.canImport(support)) {
					return false;
				}

				try {
					final Transferable transferable = support.getTransferable();

					@SuppressWarnings(
						"unchecked"
					) final List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

					if (files == null || files.isEmpty()) {
						return false;
					}

					for (final File file : files) {
						if (frame.loadDocument(file)) {
							return true;
						}
					}

					JOptionPane.showMessageDialog(frame, "No supported file could be loaded.", "Drop error", JOptionPane.WARNING_MESSAGE);
					return false;
				} catch (final Exception ex) {
					JOptionPane.showMessageDialog(frame,
							"Failed to load dropped file:\n" + ex.getMessage(),
							"Drop error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		};

		frame.getRootPane().setTransferHandler(fileDropHandler);
		frame.rootDockingPanel.setTransferHandler(fileDropHandler);
	}

	default void loadDocument() {
		final JFileChooser chooser = this.createOpenFileChooser();
		if (chooser.showOpenDialog((Component) this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		if (!((MainFrame) this).loadDocument(chooser.getSelectedFile())) {
			JOptionPane.showMessageDialog((Component) this,
					"Failed to load document:\n" + chooser.getSelectedFile().getPath(),
					"Error during load",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	default boolean loadDocumentFromFile(final File selectedFile) {
		if (selectedFile == null || !selectedFile.isFile()) {
			return false;
		}

		final Optional<DocumentSession> model = DocumentSessionLoader.createDocument((Component) this, selectedFile);
		model.ifPresent(this::openInFrame);
		return model.isPresent();
	}

	default void newDocument() {
		final ModelDocument newDocument = new ModelDocument();
		newDocument.setSource("New document");
		this.openInFrame(new DocumentSession(newDocument));
	}

	void openInFrame(DocumentSession session);

	void refreshFrameTitle();

	default boolean saveDocument() {
		if (this.getSession().getCurrentFile() == null) {
			return this.saveDocumentAs();
		}
		return this.writeDocument(this.getSession().getCurrentFile());
	}

	default boolean saveDocumentAs() {
		final JFileChooser chooser = this.createSaveFileChooser();
		if (this.getSession().getCurrentFile() != null) {
			chooser.setSelectedFile(this.getSession().getCurrentFile());
		}

		if (chooser.showSaveDialog((Component) this) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		File selectedFile = chooser.getSelectedFile();
		if (!selectedFile.getName().toLowerCase().endsWith(".mn")) {
			selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".mn");
		}

		return this.writeDocument(selectedFile);
	}

	void updateUndoRedoMenuItems();

	default boolean writeDocument(final File file) {
		try {
			this.getDocument().getMeta().setUpdatedAt(Instant.now());
			this.getDocument().getMeta().setApplicationVersion(App.VERSION);
			this.getDocument().setSource(file.getPath());

			MNMain.OBJECT_MAPPER.writeValue(file, this.getDocument());
			this.getSession().markSaved(file);
			this.updateUndoRedoMenuItems();
			this.refreshFrameTitle();
			return true;
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog((Component) this,
					"Failed to save file:\n" + ex.getMessage(),
					"Save error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

}
