package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
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
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.component.ColorButton;
import lu.kbra.modelizer_next.ui.export.ImageViewExportOptions;
import lu.kbra.modelizer_next.ui.export.PdfMargins;
import lu.kbra.modelizer_next.ui.export.PdfPageFormat;
import lu.kbra.modelizer_next.ui.export.PdfPageOrientation;
import lu.kbra.modelizer_next.ui.export.PdfViewExportOptions;
import lu.kbra.modelizer_next.ui.export.ViewExportFormat;
import lu.kbra.modelizer_next.ui.export.ViewExportOptions;
import lu.kbra.modelizer_next.ui.export.ViewExportRequest;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;
import lu.kbra.modelizer_next.ui.export.ViewExporter;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

/**
 * Dialog that collects view export settings and previews the resulting export area.
 */
public class ViewExportDialog extends JDialog {

	private static final class ExportPreviewPanel extends JPanel {

		private static final long serialVersionUID = 3338223416144336229L;

		private BufferedImage previewImage;

		private ExportPreviewPanel() {
			this.setPreferredSize(new Dimension(520, 420));
			this.setBackground(Color.WHITE);
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

		private final List<String> tokens;

		private PatternTextField(final String text, final List<String> tokens) {
			super();
			this.setText(text);
			this.tokens = tokens == null ? List.of() : List.copyOf(tokens);
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

		private PatternTextField(final String text) {
			this(text, ViewExporter.FILE_PATTERN_TOKENS);
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
			for (final String token : this.tokens) {
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

	private static final class CurrentCardPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private CurrentCardPanel(final CardLayout layout) {
			super(layout);
		}

		@Override
		public Dimension getPreferredSize() {
			for (final Component component : this.getComponents()) {
				if (component.isVisible()) {
					return component.getPreferredSize();
				}
			}
			return super.getPreferredSize();
		}
	}

	private static final long serialVersionUID = -4894368238563345666L;
	private static final String IMAGE_CARD = "image";
	private static final String PDF_CARD = "pdf";
	private static final List<String> PDF_TEXT_TOKENS = List
			.of("%FILENAME%", "%TYPE%", "%PANEL%", "%EXTENSION%", "%DATE%", "%TIME%", "%PAGE%", "%PAGES%");

	public static ViewExportRequest showDialog(
			final Component parent,
			final Map<PanelType, DiagramCanvas> canvases,
			final PanelType activePanelType,
			final File defaultOutputDirectory) {
		final ViewExportDialog dialog = new ViewExportDialog(parent, canvases, activePanelType, defaultOutputDirectory);
		dialog.setVisible(true);
		return dialog.result;
	}

	private final Map<PanelType, DiagramCanvas> canvases;
	private final PanelType activePanelType;

	private final JComboBox<ViewExportFormat> formatSelector;
	private final JComboBox<ViewExportScope> scopeSelector;
	private final Map<PanelType, JCheckBox> panelTypeBoxes;
	private final JTextField outputDirectoryField;
	private final PatternTextField filePatternField;

	private final ColorButton backgroundColorButton;
	private final JCheckBox transparentBackgroundBox;
	private final CardLayout formatOptionsLayout;
	private final JPanel formatOptionsCards;

	private final JComboBox<PdfPageFormat> pdfPageFormatSelector;
	private final JComboBox<PdfPageOrientation> pdfOrientationSelector;
	private final JTextField pdfCustomWidthField;
	private final JTextField pdfCustomHeightField;
	private final JTextField pdfMarginTopField;
	private final JTextField pdfMarginRightField;
	private final JTextField pdfMarginBottomField;
	private final JTextField pdfMarginLeftField;
	private final JTextField pdfUnderTemplateField;
	private final JTextField pdfOverTemplateField;
	private final PatternTextField pdfHeaderField;
	private final PatternTextField pdfFooterField;

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
		this.outputDirectoryField = new JTextField(defaultOutputDirectory == null ? "" : defaultOutputDirectory.getAbsolutePath());
		this.filePatternField = new PatternTextField(ViewExporter.DEFAULT_FILE_PATTERN);
		this.backgroundColorButton = new ColorButton("Background", Color.WHITE);
		this.transparentBackgroundBox = new JCheckBox("Transparent background");
		this.formatOptionsLayout = new CardLayout();
		this.formatOptionsCards = new CurrentCardPanel(this.formatOptionsLayout);

		this.pdfPageFormatSelector = new JComboBox<>(PdfPageFormat.values());
		this.pdfPageFormatSelector.setSelectedItem(PdfPageFormat.A4);
		this.pdfOrientationSelector = new JComboBox<>(PdfPageOrientation.values());
		this.pdfCustomWidthField = new JTextField("595");
		this.pdfCustomHeightField = new JTextField("842");
		this.pdfMarginTopField = new JTextField("36");
		this.pdfMarginRightField = new JTextField("36");
		this.pdfMarginBottomField = new JTextField("36");
		this.pdfMarginLeftField = new JTextField("36");
		this.pdfUnderTemplateField = new JTextField();
		this.pdfOverTemplateField = new JTextField();
		this.pdfHeaderField = new PatternTextField("", ViewExportDialog.PDF_TEXT_TOKENS);
		this.pdfFooterField = new PatternTextField("", ViewExportDialog.PDF_TEXT_TOKENS);

		this.previewPanel = new ExportPreviewPanel();
		this.exportButton = new JButton("Export");

		this.setLayout(new BorderLayout(12, 12));
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		final JScrollPane previewPane = this.createPreviewPane();
		final JScrollPane optionsPane = this.createOptionsPane();

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPane, optionsPane);
		splitPane.setResizeWeight(1.0);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(null);

		this.add(splitPane, BorderLayout.CENTER);
		this.add(this.createButtonPane(), BorderLayout.SOUTH);

		this.installListeners();
		this.updateFormatOptionsVisibility();
		this.updateExportButtonState();
		this.refreshPreview();

		this.setPreferredSize(new Dimension(980, 660));
		this.pack();
		SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.55));
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

