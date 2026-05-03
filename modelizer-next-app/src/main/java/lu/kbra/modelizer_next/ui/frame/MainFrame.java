package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.AppConfig;
import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.bootstrap.AvailableUpdate;
import lu.kbra.modelizer_next.bootstrap.UpdateRuntime;
import lu.kbra.modelizer_next.bootstrap.UpdateRuntimes;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.ThemeMode;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectionInfo;
import lu.kbra.modelizer_next.ui.canvas.datastruct.StylePreviewType;
import lu.kbra.modelizer_next.ui.dialogs.ViewExportDialog;
import lu.kbra.modelizer_next.ui.export.ViewExportRequest;
import lu.kbra.modelizer_next.ui.export.ViewExporter;
import lu.kbra.modelizer_next.ui.impl.DocumentChangeListener;
import lu.kbra.modelizer_next.ui.impl.DocumentLoadHandler;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.pair.Pair;

import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;

public class MainFrame extends JFrame implements MainFrameDocumentController, MainFrameStyleController, MainFrameWindowController {

	private static final long serialVersionUID = 6643164008640695591L;

	public static final int CTRL_MODIFIER = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

	private static final List<Integer> WINDOW_ICON_SIZES = List.of(16, 20, 24, 32, 40, 48, 64, 128, 256);
	public static final Image ICON;
	public static final ImageIcon IMAGE_ICON;
	public static final List<Image> ICON_IMAGES;

	static {
		final Pair<List<Image>, Long> p = PCUtils.millisTime(() -> WINDOW_ICON_SIZES.stream()
				.sorted(Comparator.naturalOrder())
				.map(i -> new ImageIcon(PCUtils.readPackagedBytesFile(MainFrame.class, "/icons/icon-" + i + ".png")).getImage())
				.toList());
		ICON_IMAGES = p.getKey();
		System.out.println("Scaling icons took: " + ((double) p.getValue()) / 1_000 + "s");

		ICON = ICON_IMAGES.get(ICON_IMAGES.size() - 1);
		IMAGE_ICON = new ImageIcon(ICON);
	}

	DocumentSession session;

	ModelDocument document;

	JLabel statusLabel;
	JLabel selectionPathLabel;
	RootDockingPanel rootDockingPanel;
	DiagramCanvas activeCanvas;
	DiagramCanvas conceptualCanvas;
	DiagramCanvas logicalCanvas;
	DiagramCanvas physicalCanvas;
	MainFrameToolBar toolBar;
	JPanel pinnedStylesPanel;

	AppConfig appConfig;
	List<StylePalette> palettes;

	JMenuItem undoMenuItem;
	JMenuItem redoMenuItem;

	public MainFrame(final DocumentSession session) {
		super("Modelizer Next");
		super.setIconImages(MainFrame.ICON_IMAGES);
		System.out.println("setContent took: " + ((double) PCUtils.millisTime(() -> this.setContent(session))) / 1_000 + "s");
		this.setSize(1200, 800);
		this.setLocationRelativeTo(null);
	}

	public MainFrame(final ModelDocument document) {
		this(new DocumentSession(document));
	}

	public void applyDefaultPaletteToCanvases() {
		final StylePalette palette = this.findPaletteByName(this.appConfig.getDefaultPaletteName());
		this.conceptualCanvas.applyPalette(palette);
		this.logicalCanvas.applyPalette(palette);
		this.physicalCanvas.applyPalette(palette);
	}

	@Override
	public ModelDocument getDocument() {
		return this.document;
	}

	@Override
	public DocumentSession getSession() {
		return this.session;
	}

	public boolean loadDocument(final File selectedFile) {
		return this.loadDocumentFromFile(selectedFile);
	}

