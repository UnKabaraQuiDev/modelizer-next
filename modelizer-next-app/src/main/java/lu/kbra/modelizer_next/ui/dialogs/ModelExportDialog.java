package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import lombok.Getter;
import lu.kbra.model_exporter.api.ExportContext;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ExporterApiContext;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.model_exporter.api.SimpleExporterOptions;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.common.DefaultExportUpdateCallback;
import lu.kbra.modelizer_next.common.ExporterOptionRef;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.pclib.PCUtils;

@Getter
public abstract class ModelExportDialog extends JDialog {

	private static final long serialVersionUID = 7251302762903132031L;

	protected final MainFrame mainFrame;
	protected final ModelExporter service;
	protected final ExporterOptionRef editingRef;
	protected ExporterOptions original;
	protected ExporterOptions options;

	protected JPanel contentPanel;
	protected JPanel customOptionsPanel;

	public ModelExportDialog(final MainFrame mainFrame, final ModelExporter service, final ExporterOptionRef ref) {
		super(mainFrame);

		this.mainFrame = mainFrame;
		this.service = service;
		this.editingRef = ref;

		this.createOptionPanel();

		{
			this.options = ref.hasOptions() ? ref.getOptions()
					: ref.hasFile() ? PCUtils.try_(
							() -> MNMain.OBJECT_MAPPER.readValue(this.editingRef.getFile(), service.getOptionsManager().getClassType()),
							t -> null)
					: null;
			if (this.options == null) {
				this.options = this.parsePanelOptions();
			}
			ref.setOptions(this.options);
			this.original = this.options.clone();
		}

		final JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setContentPane(contentPane);

		final JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		if (this.contentPanel == null) {
			throw new IllegalStateException("Cannot have no options. (" + service.getExporterId() + ")");
		}
		this.restorePanelOption(this.options);
		this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);

		final JPanel leftPanel = new JPanel();
		panel.add(leftPanel, BorderLayout.WEST);

		final JButton btnSave = new JButton("Save");
		btnSave.addActionListener(this::saveConfig);
		leftPanel.add(btnSave);

		final JButton btnSaveAs = new JButton("Save As...");
		btnSaveAs.addActionListener(this::saveConfigAs);
		leftPanel.add(btnSaveAs);

		final JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(this::loadConfig);
		leftPanel.add(btnLoad);

		final JPanel rightPanel = new JPanel();
		panel.add(rightPanel, BorderLayout.EAST);

		final JButton btnExport = new JButton("Export");
		rightPanel.add(btnExport);

