package lu.kbra.modelizer_next.ui.help;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.formdev.flatlaf.FlatClientProperties;

public class ShortcutsTab extends JPanel {

	private static class KeyChip extends JLabel {

		private static final long serialVersionUID = -3120281287769826257L;

		private boolean pressed;

		KeyChip(final String text) {
			super(text);
			this.setForeground(HelpUi.TEXT_COLOR);
			this.setBackground(HelpUi.CHIP_BACKGROUND);
			this.setFont(HelpUi.KEY_CHIP_FONT);
			this.setOpaque(true);
			this.setBorder(HelpUi.CHIP_BORDER);
			this.putClientProperty(FlatClientProperties.STYLE, "arc: " + HelpUi.CHIP_ARC);
		}

		void setPressed(final boolean pressed) {
			if (this.pressed == pressed) {
				return;
			}

			this.pressed = pressed;
			this.setBackground(pressed ? HelpUi.PRESSED_CHIP_BACKGROUND : HelpUi.CHIP_BACKGROUND);
			this.setBorder(pressed ? HelpUi.PRESSED_CHIP_BORDER : HelpUi.CHIP_BORDER);
			this.repaint();
		}
	}

	private static class ResponsiveGridPanel extends JPanel {

		private static final long serialVersionUID = -4676952948188047231L;

		private int lastColumnCount = -1;

		ResponsiveGridPanel() {
			super(new GridBagLayout());
			this.setOpaque(false);
			this.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		}

		@Override
		public Component add(final Component component) {
			final Component added = super.add(component);
			this.lastColumnCount = -1;
			return added;
		}

		@Override
		public void doLayout() {
			this.updateGridConstraints(this.currentColumnCount());
			super.doLayout();
		}

		@Override
		public Dimension getPreferredSize() {
			this.updateGridConstraints(this.currentColumnCount());
			return super.getPreferredSize();
		}

		private int currentColumnCount() {
			final int width = this.getWidth() > 0 ? this.getWidth() : this.getParentWidth();
			return width > 0 && width < HelpUi.RESPONSIVE_GRID_BREAKPOINT ? 1 : 2;
		}

		private int getParentWidth() {
			return this.getParent() == null ? 0 : this.getParent().getWidth();
		}

		private java.awt.Insets insetsFor(final int column, final int columnCount) {
			if (columnCount == 1) {
				return HelpUi.SECTION_INSETS_SINGLE_COLUMN;
			}

			return column == 0 ? HelpUi.SECTION_INSETS_LEFT : HelpUi.SECTION_INSETS_RIGHT;
		}

		private void updateGridConstraints(final int columnCount) {
			if (this.lastColumnCount == columnCount) {
				return;
			}

			final GridBagLayout layout = (GridBagLayout) this.getLayout();

			for (int index = 0; index < this.getComponentCount(); index++) {
				final Component component = this.getComponent(index);
				final int column = index % columnCount;
				final int row = index / columnCount;

				final GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = column;
				constraints.gridy = row;
				constraints.weightx = 1.0;
				constraints.weighty = 1.0;
				constraints.fill = GridBagConstraints.BOTH;
				constraints.insets = this.insetsFor(column, columnCount);

				layout.setConstraints(component, constraints);
			}

			this.lastColumnCount = columnCount;
		}
	}

	private record Shortcut(List<List<String>> keys, String description, List<List<String>> bindings) {

		Shortcut(final List<List<String>> keys, final String description) {
			this(keys, description, keys);
		}
	}

	private record ShortcutBinding(Set<String> keys, String key, ShortcutRowPanel row) {
	}

	private static class ShortcutRowPanel extends JPanel {

		private static final long serialVersionUID = -4690946944232659852L;

		private Timer highlightTimer;
		private boolean highlighted;

		ShortcutRowPanel() {
			super(new GridBagLayout());
			this.setOpaque(false);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			if (this.highlighted) {
				final Graphics2D g2 = (Graphics2D) graphics.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				try {
					g2.setColor(HelpUi.HIGHLIGHT_BACKGROUND);
					g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpUi.CHIP_ARC, HelpUi.CHIP_ARC);
					g2.setColor(HelpUi.HIGHLIGHT_BORDER);
					g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpUi.CHIP_ARC, HelpUi.CHIP_ARC);
				} finally {
					g2.dispose();
				}
			}

