package lu.kbra.modelizer_next.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.AppConfig;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.ThemeMode;
import lu.kbra.modelizer_next.bootstrap.AbstractBootstrapRuntime;
import lu.kbra.modelizer_next.bootstrap.AvailableUpdate;
import lu.kbra.modelizer_next.bootstrap.UpdateChannel;
import lu.kbra.modelizer_next.common.VersionComparator;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.json.LegacyModelizerImporter;
import lu.kbra.modelizer_next.json.ModernModelizerImporter;
import lu.kbra.modelizer_next.json.OnlineModelizerImporter;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.dialogs.StylePaletteEditorDialog;
import lu.kbra.pclib.PCUtils;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 6643164008640695591L;
	private final DocumentSession session;
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

	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;

	private MainFrame(final DocumentSession session) {
		super("Modelizer Next");
		this.session = session;
		this.document = session.getDocument();

		this.setLayout(new BorderLayout());
		this.installCloseHandling();

		this.appConfig = App.loadConfig();
		this.palettes = StylePaletteService.loadAll();

		this.statusLabel = new JLabel("Left drag: move object   |   Middle drag: pan   |   Mouse wheel: zoom   |   Right drag: create link",
				SwingConstants.LEFT);
		this.selectionPathLabel = new JLabel("No selection", SwingConstants.RIGHT);

		final CanvasEventListener canvasListener = new CanvasEventListener() {

			@Override
			public void onDocumentChanged() {
				MainFrame.this.onDocumentChanged();
			}

			@Override
			public void onSelectionChanged(final SelectionInfo selectionInfo) {
				if (MainFrame.this.getActiveCanvas() != null
						&& MainFrame.this.getActiveCanvas().getPanelType() == selectionInfo.panelType()) {
					MainFrame.this.updateSelectionLabel(selectionInfo);
				}
			}

		};

		this.conceptualCanvas = new DiagramCanvas(this.document, PanelType.CONCEPTUAL, canvasListener);
		this.logicalCanvas = new DiagramCanvas(this.document, PanelType.LOGICAL, canvasListener);
		this.physicalCanvas = new DiagramCanvas(this.document, PanelType.PHYSICAL, canvasListener);
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
		this.refreshToolbarLabels();
		this.updateUndoRedoMenuItems();
		this.refreshFrameTitle();
	}

	public MainFrame(final ModelDocument document) {
		this(new DocumentSession(document));
	}

	private void applyDefaultPaletteToCanvases() {
		final StylePalette palette = this.findPaletteByName(this.appConfig.getDefaultPaletteName());
		this.conceptualCanvas.setDefaultPalette(palette);
		this.logicalCanvas.setDefaultPalette(palette);
		this.physicalCanvas.setDefaultPalette(palette);
	}

	private void applyThemeAndReopen(final ThemeMode mode) {
		this.appConfig.setThemeMode(mode);
		App.saveConfig(this.appConfig);

		final int choice = JOptionPane.showConfirmDialog(this,
				"Theme change requires restarting the window.\nReopen now with the current document?",
				"Apply theme",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		this.reopenWithCurrentDocument();
	}

	private void attemptClose() {
		if (this.confirmCloseWithSave("Do you want to save changes before closing?")) {
			this.dispose();
		}
	}

	private Optional<AbstractBootstrapRuntime> bootstrapRuntime() {
		return AbstractBootstrapRuntime.isActive() ? Optional.of(AbstractBootstrapRuntime.getInstance()) : Optional.empty();
	}

	private void checkForUpdatesManually() {
		final Optional<AbstractBootstrapRuntime> runtime = this.bootstrapRuntime();
		if (runtime.isPresent()) {
			JOptionPane.showMessageDialog(this,
					"Updates are only available when the application is launched through the bootstrap launcher.",
					"Updates unavailable",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		new SwingWorker<AvailableUpdate, Void>() {
			@Override
			protected AvailableUpdate doInBackground() throws Exception {
				return runtime.get().checkForUpdates();
			}

			@Override
			protected void done() {
				try {
					final AvailableUpdate update = this.get();
					if (update == null || !update.isUpdateAvailable()) {
						JOptionPane.showMessageDialog(MainFrame.this,
								"You are already using the latest version for the selected channel.",
								"No updates available",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					final StringBuilder message = new StringBuilder();
					message.append("A new ")
							.append(update.channel().displayName().toLowerCase())
							.append(" build is available.\n\n")
							.append("Current version: ")
							.append(update.currentVersion())
							.append("\nLatest version: ")
							.append(update.latestVersion());
					if (update.notes() != null && !update.notes().isBlank()) {
						message.append("\n\n").append(update.notes());
					}
					message.append("\n\nThe application will close after the update is installed.");

					final int choice = JOptionPane.showConfirmDialog(MainFrame.this,
							message.toString(),
							"Update available",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
					if (choice == JOptionPane.YES_OPTION) {
						runtime.get().installUpdateAndExit(MainFrame.this, update, MainFrame.this::prepareForUpdateInstall);
					}
				} catch (final Exception ex) {
					final Throwable cause = ex.getCause() == null ? ex : ex.getCause();
					JOptionPane.showMessageDialog(MainFrame.this,
							"Failed to check for updates:\n" + cause.getMessage(),
							"Update error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	private boolean confirmCloseWithSave(final String prompt) {
		if (!this.session.isDirty()) {
			return true;
		}

		final int choice = JOptionPane
				.showConfirmDialog(this, prompt, "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
			return false;
		}

		return choice != JOptionPane.YES_OPTION || this.saveDocument();
	}

	private boolean confirmModernDocumentVersion(final ModelDocument loadedDocument) {
		final String fileVersion = loadedDocument.getMeta() == null ? null : loadedDocument.getMeta().getApplicationVersion();

		if (fileVersion != null && !fileVersion.isBlank() && VersionComparator.COMPARATOR.compare(fileVersion, App.VERSION) > 0) {
			final int choice = JOptionPane.showConfirmDialog(this,
					"This file was created with a newer version of the application (" + fileVersion
							+ ").\nDo you want to try to load the file anyways ?",
					"Newer file version",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			return choice == JOptionPane.YES_OPTION;
		}

		return true;
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

	private JMenu createEditMenu() {
		final JMenu editMenu = new JMenu("Edit");

		this.undoMenuItem = new JMenuItem("Undo");
		this.undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		this.undoMenuItem.addActionListener(event -> this.undo());

		this.redoMenuItem = new JMenuItem("Redo");
		this.redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		this.redoMenuItem.addActionListener(event -> this.redo());

		editMenu.add(this.undoMenuItem);
		editMenu.add(this.redoMenuItem);
		return editMenu;
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

	private JMenu createHelpMenu() {
		final JMenu helpMenu = new JMenu("Help");

		final JMenuItem checkForUpdates = new JMenuItem("Check for updates...");
		checkForUpdates.addActionListener(event -> this.checkForUpdatesManually());
		helpMenu.add(checkForUpdates);

		final Optional<AbstractBootstrapRuntime> bootstrapRuntime = this.bootstrapRuntime();
		final boolean updateRuntimeAvailable = bootstrapRuntime != null;

		final JCheckBoxMenuItem autoCheckUpdates = new JCheckBoxMenuItem("Check for updates on startup",
				updateRuntimeAvailable && bootstrapRuntime.get().isAutoCheckUpdates());
		autoCheckUpdates.setEnabled(updateRuntimeAvailable && bootstrapRuntime.get().isAutomaticUpdateChecksEnabledByProperty());
		autoCheckUpdates
				.addActionListener(event -> this.bootstrapRuntime().ifPresent(c -> c.setAutoCheckUpdates(autoCheckUpdates.isSelected())));
		helpMenu.add(autoCheckUpdates);

		final JMenu channelMenu = new JMenu("Update channel");
		channelMenu.setEnabled(updateRuntimeAvailable);
		final ButtonGroup channelGroup = new ButtonGroup();
		final UpdateChannel selectedChannel = updateRuntimeAvailable ? bootstrapRuntime.get().getSelectedChannel() : UpdateChannel.RELEASE;
		for (final UpdateChannel updateChannel : UpdateChannel.values()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(updateChannel.displayName());
			item.setSelected(updateChannel == selectedChannel);
			item.addActionListener(event -> this.bootstrapRuntime().ifPresent(c -> c.setSelectedChannel(updateChannel)));
			channelGroup.add(item);
			channelMenu.add(item);
		}
		helpMenu.add(channelMenu);

		if (!updateRuntimeAvailable) {
			final JMenuItem standaloneInfo = new JMenuItem("Standalone mode: bootstrap updates unavailable");
			standaloneInfo.setEnabled(false);
			helpMenu.add(standaloneInfo);
		}

		return helpMenu;
	}

	private JMenuBar createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu("File");
		fileMenu.add(this.createFileMenuItem("New", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), this::newDocument));
		fileMenu.add(this.createFileMenuItem("Load", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), this::loadDocument));
		fileMenu.add(this.createFileMenuItem("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), this::saveDocument));
		fileMenu.add(this.createFileMenuItem("Save As...",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				this::saveDocumentAs));

		final JMenu editMenu = this.createEditMenu();

		final JMenu insertMenu = new JMenu("Insert");
		insertMenu
				.add(this.createCanvasMenuItem("New table", "addTable", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK)));
		insertMenu
				.add(this.createCanvasMenuItem("New field", "addField", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)));
		insertMenu.add(
				this.createCanvasMenuItem("New comment", "addComment", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)));
		insertMenu.add(this.createCanvasMenuItem("New link", "addLink", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK)));

		final JMenu appearanceMenu = new JMenu("Appearance");
		final ButtonGroup group = new ButtonGroup();
		appearanceMenu.add(this.createThemeItem("Light", ThemeMode.LIGHT, group));
		appearanceMenu.add(this.createThemeItem("Dark", ThemeMode.DARK, group));
		appearanceMenu.add(this.createThemeItem("Follow system", ThemeMode.SYSTEM, group));

		final JMenu stylesMenu = new JMenu("Styles");
		this.populateStylesMenu(stylesMenu);

		final JMenu helpMenu = this.createHelpMenu();

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(insertMenu);
		menuBar.add(appearanceMenu);
		menuBar.add(stylesMenu);
		menuBar.add(helpMenu);

		this.updateUndoRedoMenuItems();
		return menuBar;
	}

	private JFileChooser createOpenFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Model files (*.mn, *.mod, *.mdlz)", "mn", "mod", "mdlz"));
		return chooser;
	}

	private JFileChooser createSaveFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Modelizer Next (*.mn)", "mn"));
		return chooser;
	}

	private JRadioButtonMenuItem createThemeItem(final String text, final ThemeMode mode, final ButtonGroup group) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		item.setSelected(this.appConfig.getThemeMode() == mode);
		item.addActionListener(event -> this.applyThemeAndReopen(mode));
		group.add(item);
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

	private void installCloseHandling() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				MainFrame.this.attemptClose();
			}
		});
	}

	private void loadDocument() {
		final JFileChooser chooser = this.createOpenFileChooser();
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		final File selectedFile = chooser.getSelectedFile();
		final String extension = PCUtils.getFileExtension(selectedFile.getName());

		try {
			final ModelDocument loadedDocument;
			final File openedFile;

			switch (extension) {
			case "mod" -> {
				final int choice = JOptionPane.showConfirmDialog(this,
						"This file comes from an older version of Modelizer.\nThere may be errors or unsupported elements during import.\nDo you want to continue?",
						"Legacy Modelizer import",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.YES_OPTION) {
					return;
				}

				loadedDocument = LegacyModelizerImporter.importFile(selectedFile);
				openedFile = null;
			}
			case "mdlz" -> {
				loadedDocument = OnlineModelizerImporter.importFile(selectedFile);
				openedFile = null;
			}
			case "mn" -> {
				loadedDocument = ModernModelizerImporter.importFile(selectedFile);
				if (!this.confirmModernDocumentVersion(loadedDocument)) {
					return;
				}
				openedFile = selectedFile;
			}
			default -> throw new IOException("Unsupported file extension: ." + extension);
			}

			if (loadedDocument == null) {
				return;
			}

			loadedDocument.setSource(selectedFile.getPath());
			this.openInNewFrame(new DocumentSession(loadedDocument, openedFile));
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog(this, "Failed to load file:\n" + ex.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void newDocument() {
		final ModelDocument newDocument = new ModelDocument();
		newDocument.setSource("New document");
		this.openInNewFrame(new DocumentSession(newDocument));
	}

	private void onDocumentChanged() {
		this.session.markChanged();
		this.updateUndoRedoMenuItems();
		this.refreshFrameTitle();
	}

	private void openInNewFrame(final DocumentSession session) {
		final MainFrame frame = new MainFrame(session);
		frame.setVisible(true);
		this.dispose();
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
		noneItem.setSelected(this.appConfig.getDefaultPaletteName() == null || this.appConfig.getDefaultPaletteName().isBlank());
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

	private boolean prepareForUpdateInstall() throws IOException {
		return this.confirmCloseWithSave("Do you want to save changes before installing the update?");
	}

	private void redo() {
		if (!this.session.redo()) {
			return;
		}

		this.refreshAfterHistoryRestore();
	}

	private void refreshAfterHistoryRestore() {
		this.conceptualCanvas.resetUiAfterDocumentRestore();
		this.logicalCanvas.resetUiAfterDocumentRestore();
		this.physicalCanvas.resetUiAfterDocumentRestore();
		this.updateSelectionLabel(this.getActiveCanvas().getSelectionInfo());
		this.refreshToolbarLabels();
		this.updateUndoRedoMenuItems();
		this.refreshFrameTitle();
		this.revalidate();
		this.repaint();
	}

	private void refreshFrameTitle() {
		final String source = this.document.getSource() == null || this.document.getSource().isBlank() ? "Untitled"
				: this.document.getSource();
		this.setTitle((this.session.isDirty() ? "* " : "") + App.title(source));
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

	private void reopenWithCurrentDocument() {
		this.dispose();

		SwingUtilities.invokeLater(() -> {
			MNMain.applyConfiguredLookAndFeel();

			final MainFrame frame = new MainFrame(this.session);
			frame.setVisible(true);
		});
	}

	private boolean saveDocument() {
		if (this.session.getCurrentFile() == null) {
			return this.saveDocumentAs();
		}
		return this.writeDocument(this.session.getCurrentFile());
	}

	private boolean saveDocumentAs() {
		final JFileChooser chooser = this.createSaveFileChooser();
		if (this.session.getCurrentFile() != null) {
			chooser.setSelectedFile(this.session.getCurrentFile());
		}

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		File selectedFile = chooser.getSelectedFile();
		if (!selectedFile.getName().toLowerCase().endsWith(".mn")) {
			selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".mn");
		}

		return this.writeDocument(selectedFile);
	}

	private void undo() {
		if (!this.session.undo()) {
			return;
		}

		this.refreshAfterHistoryRestore();
	}

	private void updateSelectionLabel(final SelectionInfo selectionInfo) {
		final String path = selectionInfo == null || selectionInfo.path() == null || selectionInfo.path().isBlank() ? "No selection"
				: selectionInfo.path();
		this.selectionPathLabel.setText(path);
	}

	private void updateUndoRedoMenuItems() {
		if (this.undoMenuItem != null) {
			this.undoMenuItem.setEnabled(this.session.canUndo());
		}
		if (this.redoMenuItem != null) {
			this.redoMenuItem.setEnabled(this.session.canRedo());
		}
	}

	private boolean writeDocument(final File file) {
		try {
			this.document.getMeta().setUpdatedAt(Instant.now());
			this.document.getMeta().setApplicationVersion(App.VERSION);
			this.document.setSource(file.getPath());

			MNMain.OBJECT_MAPPER.writeValue(file, this.document);
			this.session.markSaved(file);
			this.updateUndoRedoMenuItems();
			this.refreshFrameTitle();
			return true;
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog(this, "Failed to save file:\n" + ex.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

}
