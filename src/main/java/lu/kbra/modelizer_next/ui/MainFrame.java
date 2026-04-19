package lu.kbra.modelizer_next.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.AppConfig;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.ThemeMode;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.dialogs.StylePaletteEditorDialog;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 6643164008640695591L;
	private File currentFile;
	private final ModelDocument document;
	private final JLabel statusLabel;
	private final JLabel selectionPathLabel;
	private final JTabbedPane tabbedPane;
	private final DiagramCanvas conceptualCanvas;
	private final DiagramCanvas logicalCanvas;
	private final DiagramCanvas physicalCanvas;
	private final JToolBar toolBar;

	private final AppConfig appConfig;
	private List<StylePalette> palettes;

	public MainFrame(final ModelDocument document) {
		super("Modelizer Next");
		this.document = document;

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.appConfig = App.loadConfig();
		this.palettes = StylePaletteService.loadAll();

		this.statusLabel = new JLabel(
				"Left drag: move object   |   Middle drag: pan   |   Mouse wheel: zoom   |   Right drag: create link",
				SwingConstants.LEFT);
		this.selectionPathLabel = new JLabel("No selection", SwingConstants.RIGHT);

		final CanvasStatusListener statusListener = selectionInfo -> {
			if (this.getActiveCanvas() != null && this.getActiveCanvas().getPanelType() == selectionInfo.panelType()) {
				this.updateSelectionLabel(selectionInfo);
			}
		};

		this.conceptualCanvas = new DiagramCanvas(this.document, PanelType.CONCEPTUAL, statusListener);
		this.logicalCanvas = new DiagramCanvas(this.document, PanelType.LOGICAL, statusListener);
		this.physicalCanvas = new DiagramCanvas(this.document, PanelType.PHYSICAL, statusListener);
		this.applyDefaultPaletteToCanvases();

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.addTab("Conceptual", this.conceptualCanvas);
		this.tabbedPane.addTab("Logical", this.logicalCanvas);
		this.tabbedPane.addTab("Physical", this.physicalCanvas);
		this.tabbedPane.addChangeListener(event -> {
			this.updateSelectionLabel(this.getActiveCanvas().getSelectionInfo());
			this.refreshToolbarLabels();
		});

		this.setJMenuBar(this.createMenuBar());

		this.toolBar = this.createToolBar();
		this.add(this.toolBar, BorderLayout.NORTH);

		final JPanel statusPanel = new JPanel(new BorderLayout(12, 0));
		statusPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
		statusPanel.add(this.statusLabel, BorderLayout.WEST);
		statusPanel.add(this.selectionPathLabel, BorderLayout.EAST);

		this.add(this.tabbedPane, BorderLayout.CENTER);
		this.add(statusPanel, BorderLayout.SOUTH);
		this.setSize(1200, 800);
		this.setLocationRelativeTo(null);

		this.updateSelectionLabel(this.getActiveCanvas().getSelectionInfo());
	}

	private void refreshToolbarLabels() {
		for (int i = 0; i < this.toolBar.getComponentCount(); i++) {
			if (this.toolBar.getComponent(i) instanceof final JButton button) {
				final String actionKey = (String) button.getClientProperty("actionKey");
				final String baseText = (String) button.getClientProperty("baseText");
				if (actionKey == null || baseText == null) {
					continue;
				}

				final DiagramCanvas canvas = this.getActiveCanvas();
				final String shortcutText = canvas == null ? "" : this.findShortcutText(canvas, actionKey);
				button.setText(shortcutText.isBlank() ? baseText : baseText + " (" + shortcutText + ")");
			}
		}
	}

	private JButton createToolbarButton(final String text, final String actionKey) {
		final JButton button = new JButton(text);
		button.putClientProperty("baseText", text);
		button.putClientProperty("actionKey", actionKey);

		button.addActionListener(event -> {
			final DiagramCanvas canvas = this.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final ActionEvent actionEvent = new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey);
			final javax.swing.Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(actionEvent);
				canvas.requestFocusInWindow();
			}
		});

		final DiagramCanvas canvas = this.getActiveCanvas();
		if (canvas != null) {
			final String shortcutText = this.findShortcutText(canvas, actionKey);
			if (!shortcutText.isBlank()) {
				button.setText(text + " (" + shortcutText + ")");
			}
		}

		return button;
	}

	private JMenuBar createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu("File");
		fileMenu.add(this.createFileMenuItem("New", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
				this::newDocument));
		fileMenu.add(this.createFileMenuItem("Load", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
				this::loadDocument));
		fileMenu.add(this.createFileMenuItem("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
				this::saveDocument));
		fileMenu.add(this.createFileMenuItem("Save As...",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				this::saveDocumentAs));

		final JMenu insertMenu = new JMenu("Insert");
		insertMenu.add(this.createCanvasMenuItem("New table", "addTable",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK)));
		insertMenu.add(this.createCanvasMenuItem("New field", "addField",
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)));
		insertMenu.add(this.createCanvasMenuItem("New comment", "addComment",
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)));
		insertMenu.add(this.createCanvasMenuItem("New link", "addLink",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK)));

		final JMenu appearanceMenu = new JMenu("Appearance");
		final ButtonGroup group = new ButtonGroup();
		appearanceMenu.add(this.createThemeItem("Light", ThemeMode.LIGHT, group));
		appearanceMenu.add(this.createThemeItem("Dark", ThemeMode.DARK, group));
		appearanceMenu.add(this.createThemeItem("Follow system", ThemeMode.SYSTEM, group));

		final JMenu stylesMenu = new JMenu("Styles");
		this.populateStylesMenu(stylesMenu);

		menuBar.add(fileMenu);
		menuBar.add(insertMenu);
		menuBar.add(appearanceMenu);
		menuBar.add(stylesMenu);

		return menuBar;
	}

	private StylePalette findPaletteByName(final String paletteName) {
		if (paletteName == null || paletteName.isBlank()) {
			return null;
		}

		for (final StylePalette palette : this.palettes) {
			if (paletteName.equals(palette.getName())) {
				return palette;
			}
		}

		return null;
	}

	private void applyDefaultPaletteToCanvases() {
		final StylePalette palette = this.findPaletteByName(this.appConfig.getDefaultPaletteName());
		this.conceptualCanvas.setDefaultPalette(palette);
		this.logicalCanvas.setDefaultPalette(palette);
		this.physicalCanvas.setDefaultPalette(palette);
	}

	private void populateStylesMenu(final JMenu stylesMenu) {
		stylesMenu.removeAll();

		final JMenuItem newPaletteItem = new JMenuItem("New palette...");
		newPaletteItem.addActionListener(event -> {
			final StylePalette palette = StylePaletteEditorDialog.showDialog(this);
			if (palette == null) {
				return;
			}

			StylePaletteService.save(palette);
			this.palettes = StylePaletteService.loadAll();
			this.setJMenuBar(this.createMenuBar());
			this.applyDefaultPaletteToCanvases();
			this.revalidate();
			this.repaint();
		});

		final JMenu applyMenu = new JMenu("Apply style");
		for (final StylePalette palette : this.palettes) {
			final JMenuItem item = new JMenuItem(palette.getName());
			item.addActionListener(event -> {
				final DiagramCanvas canvas = this.getActiveCanvas();
				if (canvas != null) {
					canvas.applyPalette(palette);
					this.appConfig.setSelectedPaletteName(palette.getName());
					App.saveConfig(this.appConfig);
				}
			});
			applyMenu.add(item);
		}

		final JMenu editMenu = new JMenu("Edit style");
		for (final StylePalette palette : this.palettes) {
			final JMenuItem item = new JMenuItem(palette.getName());
			item.addActionListener(event -> {
				final String oldName = palette.getName();
				final StylePalette edited = StylePaletteEditorDialog.showDialog(this, palette);
				if (edited == null) {
					return;
				}

				if (!oldName.equals(edited.getName())) {
					StylePaletteService.deleteByName(oldName);
					if (oldName.equals(this.appConfig.getDefaultPaletteName())) {
						this.appConfig.setDefaultPaletteName(edited.getName());
					}
					if (oldName.equals(this.appConfig.getSelectedPaletteName())) {
						this.appConfig.setSelectedPaletteName(edited.getName());
					}
					App.saveConfig(this.appConfig);
				}

				StylePaletteService.save(edited);
				this.palettes = StylePaletteService.loadAll();
				this.applyDefaultPaletteToCanvases();
				this.setJMenuBar(this.createMenuBar());
				this.revalidate();
				this.repaint();
			});
			editMenu.add(item);
		}

		final JMenu defaultMenu = new JMenu("Default style");
		final ButtonGroup defaultGroup = new ButtonGroup();

		final JRadioButtonMenuItem noneItem = new JRadioButtonMenuItem("None");
		noneItem.setSelected(
				this.appConfig.getDefaultPaletteName() == null || this.appConfig.getDefaultPaletteName().isBlank());
		noneItem.addActionListener(event -> {
			this.appConfig.setDefaultPaletteName(null);
			App.saveConfig(this.appConfig);
			this.applyDefaultPaletteToCanvases();
		});
		defaultGroup.add(noneItem);
		defaultMenu.add(noneItem);

		defaultMenu.addSeparator();

		for (final StylePalette palette : this.palettes) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(palette.getName());
			item.setSelected(palette.getName().equals(this.appConfig.getDefaultPaletteName()));
			item.addActionListener(event -> {
				this.appConfig.setDefaultPaletteName(palette.getName());
				App.saveConfig(this.appConfig);
				this.applyDefaultPaletteToCanvases();
			});
			defaultGroup.add(item);
			defaultMenu.add(item);
		}

		final JMenuItem reloadItem = new JMenuItem("Reload styles");
		reloadItem.addActionListener(event -> {
			this.palettes = StylePaletteService.loadAll();
			this.applyDefaultPaletteToCanvases();
			this.setJMenuBar(this.createMenuBar());
			this.revalidate();
			this.repaint();
		});

		stylesMenu.add(newPaletteItem);
		stylesMenu.addSeparator();
		stylesMenu.add(applyMenu);
		stylesMenu.add(editMenu);
		stylesMenu.add(defaultMenu);
		stylesMenu.addSeparator();
		stylesMenu.add(reloadItem);
	}

	private void applyThemeAndReopen(final ThemeMode mode) {
		this.appConfig.setThemeMode(mode);
		App.saveConfig(this.appConfig);

		final int choice = JOptionPane.showConfirmDialog(this,
				"Theme change requires restarting the window.\nReopen now with the current document?", "Apply theme",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		this.reopenWithCurrentDocument();
	}

	private void reopenWithCurrentDocument() {
		this.dispose();

		SwingUtilities.invokeLater(() -> {
			MNMain.applyConfiguredLookAndFeel();

			final MainFrame frame = new MainFrame(this.document);
			frame.currentFile = this.currentFile;
			frame.setTitle(App.title(this.document.getSource()));
			frame.setVisible(true);
		});
	}

	private JRadioButtonMenuItem createThemeItem(final String text, final ThemeMode mode, final ButtonGroup group) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		item.setSelected(this.appConfig.getThemeMode() == mode);
		item.addActionListener(event -> this.applyThemeAndReopen(mode));
		group.add(item);
		return item;
	}

	private JMenuItem createFileMenuItem(final String text, final KeyStroke keyStroke, final Runnable action) {
		final JMenuItem item = new JMenuItem(new AbstractAction(text) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
		item.setAccelerator(keyStroke);
		return item;
	}

	private void newDocument() {
		final ModelDocument newDocument = new ModelDocument();
		newDocument.setSource("New document");
		this.openInNewFrame(newDocument, null);
	}

	private void loadDocument() {
		final JFileChooser chooser = this.createFileChooser();
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		final File selectedFile = chooser.getSelectedFile();
		try {
			final ModelDocument loadedDocument = MNMain.OBJECT_MAPPER.readValue(selectedFile, ModelDocument.class);
			loadedDocument.setSource(selectedFile.getPath());
			final String fileVersion = loadedDocument.getMeta() == null ? null
					: loadedDocument.getMeta().getApplicationVersion();

			if (fileVersion != null && !fileVersion.isBlank()
					&& VersionComparator.COMPARATOR.compare(fileVersion, App.VERSION) > 0) {
				final int choice = JOptionPane.showConfirmDialog(this,
						"This file was created with a newer version of the application (" + fileVersion
								+ ").\nDo you want to try to load the file anyways ?",
						"Newer file version", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.YES_OPTION) {
					return;
				}
			}

			this.openInNewFrame(loadedDocument, selectedFile);
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog(this, "Failed to load file:\n" + ex.getMessage(), "Load error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveDocument() {
		if (this.currentFile == null) {
			this.saveDocumentAs();
			return;
		}
		this.writeDocument(this.currentFile);
	}

	private void saveDocumentAs() {
		final JFileChooser chooser = this.createFileChooser();
		if (this.currentFile != null) {
			chooser.setSelectedFile(this.currentFile);
		}

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File selectedFile = chooser.getSelectedFile();
		if (!selectedFile.getName().toLowerCase().endsWith(".mn")) {
			selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".mn");
		}

		this.writeDocument(selectedFile);
	}

	private void writeDocument(final File file) {
		try {
			this.document.getMeta().setUpdatedAt(Instant.now());
			this.document.getMeta().setApplicationVersion(App.VERSION);
			this.document.setSource(file.getPath());

			MNMain.OBJECT_MAPPER.writeValue(file, this.document);
			this.currentFile = file;
			this.setTitle(App.title(this.document.getSource()));
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog(this, "Failed to save file:\n" + ex.getMessage(), "Save error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private JFileChooser createFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Modelizer Next (*.mn)", "mn"));
		return chooser;
	}

	private void openInNewFrame(final ModelDocument modelDocument, final File file) {
		final MainFrame frame = new MainFrame(modelDocument);
		frame.currentFile = file;
		frame.setTitle(App.title(modelDocument.getSource()));
		frame.setVisible(true);
		this.dispose();
	}

	private JMenuItem createCanvasMenuItem(final String text, final String actionKey, final KeyStroke keyStroke) {
		final JMenuItem item = new JMenuItem(text);
		item.setAccelerator(keyStroke);
		item.addActionListener(event -> {
			final DiagramCanvas canvas = this.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final javax.swing.Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey));
				canvas.requestFocusInWindow();
			}
		});
		return item;
	}

	private JToolBar createToolBar() {
		final JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		toolbar.add(this.createToolbarButton("New table", "addTable"));
		toolbar.add(this.createToolbarButton("New field", "addField"));
		toolbar.add(this.createToolbarButton("New comment", "addComment"));
		toolbar.add(this.createToolbarButton("New link", "addLink"));
		toolbar.addSeparator();
		toolbar.add(this.createToolbarButton("Rename", "renameSelection"));
		toolbar.add(this.createToolbarButton("Delete", "deleteSelection"));
		toolbar.add(this.createToolbarButton("Duplicate", "duplicateSelection"));

		return toolbar;
	}

	private String findShortcutText(final DiagramCanvas canvas, final String actionKey) {
		for (final KeyStroke keyStroke : canvas.getInputMap(JComponent.WHEN_FOCUSED).allKeys()) {
			if (keyStroke == null) {
				continue;
			}
			final Object mapped = canvas.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
			if (actionKey.equals(mapped)) {
				return this.formatKeyStroke(keyStroke);
			}
		}
		return "";
	}

	private String formatKeyStroke(final KeyStroke keyStroke) {
		final StringBuilder builder = new StringBuilder();

		final int modifiers = keyStroke.getModifiers();
		if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
			builder.append("Ctrl+");
		}
		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
			builder.append("Shift+");
		}
		if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
			builder.append("Alt+");
		}

		builder.append(KeyEvent.getKeyText(keyStroke.getKeyCode()));
		return builder.toString();
	}

	private DiagramCanvas getActiveCanvas() {
		return switch (this.tabbedPane.getSelectedIndex()) {
		case 0 -> this.conceptualCanvas;
		case 1 -> this.logicalCanvas;
		case 2 -> this.physicalCanvas;
		default -> this.conceptualCanvas;
		};
	}

	private void updateSelectionLabel(final SelectionInfo selectionInfo) {
		final String path = selectionInfo == null || selectionInfo.path() == null || selectionInfo.path().isBlank()
				? "No selection"
				: selectionInfo.path();
		this.selectionPathLabel.setText(path);
	}

}