	protected void setContent(final DocumentSession session) {
		super.setTitle("Modelizer Next");

		this.setContentPane(new JPanel());
		this.clearListeners();

		this.session = session;
		this.document = session.getDocument();

		this.setLayout(new BorderLayout());
		this.installCloseHandling();

		this.appConfig = App.loadConfig();
		this.palettes = StylePaletteService.loadAll();
		this.sanitizePinnedPaletteNames();

		this.statusLabel = this.createStatusLabel();
		this.selectionPathLabel = this.createSelectionPathLabel();

		final DocumentChangeListener canvasListener = new DocumentChangeListener() {

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

			@Override
			public void undo() {
				MainFrame.this.undo();
			}

			@Override
			public void redo() {
				MainFrame.this.redo();
			}

		};

		this.conceptualCanvas = new DiagramCanvas(this.document, PanelType.CONCEPTUAL, canvasListener);
		this.logicalCanvas = new DiagramCanvas(this.document, PanelType.LOGICAL, canvasListener);
		this.physicalCanvas = new DiagramCanvas(this.document, PanelType.PHYSICAL, canvasListener);
		this.setDefaultPaletteToCanvases();

		Docking.initialize(this);
		this.rootDockingPanel = new RootDockingPanel(this);

		final DockableDiagramPanel conceptualDock = this.createDockableCanvasPanel("conceptual", "Conceptual", this.conceptualCanvas);
		final DockableDiagramPanel logicalDock = this.createDockableCanvasPanel("logical", "Logical", this.logicalCanvas);
		final DockableDiagramPanel physicalDock = this.createDockableCanvasPanel("physical", "Physical", this.physicalCanvas);

		this.activeCanvas = this.conceptualCanvas;
		Docking.dock(conceptualDock, this);
		Docking.dock(logicalDock, conceptualDock, DockingRegion.CENTER);
		Docking.dock(physicalDock, conceptualDock, DockingRegion.CENTER);
		Docking.bringToFront(conceptualDock);

		this.setJMenuBar(new MainFrameMenuBar(this));

		this.pinnedStylesPanel = this.createPinnedStylesPanel();

		this.toolBar = new MainFrameToolBar(this);
		this.toolBar.add(this.pinnedStylesPanel, BorderLayout.EAST);
		this.add(this.toolBar, BorderLayout.NORTH);

		final JPanel statusPanel = this.createStatusPanel();

		this.add(this.rootDockingPanel, BorderLayout.CENTER);
		this.add(statusPanel, BorderLayout.SOUTH);

		this.installFileDropSupport();
		this.updateSelectionLabel(this.getActiveCanvas().getSelectionInfo());
		this.refreshToolbarLabels();
		this.updateUndoRedoMenuItems();
		this.refreshFrameTitle();
		this.revalidate();
		this.repaint();
	}

	void applyThemeAndReopen(final ThemeMode mode) {
		this.appConfig.setThemeMode(mode);
		App.saveConfig(this.appConfig);
		this.reopenWithCurrentDocument();
	}

	Optional<UpdateRuntime> bootstrapRuntime() {
		return UpdateRuntimes.isActive() ? Optional.of(UpdateRuntimes.getInstance()) : Optional.empty();
	}

