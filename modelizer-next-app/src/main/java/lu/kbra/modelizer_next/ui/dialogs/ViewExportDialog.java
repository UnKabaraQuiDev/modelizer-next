package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportRequest;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;
import lu.kbra.modelizer_next.ui.export.ViewExporter;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

public class ViewExportDialog extends JDialog {

	private static final class ExportPreviewPanel extends JPanel {

		private static final long serialVersionUID = 3338223416144336229L;

		private BufferedImage previewImage;

		private ExportPreviewPanel() {
			this.setPreferredSize(new Dimension(520, 420));
			this.setBackground(java.awt.Color.WHITE);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);

			if (this.previewImage == null) {
				return;
			}

			final int availableWidth = Math.max(1, this.getWidth() - 24);
			final int availableHeight = Math.max(1, this.getHeight() - 24);
			final double scale = Math.min(availableWidth / (double) this.previewImage.getWidth(),
					availableHeight / (double) this.previewImage.getHeight());
			final int imageWidth = Math.max(1, (int) Math.round(this.previewImage.getWidth() * scale));
			final int imageHeight = Math.max(1, (int) Math.round(this.previewImage.getHeight() * scale));
			final int x = (this.getWidth() - imageWidth) / 2;
			final int y = (this.getHeight() - imageHeight) / 2;

