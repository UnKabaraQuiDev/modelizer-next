package lu.kbra.modelizer_next.ui.frame;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.common.OpenedFile;

/**
 * File menu builder for open, save, import, export, and exit actions.
 */
final class FileMenu extends JMenu {

	private static final long serialVersionUID = 1L;
	private JMenu recentItem;
	private final Action recentFileOpenAction;

	/**
	 * Creates a file menu instance.
	 *
	 * @param frame frame that owns the created UI component
	 */
	FileMenu(final MainFrame frame) {
		super("File");
		this.add(this.createMenuItem("New", MainFrameMenuBar.ctrl(KeyEvent.VK_N), frame::newDocument));
		this.add(this.createMenuItem("Load", MainFrameMenuBar.ctrl(KeyEvent.VK_O), frame::loadDocument));
		this.add(this.recentItem = new JMenu("Recent Files"));
		this.recentFileOpenAction = new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JMenuItem source = (JMenuItem) e.getSource();
				final URI uri = (URI) source.getClientProperty("URI");
				final Optional<DocumentSession> session = frame.loadDocument(uri);
				session.ifPresent(d -> d.getDocument().setSource(source.getText()));
			}

		};
		this.updateRecentItem();
		App.addConfigHook(c -> this.updateRecentItem());
		this.add(this.createMenuItem("Save", MainFrameMenuBar.ctrl(KeyEvent.VK_S), frame::saveDocument));
		this.add(this.createMenuItem("Save As...", MainFrameMenuBar.ctrlShift(KeyEvent.VK_S), frame::saveDocumentAs));
		this.addSeparator();
		this.add(this.createExportImageItem(frame));
		this.add(this.createExportCodeItem(frame));
	}

	private JMenuItem createExportImageItem(final MainFrame frame) {
		final JMenu menu = new JMenu("Export image");

		final List<JMenuItem> items = new ArrayList<>();
		for (final ModelExporter service : frame.getModelExporters()) {
			if (service.getExporterType() != ExporterType.IMAGE) {
				continue;
			}
			final JMenuItem item = service.getUiProvider().buildMenuItem();
			item.addActionListener(a -> frame.exportImage(service));
			items.add(item);
		}
		items.sort(Comparator.comparing(JMenuItem::getText));
		items.forEach(menu::add);

		return menu;
	}

	private JMenuItem createExportCodeItem(final MainFrame frame) {
		final JMenu menu = new JMenu("Export code");

		final List<JMenuItem> items = new ArrayList<>();
		for (final ModelExporter service : frame.getModelExporters()) {
			if (service.getExporterType() != ExporterType.CODE) {
				continue;
			}
			final JMenuItem item = service.getUiProvider().buildMenuItem();
			item.addActionListener(a -> frame.exportCode(service));
			items.add(item);
		}
		items.sort(Comparator.comparing(JMenuItem::getText));
		items.forEach(menu::add);

		return menu;
	}

	public void updateRecentItem() {
		this.recentItem.removeAll();
		for (final OpenedFile f : App.CONFIG.getRecentFiles()) {
			final JMenuItem item = new JMenuItem(
					f.source() != null && !f.source().isBlank() ? f.source() : Paths.get(f.file()).toFile().getName());
			item.putClientProperty("URI", f.file());
			item.addActionListener(this.recentFileOpenAction);
			this.recentItem.add(item, 0);
		}
		this.recentItem.addSeparator();
		final JMenuItem clearItem = new JMenuItem("Clear Recent Files");
		clearItem.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				App.editConfig(c -> c.getRecentFiles().clear());
			}

		});
		this.recentItem.add(clearItem);
	}

	/**
	 * Creates a menu item.
	 *
	 * @param text      text to display or edit
	 * @param keyStroke keyboard shortcut to register
	 * @param action    action to register or execute
	 * @return the created menu item
	 */
	private JMenuItem createMenuItem(final String text, final KeyStroke keyStroke, final Runnable action) {
		final JMenuItem item = new JMenuItem(new AbstractAction(text) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
		item.setAccelerator(keyStroke);
		return item;
	}

}