	void checkForUpdatesManually() {
		final Optional<UpdateRuntime> runtime = this.bootstrapRuntime();
		if (runtime.isEmpty()) {
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

	void clearListeners() {
		this.removeListener(this.getComponentListeners(), this::removeComponentListener);
		this.removeListener(this.getContainerListeners(), this::removeContainerListener);
		this.removeListener(this.getFocusListeners(), this::removeFocusListener);
		this.removeListener(this.getWindowFocusListeners(), this::removeWindowFocusListener);
		this.removeListener(this.getWindowListeners(), this::removeWindowListener);
		this.removeListener(this.getWindowStateListeners(), this::removeWindowStateListener);
		this.removeListener(this.getHierarchyBoundsListeners(), this::removeHierarchyBoundsListener);
		this.removeListener(this.getHierarchyListeners(), this::removeHierarchyListener);
		this.removeListener(this.getInputMethodListeners(), this::removeInputMethodListener);
		this.removeListener(this.getKeyListeners(), this::removeKeyListener);
		this.removeListener(this.getMouseListeners(), this::removeMouseListener);
		this.removeListener(this.getMouseMotionListeners(), this::removeMouseMotionListener);
		this.removeListener(this.getMouseWheelListeners(), this::removeMouseWheelListener);
		this.removeListener(this.getPropertyChangeListeners(), this::removePropertyChangeListener);
	}

	void exportImage() {
		final DiagramCanvas activeCanvas = this.getActiveCanvas();
		final ViewExportRequest request = ViewExportDialog.showDialog(this,
				this.getCanvasesByPanelType(),
				activeCanvas == null ? PanelType.CONCEPTUAL : activeCanvas.getPanelType(),
				this.getDefaultExportDirectory());

		if (request == null) {
			return;
		}

		try {
			final List<File> exportedFiles = ViewExporter
					.exportViews(this.getCanvasesByPanelType(), request, this.getExportSourceFileName());
			if (exportedFiles.isEmpty()) {
				JOptionPane.showMessageDialog(this, "No view was exported.", "Export", JOptionPane.WARNING_MESSAGE);
				return;
			}

			final String message = "Exported " + exportedFiles.size() + " file" + (exportedFiles.size() > 1 ? "s" : "") + ":\n"
					+ exportedFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n"));

			final Object[] options = { "Show file(s)", "Close" };

			final int choice = JOptionPane.showOptionDialog(null,
					message,
					"Export successful",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE,
					null,
					options,
					options[1]);

			if (choice == 0) {
				if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
					JOptionPane.showMessageDialog(null,
							"Your system doesn't seem to support natively opening files :(",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				Desktop.getDesktop().open(exportedFiles.size() > 1 ? request.outputDirectory() : exportedFiles.get(0));
			}
		} catch (final IOException ex) {
			JOptionPane.showMessageDialog(this, "Failed to export view:\n" + ex.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
		}
	}

	String findShortcutText(final DiagramCanvas canvas, final String actionKey) {
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

	String formatKeyStroke(final KeyStroke keyStroke) {
		final StringBuilder builder = new StringBuilder();

		final int modifiers = keyStroke.getModifiers();
		if ((modifiers & MainFrame.CTRL_MODIFIER) != 0) {
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

	DiagramCanvas getActiveCanvas() {
		return this.activeCanvas == null ? this.conceptualCanvas : this.activeCanvas;
	}

	DockableDiagramPanel createDockableCanvasPanel(final String id, final String title, final DiagramCanvas canvas) {
		return new DockableDiagramPanel("modelizer-next." + System.identityHashCode(this) + "." + id, title, canvas, () -> {
			this.activeCanvas = canvas;
			this.updateSelectionLabel(canvas.getSelectionInfo());
			this.refreshToolbarLabels();
		});
	}

	Map<PanelType, DiagramCanvas> getCanvasesByPanelType() {
		final Map<PanelType, DiagramCanvas> canvases = new LinkedHashMap<>();
		canvases.put(PanelType.CONCEPTUAL, this.conceptualCanvas);
		canvases.put(PanelType.LOGICAL, this.logicalCanvas);
		canvases.put(PanelType.PHYSICAL, this.physicalCanvas);
		return canvases;
	}

	File getDefaultExportDirectory() {
		if (this.session.getCurrentFile() != null && this.session.getCurrentFile().getParentFile() != null) {
			return this.session.getCurrentFile().getParentFile();
		}

		return new File(System.getProperty("user.home"));
	}

	String getExportSourceFileName() {
		if (this.session.getCurrentFile() != null) {
			return this.session.getCurrentFile().getName();
		}

		final String source = this.document.getSource();
		if (source == null || source.isBlank()) {
			return "Untitled";
		}

		return new File(source).getName();
	}

	void onDocumentChanged() {
		this.session.markChanged();
		this.updateUndoRedoMenuItems();
		this.refreshFrameTitle();
	}

	@Override
	public void openInFrame(final DocumentSession session) {
		SwingUtilities.invokeLater(() -> this.setContent(session));
	}

	boolean prepareForUpdateInstall() throws IOException {
		return this.confirmCloseWithSave("Do you want to save changes before installing the update?");
	}

	void redo() {
		if (!this.session.redo()) {
			return;
		}

		this.refreshAfterHistoryRestore();
	}

	void refreshAfterHistoryRestore() {
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

	@Override
	public void refreshFrameTitle() {
		final String source = this.document.getSource() == null || this.document.getSource().isBlank() ? "Untitled"
				: this.document.getSource();
		this.setTitle(App.title(source + (this.session.isDirty() ? " *" : "")));
	}

	void refreshPinnedStylesPanel() {
		this.sanitizePinnedPaletteNames();
		this.pinnedStylesPanel.removeAll();

		final DiagramCanvas canvas = this.getActiveCanvas();
		final StylePreviewType previewType = canvas == null ? StylePreviewType.NONE : canvas.getStylePreviewType();

		for (final String paletteName : this.appConfig.getPinnedPaletteNames()) {
			final StylePalette palette = this.findPaletteByName(paletteName);
			if (palette != null) {
				this.pinnedStylesPanel.add(this.createPinnedStyleButton(palette, previewType));
			}
		}

		this.pinnedStylesPanel.setVisible(this.pinnedStylesPanel.getComponentCount() > 0);
		this.pinnedStylesPanel.revalidate();
		this.pinnedStylesPanel.repaint();
	}

	void refreshToolbarLabels() {
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

	<T extends EventListener> void removeListener(final T[] listeners, final Consumer<T> remove) {
		for (final T t : listeners) {
			remove.accept(t);
		}
	}

	void reopenWithCurrentDocument() {
		SwingUtilities.invokeLater(() -> {
			MNMain.applyConfiguredLookAndFeel();
			this.setContent(this.session);
		});
	}

	void undo() {
		if (!this.session.undo()) {
			return;
		}

		this.refreshAfterHistoryRestore();
	}

	void updateSelectionLabel(final SelectionInfo selectionInfo) {
		final String path = selectionInfo == null || selectionInfo.path() == null || selectionInfo.path().isBlank() ? "No selection"
				: selectionInfo.path();
		this.selectionPathLabel.setText(path);
		this.refreshPinnedStylesPanel();
	}

	@Override
	public void updateUndoRedoMenuItems() {
		if (this.undoMenuItem != null) {
			this.undoMenuItem.setEnabled(this.session.canUndo());
		}
		if (this.redoMenuItem != null) {
			this.redoMenuItem.setEnabled(this.session.canRedo());
		}
		if (this.toolBar != null) {
			if (this.toolBar.undoButton != null) {
				this.toolBar.undoButton.setEnabled(this.session.canUndo());
			}
			if (this.toolBar.redoButton != null) {
				this.toolBar.redoButton.setEnabled(this.session.canRedo());
			}
		}
	}

	public static boolean confirmModernDocumentVersion(final Component parent, final ModelDocument loadedDocument) {
		return DocumentSessionLoader.confirmModernDocumentVersion(parent, loadedDocument);
	}

	public static boolean confirmModernDocumentVersion(final ModelDocument loadedDocument, final DocumentLoadHandler handler) {
		return DocumentSessionLoader.confirmModernDocumentVersion(loadedDocument, handler);
	}

	public static Optional<DocumentSession> createDocument(final Component parent, final File selectedFile) {
		return DocumentSessionLoader.createDocument(parent, selectedFile);
	}

	public static Optional<DocumentSession> createDocument(final File selectedFile, final DocumentLoadHandler handler) {
		return DocumentSessionLoader.createDocument(selectedFile, handler);
	}

}