		super.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				ModelExportDialog.this.options = ModelExportDialog.this.parsePanelOptions();
				ref.setOptions(ModelExportDialog.this.options);
				if (ModelExportDialog.this.promptSaveCurrent(mainFrame)) {
					ModelExportDialog.this.dispose();
				}
			}

		});
		btnExport.addActionListener(this::export);
	}

	protected ExporterOptions parsePanelOptions() {
		if (this.hasCustomOptions()) {
			return this.service.getUiProvider().getOptions(this.customOptionsPanel);
		}
		throw new UnsupportedOperationException("This shouldn't've happened");
	}

	protected void restorePanelOption(final ExporterOptions options2) {
		if (this.hasCustomOptions()) {
			this.service.getUiProvider().restoreOptions(this.customOptionsPanel, options2);
		} else {
			throw new UnsupportedOperationException("This shouldn't've happened");
		}
	}

	protected void createOptionPanel() {
		this.customOptionsPanel = this.service.getUiProvider().hasCustomUI() ? this.service.getUiProvider().buildUI() : null;
		this.contentPanel = this.customOptionsPanel;
	}

	protected boolean promptSaveCurrent(final MainFrame mainFrame) {
		if (mainFrame.getSession().getCurrentFile() == null) {
			return true;
		}

		this.options = this.parsePanelOptions();
		this.editingRef.setOptions(this.options);

		if (Objects.equals(this.options, this.original)) {
			return true;
		}

		final int result = JOptionPane.showConfirmDialog(this,
				"Looks like the the current configuration changed.\nDo you want to save it?",
				"Warning",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (result == JOptionPane.NO_OPTION) {
			return true;
		} else if (result == JOptionPane.YES_OPTION) {
			final File file = this.editingRef.getFile();
			this.saveTo(mainFrame, file, this.options);
			return true;
		}
		return false;
	}

	protected File saveParent(final MainFrame mainFrame) {
		final File file = mainFrame.getSession().getCurrentFile();
		if (file == null) {
			final int result = JOptionPane.showConfirmDialog(this,
					"You need to save the current file before saving an export configuration.",
					"Warning",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				mainFrame.saveDocumentAs();
			} else {
				return null;
			}
		}
		return file;
	}

	protected void saveTo(final MainFrame mainFrame, final File file, final ExporterOptions options) {
		if (file == null) {
			this.saveAs(mainFrame, file, options);
			return;
		}

		try {
			MNMain.OBJECT_MAPPER.writeValue(file, options);
			this.editingRef.setFile(file);
//			this.original = this.options.clone();
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(this,
					"Failed to save the export configuration.\n\n" + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void saveAs(final MainFrame mainFrame, final File inputFile, final ExporterOptions options) {
		final String extension = this.service.getExporterType().getExtension();
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(
				new FileNameExtensionFilter("Modelizer Next " + this.service.getExporterType().getName() + " Export (*." + extension + ")",
						extension));

		if (inputFile != null) {
			chooser.setSelectedFile(inputFile);
		}

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		final File selectedFile = chooser.getSelectedFile();
		final File file;
		if (!selectedFile.getName().toLowerCase().endsWith("." + extension)) {
			file = new File(selectedFile.getParentFile(), selectedFile.getName() + "." + extension);
		} else {
			file = selectedFile;
		}

		this.saveTo(mainFrame, file, options);
	}

	protected File load(final File inputFile) {
		final String extension = this.service.getExporterType().getExtension();
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(
				new FileNameExtensionFilter("Modelizer Next " + this.service.getExporterType().getName() + " Export (*." + extension + ")",
						extension));

		if (inputFile != null) {
			chooser.setSelectedFile(inputFile);
		}

		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return chooser.getSelectedFile();
	}

	protected boolean hasCustomOptions() {
		return this.customOptionsPanel != null;
	}

	protected void saveConfig(final ActionEvent actionevent1) {
//		final File file = this.saveParent(this.mainFrame);
//		if (file == null) {
//			return;
//		}
		this.options = this.parsePanelOptions();
		this.editingRef.setOptions(this.options);
		this.saveTo(this.mainFrame, this.editingRef.getFile(), this.options);
	}

	protected void saveConfigAs(final ActionEvent actionevent1) {
//		final File file = this.saveParent(this.mainFrame);
//		if (file == null) {
//			return;
//		}
		this.options = this.parsePanelOptions();
		this.editingRef.setOptions(this.options);
		this.saveAs(this.mainFrame, this.editingRef.getFile(), this.options);
	}

	protected void loadConfig(final ActionEvent actionevent1) {
//		final File parentFile = this.saveParent(this.mainFrame);
//		if (parentFile == null || !this.promptSaveCurrent(this.mainFrame)) {
//			return;
//		}

		final File importFile = this.load(this.editingRef.getFile());
		if (importFile == null) {
			return;
		}

		try {
			final SimpleExporterOptions simple = MNMain.OBJECT_MAPPER.readValue(importFile, SimpleExporterOptions.class);
			if (!Objects.equals(simple.getExporterId(), this.service.getExporterId())) {
				final int result = JOptionPane.showConfirmDialog(this,
						"It looks like that export configuration is for: " + simple.getExporterId() + ".\nDo you want to open it anyways ?",
						"Error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);

				if (result == JOptionPane.YES_OPTION) {
					this.mainFrame.getLoadedExporterOptions().put(simple.getExporterId(), new ExporterOptionRef(importFile, null));
					this.mainFrame.export(simple.getExporterId());
					this.dispose();
				}
				return;

			} else {
				this.options = MNMain.OBJECT_MAPPER.readValue(importFile, this.service.getOptionsManager().getClassType());
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(this,
					"Failed to loading the export configuration.\n\n" + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.original = this.options.clone();

		this.editingRef.setFile(importFile);
		this.editingRef.setOptions(this.options);
		this.restorePanelOption(this.options);
	}

	private void export(final ActionEvent actionevent1) {
		final ExportProgressDialog progressDialog = new ExportProgressDialog(this.mainFrame);

		progressDialog.setVisible(true);

		final ExportUpdateCallback callback = DefaultExportUpdateCallback.create(progressDialog);

		final SwingWorker<Void, Void> worker = new SwingWorker<>() {

			@Override
			protected Void doInBackground() throws Exception {
				ExporterApiContext.clearApiContext();
				ExporterApiContext.getApiContext().setContext(ExportContext.GUI);
				ExporterApiContext.getApiContext()
						.setCurrentConfig(ModelExportDialog.this.editingRef.getFile() == null ? null
								: ModelExportDialog.this.editingRef.getFile().toURI());
				ExporterApiContext.getApiContext()
						.setCurrentDocument(ModelExportDialog.this.mainFrame.getSession().getCurrentFile() == null ? null
								: ModelExportDialog.this.mainFrame.getSession().getCurrentFile().toURI());
				ExporterApiContext.getApiContext().setRenderers(pts -> ModelExportDialog.this.mainFrame.getCanvasesByPanelType());

				ModelExportDialog.this.service.buildModelVisitor(ModelExportDialog.this.parsePanelOptions())
						.visitDocument(ModelExportDialog.this.mainFrame.getSession().getDocument(), callback);

				return null;
			}

			@Override
			protected void done() {
				try {
					this.get();
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (final ExecutionException e) {
					final Throwable cause = e.getCause();

					cause.printStackTrace();
				} finally {
					progressDialog.dispose();
				}
			}
		};

		worker.execute();
	}

}