			graphics.drawImage(this.previewImage.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH), x, y, null);
		}

		private void setPreview(final DiagramCanvas canvas, final ViewExportScope scope) {
			if (canvas == null || scope == null) {
				this.previewImage = null;
				this.repaint();
				return;
			}

			this.previewImage = canvas.createExportPreviewImage(scope, 900, 700);
			this.repaint();
		}

	}

	private static final class PatternTextField extends JTextField {

		private static final long serialVersionUID = 7204067903603166607L;

		private PatternTextField(final String text) {
			super(text);
			this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, MainFrame.CTRL_MODIFIER), "showTokenSuggestions");
			this.getActionMap().put("showTokenSuggestions", new AbstractAction() {
				private static final long serialVersionUID = 8970378556838542205L;

				@Override
				public void actionPerformed(final ActionEvent event) {
					PatternTextField.this.showTokenSuggestions();
				}
			});
			this.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(final KeyEvent event) {
					if (event.getKeyChar() == '%') {
						SwingUtilities.invokeLater(PatternTextField.this::showTokenSuggestions);
					}
				}
			});
		}

		private void insertToken(final String token) {
			final int caretPosition = this.getCaretPosition();
			if (caretPosition > 0 && this.getText().charAt(caretPosition - 1) == '%') {
				this.setSelectionStart(caretPosition - 1);
				this.setSelectionEnd(caretPosition);
			}
			this.replaceSelection(token);
			this.requestFocusInWindow();
		}

		private void showTokenSuggestions() {
			final JPopupMenu menu = new JPopupMenu();
			for (final String token : ViewExporter.FILE_PATTERN_TOKENS) {
				menu.add(new AbstractAction(token) {
					private static final long serialVersionUID = 6950296399216736075L;

					@Override
					public void actionPerformed(final ActionEvent event) {
						PatternTextField.this.insertToken(token);
					}
				});
			}
			menu.show(this, 0, this.getHeight());
		}

	}

	private record SimpleDocumentListener(Runnable delegate) implements DocumentListener {
		@Override
		public void changedUpdate(final DocumentEvent event) {
			this.delegate.run();
		}

		@Override
		public void insertUpdate(final DocumentEvent event) {
			this.delegate.run();
		}

		@Override
		public void removeUpdate(final DocumentEvent event) {
			this.delegate.run();
		}
	}

	private static final long serialVersionUID = -4894368238563345666L;
	private final Map<PanelType, DiagramCanvas> canvases;
	private final PanelType activePanelType;
	private final JComboBox<ViewExportFormat> formatSelector;
	private final JComboBox<ViewExportScope> scopeSelector;
	private final Map<PanelType, JCheckBox> panelTypeBoxes;
	private final JTextField outputDirectoryField;

	private final PatternTextField filePatternField;

	private final ExportPreviewPanel previewPanel;

	private final JButton exportButton;

	private ViewExportRequest result;

	private ViewExportDialog(
			final Component parent,
			final Map<PanelType, DiagramCanvas> canvases,
			final PanelType activePanelType,
			final File defaultOutputDirectory) {

		super(SwingUtilities.getWindowAncestor(parent), "Export current view", ModalityType.APPLICATION_MODAL);
		this.canvases = new LinkedHashMap<>(canvases);
		this.activePanelType = activePanelType;
		this.panelTypeBoxes = new EnumMap<>(PanelType.class);

		this.formatSelector = new JComboBox<>(ViewExportFormat.values());
		this.scopeSelector = new JComboBox<>(ViewExportScope.values());
		this.scopeSelector.setSelectedItem(ViewExportScope.VIEW);
		this.outputDirectoryField = new JTextField(defaultOutputDirectory == null ? "" : defaultOutputDirectory.getAbsolutePath(), 24);
		this.filePatternField = new PatternTextField(ViewExporter.DEFAULT_FILE_PATTERN);
		this.previewPanel = new ExportPreviewPanel();
		this.exportButton = new JButton("Export");

		this.setLayout(new BorderLayout(12, 12));
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		this.add(this.createPreviewPane(), BorderLayout.CENTER);
		this.add(this.createOptionsPane(), BorderLayout.EAST);
		this.add(this.createButtonPane(), BorderLayout.SOUTH);

		this.installListeners();
		this.updateExportButtonState();
		this.refreshPreview();

		this.setPreferredSize(new Dimension(920, 620));
		this.pack();
		this.setLocationRelativeTo(parent);
	}

	private void addRow(final JPanel panel, final int row, final String label, final Component component) {
		final GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.gridx = 0;
		labelGbc.gridy = row;
		labelGbc.anchor = GridBagConstraints.NORTHWEST;
		labelGbc.insets = new Insets(4, 4, 8, 8);
		panel.add(new JLabel(label), labelGbc);

		final GridBagConstraints componentGbc = new GridBagConstraints();
		componentGbc.gridx = 1;
		componentGbc.gridy = row;
		componentGbc.fill = GridBagConstraints.HORIZONTAL;
		componentGbc.weightx = 1.0;
		componentGbc.insets = new Insets(4, 4, 8, 4);
		panel.add(component, componentGbc);
	}

	private void browseOutputDirectory() {
		final JFileChooser chooser = new JFileChooser(this.outputDirectoryField.getText());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select output directory");

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.outputDirectoryField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private JPanel createButtonPane() {
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton cancelButton = new JButton("Cancel");

		this.exportButton.addActionListener(event -> this.saveResultAndClose());
		cancelButton.addActionListener(event -> this.dispose());

		buttons.add(cancelButton);
		buttons.add(this.exportButton);
		this.getRootPane().setDefaultButton(this.exportButton);
		return buttons;
	}

	private JPanel createOptionsPane() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Export settings"));
		panel.setPreferredSize(new Dimension(320, 420));

		int row = 0;
		this.addRow(panel, row++, "Type", this.formatSelector);
		this.addRow(panel, row++, "Export", this.scopeSelector);

		final JPanel panelTypePanel = new JPanel(new GridBagLayout());
		int panelRow = 0;
		for (final PanelType panelType : PanelType.values()) {
			final JCheckBox checkBox = new JCheckBox(this.panelTypeLabel(panelType), panelType == this.activePanelType);
			this.panelTypeBoxes.put(panelType, checkBox);
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = panelRow++;
			gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			panelTypePanel.add(checkBox, gbc);
		}
		this.addRow(panel, row++, "Panels", panelTypePanel);

		final JPanel directoryPanel = new JPanel(new BorderLayout(4, 0));
		final JButton browseButton = new JButton("Browse...");
		directoryPanel.add(this.outputDirectoryField, BorderLayout.CENTER);
		directoryPanel.add(browseButton, BorderLayout.EAST);
		browseButton.addActionListener(event -> this.browseOutputDirectory());
		this.addRow(panel, row++, "Directory", directoryPanel);

		this.addRow(panel, row++, "File pattern", this.filePatternField);
//		final JLabel tokenHint = new JLabel("Use Ctrl+Space for tokens: %FILENAME%, %TYPE%, %EXTENSION%.");
//		final GridBagConstraints hintGbc = new GridBagConstraints();
//		hintGbc.gridx = 0;
//		hintGbc.gridy = row++;
//		hintGbc.gridwidth = 2;
//		hintGbc.insets = new Insets(0, 4, 8, 4);
//		hintGbc.anchor = GridBagConstraints.WEST;
//		panel.add(tokenHint, hintGbc);

		final GridBagConstraints filler = new GridBagConstraints();
		filler.gridx = 0;
		filler.gridy = row;
		filler.gridwidth = 2;
		filler.weighty = 1.0;
		panel.add(new JPanel(), filler);
		return panel;
	}

	private JScrollPane createPreviewPane() {
		final JScrollPane scrollPane = new JScrollPane(this.previewPanel);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Preview"));
		return scrollPane;
	}

	private DiagramCanvas findPreviewCanvas() {
		if (this.panelTypeBoxes.getOrDefault(this.activePanelType, new JCheckBox()).isSelected()) {
			return this.canvases.get(this.activePanelType);
		}

		for (final PanelType panelType : PanelType.values()) {
			final JCheckBox checkBox = this.panelTypeBoxes.get(panelType);
			if (checkBox != null && checkBox.isSelected()) {
				return this.canvases.get(panelType);
			}
		}

		return this.canvases.get(this.activePanelType);
	}

	private List<PanelType> getSelectedPanelTypes() {
		final List<PanelType> panelTypes = new ArrayList<>();
		for (final PanelType panelType : PanelType.values()) {
			final JCheckBox checkBox = this.panelTypeBoxes.get(panelType);
			if (checkBox != null && checkBox.isSelected()) {
				panelTypes.add(panelType);
			}
		}
		return panelTypes;
	}

	private void installListeners() {
		this.formatSelector.addActionListener(event -> this.refreshPreview());
		this.scopeSelector.addActionListener(event -> this.refreshPreview());
		for (final JCheckBox checkBox : this.panelTypeBoxes.values()) {
			checkBox.addActionListener(event -> {
				this.updateExportButtonState();
				this.refreshPreview();
			});
		}
		this.filePatternField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateExportButtonState));
		this.outputDirectoryField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateExportButtonState));
	}

	private String panelTypeLabel(final PanelType panelType) {
		return switch (panelType) {
		case CONCEPTUAL -> "Conceptual";
		case LOGICAL -> "Logical";
		case PHYSICAL -> "Physical";
		};
	}

	private void refreshPreview() {
		final DiagramCanvas canvas = this.findPreviewCanvas();
		this.previewPanel.setPreview(canvas, (ViewExportScope) this.scopeSelector.getSelectedItem());
	}

	private void saveResultAndClose() {
		this.result = new ViewExportRequest((ViewExportFormat) this.formatSelector.getSelectedItem(),
				(ViewExportScope) this.scopeSelector.getSelectedItem(),
				this.getSelectedPanelTypes(),
				new File(this.outputDirectoryField.getText()),
				this.filePatternField.getText());
		this.dispose();
	}

	private void updateExportButtonState() {
		this.exportButton.setEnabled(!this.getSelectedPanelTypes().isEmpty() && !this.outputDirectoryField.getText().isBlank()
				&& !this.filePatternField.getText().isBlank());
	}

	public static ViewExportRequest showDialog(
			final Component parent,
			final Map<PanelType, DiagramCanvas> canvases,
			final PanelType activePanelType,
			final File defaultOutputDirectory) {

		final ViewExportDialog dialog = new ViewExportDialog(parent, canvases, activePanelType, defaultOutputDirectory);
		dialog.setVisible(true);
		return dialog.result;
	}

}