			super.paintComponent(graphics);
		}

		void highlightTemporarily() {
			if (this.highlightTimer != null) {
				this.highlightTimer.stop();
			}

			this.highlighted = true;
			this.repaint();

			this.highlightTimer = new Timer(HelpUi.HIGHLIGHT_DURATION_MS, event -> {
				this.highlighted = false;
				this.repaint();
			});
			this.highlightTimer.setRepeats(false);
			this.highlightTimer.start();
		}

		void scrollToVisible() {
			this.scrollRectToVisible(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
		}
	}

	private static final long serialVersionUID = -3605281027804194279L;

	private static boolean isGestureLabel(final String text) {
		return text.indexOf(' ') >= 0 || text.indexOf('/') >= 0;
	}

	private static String normalizeKeyEvent(final KeyEvent event) {
		return switch (event.getKeyCode()) {
		case KeyEvent.VK_CONTROL -> "Ctrl";
		case KeyEvent.VK_SHIFT -> "Shift";
		case KeyEvent.VK_ALT -> "Alt";
		case KeyEvent.VK_ESCAPE -> "Esc";
		case KeyEvent.VK_DELETE -> "Delete";
		case KeyEvent.VK_UP -> "↑";
		case KeyEvent.VK_DOWN -> "↓";
		case KeyEvent.VK_F2 -> "F2";
		default -> {
			final String text = KeyEvent.getKeyText(event.getKeyCode());
			yield text == null ? "" : ShortcutsTab.normalizeKeyName(text.toUpperCase());
		}
		};
	}

	private static String normalizeKeyName(final String key) {
		return switch (key) {
		case "Control", "CTRL", "ctrl" -> "Ctrl";
		case "SHIFT", "shift" -> "Shift";
		case "ALT", "alt" -> "Alt";
		case "Escape", "ESC", "esc" -> "Esc";
		case "Up" -> "↑";
		case "Down" -> "↓";
		default -> key;
		};
	}

	private static String shortcutKey(final Set<String> keys) {
		return String.join("+", keys);
	}

	private final Map<String, List<KeyChip>> keyChipsByKey = new HashMap<>();

	private final List<ShortcutBinding> shortcutBindings = new ArrayList<>();

	private final Set<String> activeKeys = new HashSet<>();

	private boolean keyDispatcherRegistered;

	private String activeShortcutKey = "";

	private final KeyEventDispatcher keyEventDispatcher = this::dispatchHelpKeyEvent;

	public ShortcutsTab() {
		super(new java.awt.BorderLayout());
		this.setOpaque(false);
		this.add(this.createShortcutsPage(), java.awt.BorderLayout.CENTER);
	}

	private void addShortcutGroup(final JPanel parent, final JComponent group, final int x, final int y) {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = x == 0 ? HelpUi.SECTION_INSETS_LEFT : HelpUi.SECTION_INSETS_RIGHT;
		parent.add(group, constraints);
	}

	private JComponent createAddGroup() {
		return this.createShortcutGroup("Add table, field, comment, link",
				List.of(new Shortcut(List.of(List.of("Ctrl", "T")), "Add table"),
						new Shortcut(List.of(List.of("Ctrl", "F")), "Add field"),
						new Shortcut(List.of(List.of("Shift", "C")), "Add comment"),
						new Shortcut(List.of(List.of("Ctrl", "L")), "Add link")));
	}

	private JComponent createCopyGroup() {
		return this.createShortcutGroup("Copy, paste, undo",
				List.of(new Shortcut(List.of(List.of("Ctrl", "Z")), "Undo"),
						new Shortcut(List.of(List.of("Ctrl", "Shift", "Z")), "Redo"),
						new Shortcut(List.of(List.of("Ctrl", "C")), "Copy selection"),
						new Shortcut(List.of(List.of("Ctrl", "X")), "Cut selection"),
						new Shortcut(List.of(List.of("Ctrl", "V")), "Paste selection")));
	}

	private JComponent createEditGroup() {
		return this.createShortcutGroup("Edit, rename, delete",
				List.of(new Shortcut(List.of(List.of("Ctrl", "E"), List.of("Double left click")),
						"Edit selected item",
						List.of(List.of("Ctrl", "E"))),
						new Shortcut(List.of(List.of("F2")), "Rename selected item"),
						new Shortcut(List.of(List.of("Delete"), List.of("Shift", "X")), "Delete selected item"),
						new Shortcut(List.of(List.of("Ctrl", "D")), "Duplicate selected elements")));
	}

	private JComponent createFileGroup() {
		return this.createShortcutGroup("File loading, creating, saving",
				List.of(new Shortcut(List.of(List.of("Ctrl", "N")), "New document"),
						new Shortcut(List.of(List.of("Ctrl", "O")), "Load a model file"),
						new Shortcut(List.of(List.of("Ctrl", "S")), "Save"),
						new Shortcut(List.of(List.of("Ctrl", "Shift", "S")), "Save as"),
						new Shortcut(List.of(List.of("Ctrl", "Shift", "E")), "Export as image")));
	}

	private JComponent createGestureGroup() {
		return this.createShortcutGroup("Gestures",
				List.of(new Shortcut(List.of(List.of("Drag right click")),
						"<html><b>Connect elements</b>, creates a link between classes. Attaches a comment to another element.</html>"),
						new Shortcut(List.of(List.of("Drag middle click")), "<html><b>Pan the view</b></html>"),
						new Shortcut(List.of(List.of("Scroll wheel")), "<html><b>Zoom in/out</b></html>"),
						new Shortcut(List.of(List.of("Drag left click")), "<html><b>Move selected elements</b></html>"),
						new Shortcut(List.of(List.of("Double left click")), "<html><b>Edit element</b></html>")));
	}

	private KeyChip createKeyChip(final String text) {
		final KeyChip label = new KeyChip(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setMinimumSize(new java.awt.Dimension(HelpUi.KEY_CHIP_MIN_WIDTH, HelpUi.KEY_CHIP_HEIGHT));
		label.setPreferredSize(
				new java.awt.Dimension(Math.max(HelpUi.KEY_CHIP_MIN_WIDTH, label.getPreferredSize().width), HelpUi.KEY_CHIP_HEIGHT));

		final String normalizedKey = ShortcutsTab.normalizeKeyName(text);
		this.keyChipsByKey.computeIfAbsent(normalizedKey, ignored -> new ArrayList<>()).add(label);
		return label;
	}

	private JComponent createKeyGroup(final List<List<String>> keyLines) {
		final JPanel group = new JPanel();
		group.setOpaque(false);
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

		for (int index = 0; index < keyLines.size(); index++) {
			final JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			line.setOpaque(false);
			line.setAlignmentX(Component.LEFT_ALIGNMENT);

			final List<String> keys = keyLines.get(index);
			if (keys.size() == 1 && ShortcutsTab.isGestureLabel(keys.get(0))) {
				line.add(HelpUi.gestureChip(keys.get(0)));
			} else {
				for (final String key : keys) {
					line.add(this.createKeyChip(key));
				}
			}

			group.add(line);
			if (index < keyLines.size() - 1) {
				group.add(Box.createVerticalStrut(4));
			}
		}

		final java.awt.Dimension preferredSize = group.getPreferredSize();
		final java.awt.Dimension size = new java.awt.Dimension(HelpUi.KEY_COLUMN_WIDTH, preferredSize.height);
		group.setMinimumSize(size);
		group.setPreferredSize(size);

		return group;
	}

	private JComponent createOtherGroup() {
		return this.createShortcutGroup("Others",
				List.of(new Shortcut(List.of(List.of("Ctrl", "A")), "Select all"),
						new Shortcut(List.of(List.of("Esc")), "Clear selection"),
						new Shortcut(List.of(List.of("↑", "↓")), "Move through fields", List.of(List.of("↑"), List.of("↓"))),
						new Shortcut(List.of(List.of("Alt", "↑"), List.of("Alt", "↓")), "Move a selected field in the list")));
	}

	private JComponent createShortcutGroup(final String title, final List<Shortcut> rows) {
		final HelpUi.CardPanel group = new HelpUi.CardPanel(new GridBagLayout());
		group.setBackground(HelpUi.GROUP_BACKGROUND);
		group.setBorder(HelpUi.GROUP_BORDER);

		final GridBagConstraints titleConstraints = new GridBagConstraints();
		titleConstraints.gridx = 0;
		titleConstraints.gridy = 0;
		titleConstraints.gridwidth = 2;
		titleConstraints.weightx = 1.0;
		titleConstraints.fill = GridBagConstraints.HORIZONTAL;
		titleConstraints.anchor = GridBagConstraints.NORTHWEST;
		titleConstraints.insets = HelpUi.TITLE_INSETS;
		group.add(HelpUi.groupTitle(title), titleConstraints);

		for (int index = 0; index < rows.size(); index++) {
			final Shortcut shortcut = rows.get(index);
			final JComponent row = this.createShortcutRow(shortcut);

			final GridBagConstraints rowConstraints = new GridBagConstraints();
			rowConstraints.gridx = 0;
			rowConstraints.gridy = index + 1;
			rowConstraints.gridwidth = 2;
			rowConstraints.weightx = 1.0;
			rowConstraints.fill = GridBagConstraints.HORIZONTAL;
			rowConstraints.anchor = GridBagConstraints.NORTHWEST;
			rowConstraints.insets = new java.awt.Insets(0, 0, index == rows.size() - 1 ? 0 : 4, 0);
			group.add(row, rowConstraints);
		}

		final GridBagConstraints fillerConstraints = new GridBagConstraints();
		fillerConstraints.gridx = 0;
		fillerConstraints.gridy = rows.size() + 1;
		fillerConstraints.weighty = 1.0;
		fillerConstraints.fill = GridBagConstraints.VERTICAL;
		group.add(Box.createVerticalGlue(), fillerConstraints);

		return group;
	}

	private JComponent createShortcutLayout() {
		final ResponsiveGridPanel sections = new ResponsiveGridPanel();
		sections.add(this.createFileGroup());
		sections.add(this.createAddGroup());
		sections.add(this.createOtherGroup());
		sections.add(this.createCopyGroup());
		sections.add(this.createEditGroup());
		return sections;
	}

	private JComponent createShortcutRow(final Shortcut shortcut) {
		final ShortcutRowPanel row = new ShortcutRowPanel();
		row.setBorder(HelpUi.ROW_BORDER);

		final JComponent keys = this.createKeyGroup(shortcut.keys());
		final JComponent text = HelpUi.descriptionLabel(shortcut.description());

		final GridBagConstraints keyConstraints = new GridBagConstraints();
		keyConstraints.gridx = 0;
		keyConstraints.gridy = 0;
		keyConstraints.weightx = 0.0;
		keyConstraints.anchor = GridBagConstraints.NORTHWEST;
		keyConstraints.fill = GridBagConstraints.NONE;
		keyConstraints.insets = HelpUi.KEY_COLUMN_INSETS;
		row.add(keys, keyConstraints);

		final GridBagConstraints textConstraints = new GridBagConstraints();
		textConstraints.gridx = 1;
		textConstraints.gridy = 0;
		textConstraints.weightx = 1.0;
		textConstraints.anchor = GridBagConstraints.WEST;
		textConstraints.fill = GridBagConstraints.HORIZONTAL;
		row.add(text, textConstraints);

		this.registerShortcutBindings(shortcut, row);
		return row;
	}

	private JScrollPane createShortcutsPage() {
		final JPanel content = HelpUi.createPageContent();

		content.add(HelpUi.createHeading("Controls, shortcuts, and gestures"));
		content.add(Box.createVerticalStrut(18));
		content.add(this.createShortcutLayout());

		return HelpUi.createScrollPane(content);
	}

	private boolean dispatchHelpKeyEvent(final KeyEvent event) {
		final Window window = SwingUtilities.getWindowAncestor(this);
		if (!this.isShowing() || window == null || !window.isActive()) {
			return false;
		}

		final String key = ShortcutsTab.normalizeKeyEvent(event);
		if (key.isBlank()) {
			return false;
		}

		if (event.getID() == KeyEvent.KEY_PRESSED) {
			this.activeKeys.add(key);
			this.updatePressedChips();
			this.highlightMatchingShortcut();
		} else if (event.getID() == KeyEvent.KEY_RELEASED) {
			this.activeKeys.remove(key);
			this.updatePressedChips();

			if (this.findMatchingShortcut() == null) {
				this.activeShortcutKey = "";
			}
		}

		return false;
	}

	private ShortcutBinding findMatchingShortcut() {
		for (final ShortcutBinding binding : this.shortcutBindings) {
			if (this.activeKeys.equals(binding.keys())) {
				return binding;
			}
		}

		return null;
	}

	private void highlightMatchingShortcut() {
		final ShortcutBinding match = this.findMatchingShortcut();
		if (match == null || match.key().equals(this.activeShortcutKey)) {
			return;
		}

		this.activeShortcutKey = match.key();
		match.row().scrollToVisible();
		match.row().highlightTemporarily();
	}

	private void registerShortcutBindings(final Shortcut shortcut, final ShortcutRowPanel row) {
		for (final List<String> keyLine : shortcut.bindings()) {
			if (keyLine.stream().anyMatch(ShortcutsTab::isGestureLabel)) {
				continue;
			}

			final Set<String> keys = new LinkedHashSet<>();
			for (final String key : keyLine) {
				keys.add(ShortcutsTab.normalizeKeyName(key));
			}

			if (!keys.isEmpty()) {
				this.shortcutBindings.add(new ShortcutBinding(keys, ShortcutsTab.shortcutKey(keys), row));
			}
		}
	}

	private void updatePressedChips() {
		for (final Map.Entry<String, List<KeyChip>> entry : this.keyChipsByKey.entrySet()) {
			final boolean pressed = this.activeKeys.contains(entry.getKey());

			for (final KeyChip chip : entry.getValue()) {
				chip.setPressed(pressed);
			}
		}
	}

	void registerKeyDispatcher() {
		if (this.keyDispatcherRegistered) {
			return;
		}

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.keyEventDispatcher);
		this.keyDispatcherRegistered = true;
	}

	void unregisterKeyDispatcher() {
		if (!this.keyDispatcherRegistered) {
			return;
		}

		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.keyEventDispatcher);
		this.keyDispatcherRegistered = false;
		this.activeKeys.clear();
		this.updatePressedChips();
	}

}
