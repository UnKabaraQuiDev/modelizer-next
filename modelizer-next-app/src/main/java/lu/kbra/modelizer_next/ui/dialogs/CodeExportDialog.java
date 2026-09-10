package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import lombok.Getter;
import lu.kbra.code_exporter.api.CodeExporter;
import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

@Getter
public class CodeExportDialog extends JDialog {

	private static final long serialVersionUID = 7251302762903132031L;

	private final CodeExporter service;
	private ExporterOptions original;
	private ExporterOptions options;

	private JPanel optionsPanel;

	public CodeExportDialog(final MainFrame mainFrame, final CodeExporter service, final ExporterOptions options) {
		super(mainFrame);

		optionsPanel = service.getUiProvider().buildUI();

		{
			this.service = service;
			this.options = options == null ? service.getUiProvider().getOptions(optionsPanel) : options;
			final File savedFile = this.saveParent(mainFrame);
			if (savedFile == null) {
				throw new IllegalStateException();
			}
			this.options.setAttachedFile(savedFile.toPath());
			if (mainFrame.getLoadedExporterOptions().get(service.getExporterId()).hasKey()) {
				this.options = service.getOptionsManager()
						.relativizePaths(this.options,
								mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey().toPath().getParent());
			}
			this.original = this.options.clone();

			mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setValue(this.options);
		}

		final JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setContentPane(contentPane);

		final JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		service.getUiProvider()
				.restoreOptions(optionsPanel,
						mainFrame.getSession().getCurrentFile(),
						mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey(),
						this.options);
		this.getContentPane().add(optionsPanel, BorderLayout.CENTER);

		final JPanel leftPanel = new JPanel();
		panel.add(leftPanel, BorderLayout.WEST);

		final JButton btnSave = new JButton("Save");
		btnSave.addActionListener(a -> {
			final File file = this.saveParent(mainFrame);
			if (file == null) {
				return;
			}
			this.options = service.getUiProvider().getOptions(optionsPanel);
			this.saveTo(mainFrame, mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey(), this.options);
			mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setValue(options);
		});
		leftPanel.add(btnSave);

		final JButton btnSaveAs = new JButton("Save As...");
		btnSaveAs.addActionListener(a -> {
			final File file = this.saveParent(mainFrame);
			if (file == null) {
				return;
			}
			this.options = service.getUiProvider().getOptions(optionsPanel);
			this.saveAs(mainFrame, mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey(), this.options);
			mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setValue(options);
		});
		leftPanel.add(btnSaveAs);

		final JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(a -> {
			final File parentFile = this.saveParent(mainFrame);
			if (parentFile == null) {
				return;
			}

			if (!this.promptSaveCurrent(mainFrame)) {
				return;
			}

			final File importFile = this.load(mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey());
			if (importFile == null) {
				return;
			}

			try {
				this.options = MNMain.OBJECT_MAPPER.readValue(importFile, service.getOptionsManager().getClassType());
				mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setKey(importFile);
				this.options.setAttachedFile(mainFrame.getSession().getCurrentFile().toPath());
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(this,
						"Failed to loading the export configuration.\n\n" + e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.options = service.getOptionsManager().relativizePaths(this.options, importFile.toPath().getParent());

			this.original = this.options.clone();

			mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setKey(importFile);
			mainFrame.getLoadedExporterOptions().get(service.getExporterId()).setValue(this.options);
			service.getUiProvider().restoreOptions(optionsPanel, parentFile, importFile, this.options);
		});
		leftPanel.add(btnLoad);

		final JPanel rightPanel = new JPanel();
		panel.add(rightPanel, BorderLayout.EAST);

		final JButton btnExport = new JButton("Export");
		rightPanel.add(btnExport);

		super.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				CodeExportDialog.this.options = service.getUiProvider().getOptions(optionsPanel);
				if (CodeExportDialog.this.promptSaveCurrent(mainFrame)) {
					CodeExportDialog.this.dispose();
				}
			}
		});

		this.pack();
		this.setVisible(true);
	}

	private boolean promptSaveCurrent(final MainFrame mainFrame) {
		if (mainFrame.getSession().getCurrentFile() == null) {
			return true;
		}

		this.options = service.getUiProvider().getOptions(optionsPanel);
		this.options.setAttachedFile(mainFrame.getSession().getCurrentFile().toPath());
		if (mainFrame.getLoadedExporterOptions().get(service.getExporterId()).hasKey()) {
			this.options = service.getOptionsManager()
					.relativizePaths(this.options,
							mainFrame.getLoadedExporterOptions().get(service.getExporterId()).getKey().toPath().getParent());
		}

		if (Objects.equals(options, original)) {
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
			final File file = mainFrame.getLoadedExporterOptions().get(this.service.getExporterId()).getKey();
			this.saveTo(mainFrame, file, this.options);
			return true;
		}
		return false;
	}

	private File saveParent(final MainFrame mainFrame) {
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

	private void saveTo(final MainFrame mainFrame, final File file, final ExporterOptions options) {
		if (file == null) {
			this.saveAs(mainFrame, file, options);
			return;
		}

		try {
			this.options.setAttachedFile(mainFrame.getSession().getCurrentFile().toPath());
			this.options = this.service.getOptionsManager().relativizePaths(this.options, file.toPath().getParent());
			MNMain.OBJECT_MAPPER.writeValue(file, options);
			mainFrame.getLoadedExporterOptions().get(options.getExporterId()).setKey(file);
			this.original = this.options.clone();
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(this,
					"Failed to save the export configuration.\n\n" + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveAs(final MainFrame mainFrame, final File inputFile, final ExporterOptions options) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Modelizer Next Code Export (*.mnce)", "mnce"));

		if (inputFile != null) {
			chooser.setSelectedFile(inputFile);
		}

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		final File selectedFile = chooser.getSelectedFile();
		final File file;
		if (!selectedFile.getName().toLowerCase().endsWith(".mnce")) {
			file = new File(selectedFile.getParentFile(), selectedFile.getName() + ".mnce");
		} else {
			file = selectedFile;
		}

		this.saveTo(mainFrame, file, options);
	}

	private File load(final File inputFile) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Modelizer Next Code Export (*.mnce)", "mnce"));

		if (inputFile != null) {
			chooser.setSelectedFile(inputFile);
		}

		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		final File selectedFile = chooser.getSelectedFile();
		final File file;
		if (!selectedFile.getName().toLowerCase().endsWith(".mnce")) {
			file = new File(selectedFile.getParentFile(), selectedFile.getName() + ".mnce");
		} else {
			file = selectedFile;
		}

		return file;
	}

}