	private void addFullRow(final JPanel panel, final int row, final Component component) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 4, 8, 4);
		panel.add(component, gbc);
	}

	private ViewExportOptions buildExportOptions(final ViewExportFormat format) {
		if (format == ViewExportFormat.PDF) {
			return new PdfViewExportOptions((PdfPageFormat) this.pdfPageFormatSelector.getSelectedItem(),
					(PdfPageOrientation) this.pdfOrientationSelector.getSelectedItem(),
					this.parseDouble(this.pdfCustomWidthField, PdfPageFormat.A4.getWidth()),
					this.parseDouble(this.pdfCustomHeightField, PdfPageFormat.A4.getHeight()),
					new PdfMargins(this.parseDouble(this.pdfMarginTopField, 36.0),
							this.parseDouble(this.pdfMarginRightField, 36.0),
							this.parseDouble(this.pdfMarginBottomField, 36.0),
							this.parseDouble(this.pdfMarginLeftField, 36.0)),
					this.optionalFile(this.pdfUnderTemplateField),
					this.optionalFile(this.pdfOverTemplateField),
					this.pdfHeaderField.getText(),
					this.pdfFooterField.getText());
		}
		return new ImageViewExportOptions(this.transparentBackgroundBox.isSelected() && format.supportsTransparency());
	}

	private void browseOutputDirectory() {
		final JFileChooser chooser = new JFileChooser(this.outputDirectoryField.getText());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select output directory");

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.outputDirectoryField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void browseTemplateFile(final JTextField targetField) {
		final JFileChooser chooser = new JFileChooser(
				targetField.getText().isBlank() ? this.outputDirectoryField.getText() : targetField.getText());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Select template image");

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			targetField.setText(chooser.getSelectedFile().getAbsolutePath());
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

	private JPanel createImageOptionsPane() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Image options"));
		int row = 0;
		this.addFullRow(panel, row++, this.transparentBackgroundBox);
		return panel;
	}

	private JScrollPane createOptionsPane() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Export settings"));

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
		this.addRow(panel, row++, "Background", this.backgroundColorButton);

		this.formatOptionsCards.add(this.createImageOptionsPane(), ViewExportDialog.IMAGE_CARD);
		this.formatOptionsCards.add(this.createPdfOptionsPane(), ViewExportDialog.PDF_CARD);
		this.addFullRow(panel, row++, this.formatOptionsCards);

		final GridBagConstraints filler = new GridBagConstraints();
		filler.gridx = 0;
		filler.gridy = row;
		filler.gridwidth = 2;
		filler.weighty = 1.0;
		filler.fill = GridBagConstraints.VERTICAL;
		panel.add(new JPanel(), filler);

		final JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	private JPanel createPdfOptionsPane() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("PDF options"));
		int row = 0;
		this.addRow(panel, row++, "Page", this.pdfPageFormatSelector);
		this.addRow(panel, row++, "Orientation", this.pdfOrientationSelector);

		final JPanel sizePanel = new JPanel(new GridBagLayout());
		this.addRow(sizePanel, 0, "Width", this.pdfCustomWidthField);
		this.addRow(sizePanel, 1, "Height", this.pdfCustomHeightField);
		this.addRow(panel, row++, "Custom size", sizePanel);

		final JPanel marginsPanel = new JPanel(new GridBagLayout());
		this.addRow(marginsPanel, 0, "Top", this.pdfMarginTopField);
		this.addRow(marginsPanel, 1, "Right", this.pdfMarginRightField);
		this.addRow(marginsPanel, 2, "Bottom", this.pdfMarginBottomField);
		this.addRow(marginsPanel, 3, "Left", this.pdfMarginLeftField);
		this.addRow(panel, row++, "Margins", marginsPanel);

		this.addRow(panel, row++, "Template under", this.createTemplateChooserPanel(this.pdfUnderTemplateField));
		this.addRow(panel, row++, "Template over", this.createTemplateChooserPanel(this.pdfOverTemplateField));
		this.addRow(panel, row++, "Header", this.pdfHeaderField);
		this.addRow(panel, row++, "Footer", this.pdfFooterField);
		final JLabel hint = new JLabel("Fields: " + String.join(", ", ViewExportDialog.PDF_TEXT_TOKENS));
		this.addFullRow(panel, row++, hint);
		return panel;
	}

	private JScrollPane createPreviewPane() {
		final JScrollPane scrollPane = new JScrollPane(this.previewPanel);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Preview"));
		return scrollPane;
	}

	private JPanel createTemplateChooserPanel(final JTextField field) {
		final JPanel panel = new JPanel(new BorderLayout(4, 0));
		final JButton browseButton = new JButton("Browse...");
		panel.add(field, BorderLayout.CENTER);
		panel.add(browseButton, BorderLayout.EAST);
		browseButton.addActionListener(event -> this.browseTemplateFile(field));
		return panel;
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
		this.formatSelector.addActionListener(event -> {
			this.updateFormatOptionsVisibility();
			this.updateExportButtonState();
			this.refreshPreview();
		});
		this.scopeSelector.addActionListener(event -> this.refreshPreview());
		for (final JCheckBox checkBox : this.panelTypeBoxes.values()) {
			checkBox.addActionListener(event -> {
				this.updateExportButtonState();
				this.refreshPreview();
			});
		}
		this.filePatternField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateExportButtonState));
		this.outputDirectoryField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateExportButtonState));
		this.installValidationListener(this.pdfCustomWidthField);
		this.installValidationListener(this.pdfCustomHeightField);
		this.installValidationListener(this.pdfMarginTopField);
		this.installValidationListener(this.pdfMarginRightField);
		this.installValidationListener(this.pdfMarginBottomField);
		this.installValidationListener(this.pdfMarginLeftField);
	}

	private void installValidationListener(final JTextField textField) {
		textField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateExportButtonState));
	}

	private File optionalFile(final JTextField textField) {
		final String text = textField.getText() == null ? "" : textField.getText().trim();
		return text.isBlank() ? null : new File(text);
	}

	private String panelTypeLabel(final PanelType panelType) {
		return switch (panelType) {
		case CONCEPTUAL -> "Conceptual";
		case LOGICAL -> "Logical";
		case PHYSICAL -> "Physical";
		};
	}

	private double parseDouble(final JTextField field, final double defaultValue) {
		try {
			return Math.max(0.0, Double.parseDouble(field.getText().trim()));
		} catch (final NumberFormatException ex) {
			return defaultValue;
		}
	}

	private boolean nonNegativeNumberField(final JTextField field) {
		try {
			return Double.parseDouble(field.getText().trim()) >= 0.0;
		} catch (final NumberFormatException ex) {
			return false;
		}
	}

	private boolean positiveNumberField(final JTextField field) {
		try {
			return Double.parseDouble(field.getText().trim()) > 0.0;
		} catch (final NumberFormatException ex) {
			return false;
		}
	}

	private void refreshPreview() {
		final DiagramCanvas canvas = this.findPreviewCanvas();
		this.previewPanel.setPreview(canvas, (ViewExportScope) this.scopeSelector.getSelectedItem());
	}

	private void saveResultAndClose() {
		final ViewExportFormat format = (ViewExportFormat) this.formatSelector.getSelectedItem();
		this.result = new ViewExportRequest(format,
				(ViewExportScope) this.scopeSelector.getSelectedItem(),
				this.getSelectedPanelTypes(),
				new File(this.outputDirectoryField.getText()),
				this.filePatternField.getText(),
				false,
				false,
				this.backgroundColorButton.getSelectedColor(),
				this.buildExportOptions(format));
		this.dispose();
	}

	private void updateFormatOptionsVisibility() {
		final ViewExportFormat format = (ViewExportFormat) this.formatSelector.getSelectedItem();

		if (format == ViewExportFormat.PDF) {
			this.formatOptionsLayout.show(this.formatOptionsCards, ViewExportDialog.PDF_CARD);
		} else {
			this.formatOptionsLayout.show(this.formatOptionsCards, ViewExportDialog.IMAGE_CARD);
		}

		this.formatOptionsCards.revalidate();
		this.formatOptionsCards.repaint();

		this.transparentBackgroundBox.setEnabled(format != null && format.supportsTransparency());
		if (format != null && !format.supportsTransparency()) {
			this.transparentBackgroundBox.setSelected(false);
		}
	}

	private void updateExportButtonState() {
		final boolean pdfSettingsValid = this.formatSelector.getSelectedItem() != ViewExportFormat.PDF
				|| this.positiveNumberField(this.pdfCustomWidthField) && this.positiveNumberField(this.pdfCustomHeightField)
						&& this.nonNegativeNumberField(this.pdfMarginTopField) && this.nonNegativeNumberField(this.pdfMarginRightField)
						&& this.nonNegativeNumberField(this.pdfMarginBottomField) && this.nonNegativeNumberField(this.pdfMarginLeftField);
		this.exportButton.setEnabled(!this.getSelectedPanelTypes().isEmpty() && !this.outputDirectoryField.getText().isBlank()
				&& !this.filePatternField.getText().isBlank() && pdfSettingsValid);
	}

}
