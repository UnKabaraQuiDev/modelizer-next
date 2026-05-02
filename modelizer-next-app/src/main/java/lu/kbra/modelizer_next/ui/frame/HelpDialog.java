package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.common.SystemThemeDetector;

public class HelpDialog extends JFrame {

	private static final long serialVersionUID = -2242189520928100036L;

	private static final int PAGE_WIDTH = 980;
	private static final int CARD_ARC = 24;
	private static final int CHIP_ARC = 10;
	private static final int SCROLL_UNIT_INCREMENT = 18;
	private static final int KEY_COLUMN_WIDTH = 150;
	private static final int KEY_CHIP_HEIGHT = 26;
	private static final int KEY_CHIP_MIN_WIDTH = 30;
	private static final int HIGHLIGHT_DURATION_MS = 1000;

	private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(760, 560);
	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(980, 720);
	private static final Dimension HEADING_MAXIMUM_SIZE = new Dimension(PAGE_WIDTH, Integer.MAX_VALUE);
	private static final Dimension DESCRIPTION_MAXIMUM_SIZE = new Dimension(760, Integer.MAX_VALUE);
	private static final Dimension CARD_MAXIMUM_SIZE = new Dimension(PAGE_WIDTH, Integer.MAX_VALUE);

	private static final Insets PAGE_CONTENT_INSETS = new Insets(28, 32, 32, 32);
	private static final Insets CARD_INSETS = new Insets(22, 22, 24, 22);
	private static final Insets GROUP_INSETS = new Insets(16, 16, 12, 16);
	private static final Insets ROW_INSETS = new Insets(6, 0, 6, 0);
	private static final Insets TITLE_INSETS = new Insets(0, 0, 8, 0);
	private static final Insets SECTION_INSETS_LEFT = new Insets(0, 0, 16, 16);
	private static final Insets SECTION_INSETS_RIGHT = new Insets(0, 0, 16, 0);
	private static final Insets KEY_COLUMN_INSETS = new Insets(0, 0, 0, 16);
	private static final Insets CHIP_INSETS = new Insets(4, 8, 4, 8);
	private static final Insets PRESSED_CHIP_INSETS = new Insets(5, 8, 3, 8);
	private static final Insets HEADING_TITLE_INSETS = new Insets(8, 0, 0, 0);
	private static final Insets HEADING_DESCRIPTION_INSETS = new Insets(8, 0, 0, 0);

	private static final Color DEFAULT_PAGE_BACKGROUND = new Color(0xF7F8FA);
	private static final Color DEFAULT_TEXT = new Color(0x1F2937);
	private static final Color DEFAULT_MUTED_TEXT = new Color(0x667085);
	private static final Color DEFAULT_ACCENT = new Color(0x3B82F6);
	private static final Color DEFAULT_BORDER = new Color(0xD0D5DD);
	private static final Color DEFAULT_CARD_BACKGROUND = Color.WHITE;
	private static final Color DEFAULT_CHIP_BACKGROUND = new Color(0xF2F4F7);
	private static final Color DEFAULT_PRESSED_CHIP_BACKGROUND = new Color(0xE5E7EB);

	private static final Color PAGE_BACKGROUND = HelpDialog.uiColor("Panel.background", DEFAULT_PAGE_BACKGROUND);
	private static final Color TEXT_COLOR = HelpDialog.uiColor("Label.foreground", DEFAULT_TEXT);
	private static final Color MUTED_TEXT_COLOR = HelpDialog.uiColor("Component.infoForeground",
			HelpDialog.uiColor("textInactiveText", DEFAULT_MUTED_TEXT));
	private static final Color ACCENT_COLOR = HelpDialog.uiColor("Component.accentColor", DEFAULT_ACCENT);
	private static final Color BORDER_COLOR = HelpDialog.uiColor("Component.borderColor", DEFAULT_BORDER);
	private static final Color CARD_BACKGROUND = HelpDialog.uiColor("TextField.background",
			HelpDialog.uiColor("Panel.background", DEFAULT_CARD_BACKGROUND));
	private static final Color GROUP_BACKGROUND = HelpDialog.uiColor("Panel.background", DEFAULT_CARD_BACKGROUND);
	private static final Color CHIP_BACKGROUND = HelpDialog.uiColor("Button.background",
			HelpDialog.uiColor("Panel.background", DEFAULT_CHIP_BACKGROUND));
	private static final Color PRESSED_CHIP_BACKGROUND = HelpDialog.uiColor("Button.pressedBackground", DEFAULT_PRESSED_CHIP_BACKGROUND);
	private static final Color HIGHLIGHT_BACKGROUND = HelpDialog.uiColor("Table.selectionBackground",
			HelpDialog.blend(ACCENT_COLOR, CARD_BACKGROUND, 0.18f));
	private static final Color HIGHLIGHT_BORDER = ACCENT_COLOR;

	private static final Font BASE_FONT = HelpDialog.uiFont("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
	private static final Font EYEBROW_FONT = BASE_FONT.deriveFont(Font.BOLD, 12f);
	private static final Font PULSE_FONT = BASE_FONT.deriveFont(Font.BOLD, 14f);
	private static final Font PARAGRAPH_FONT = BASE_FONT.deriveFont(Font.PLAIN, 14f);
	private static final Font TITLE_FONT = BASE_FONT.deriveFont(Font.BOLD, 18f);
	private static final Font HEADING_FONT = BASE_FONT.deriveFont(Font.BOLD, 28f);
	private static final Font GROUP_TITLE_FONT = BASE_FONT.deriveFont(Font.BOLD, 14f);
	private static final Font DESCRIPTION_FONT = BASE_FONT.deriveFont(Font.PLAIN, 13f);
	private static final Font KEY_CHIP_FONT = BASE_FONT.deriveFont(Font.BOLD, 12f);
	private static final Font GESTURE_CHIP_FONT = BASE_FONT.deriveFont(Font.PLAIN, 12f);

	private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
	private static final Border PAGE_CONTENT_BORDER = new EmptyBorder(PAGE_CONTENT_INSETS);
	private static final Border CARD_BORDER = new EmptyBorder(CARD_INSETS);
	private static final Border GROUP_BORDER = new EmptyBorder(GROUP_INSETS);
	private static final Border ROW_BORDER = new EmptyBorder(ROW_INSETS);
	private static final Border TITLE_BORDER = new EmptyBorder(HEADING_TITLE_INSETS);
	private static final Border DESCRIPTION_BORDER = new EmptyBorder(HEADING_DESCRIPTION_INSETS);
	private static final Border CHIP_BORDER = new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(CHIP_INSETS));
	private static final Border PRESSED_CHIP_BORDER = new CompoundBorder(new LineBorder(ACCENT_COLOR, 1, true),
			new EmptyBorder(PRESSED_CHIP_INSETS));

	private final Map<String, List<KeyChip>> keyChipsByKey = new HashMap<>();
	private final List<ShortcutBinding> shortcutBindings = new ArrayList<>();
	private final Set<String> activeKeys = new HashSet<>();
	private final KeyEventDispatcher keyEventDispatcher = this::dispatchHelpKeyEvent;

	private boolean keyDispatcherRegistered;
	private String activeShortcutKey = "";

	public HelpDialog() {
		super(App.title("Help"));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.add(this.createTabs(), BorderLayout.CENTER);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(final WindowEvent event) {
				HelpDialog.this.registerKeyDispatcher();
			}

			@Override
			public void windowClosed(final WindowEvent event) {
				HelpDialog.this.unregisterKeyDispatcher();
			}
		});

		this.setMinimumSize(MINIMUM_WINDOW_SIZE);
		this.setSize(DEFAULT_WINDOW_SIZE);
		this.setLocationRelativeTo(null);
		super.setResizable(false);
	}

	@Override
	public void dispose() {
		this.unregisterKeyDispatcher();
		super.dispose();
	}

	private JTabbedPane createTabs() {
		final JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Shortcuts", this.createShortcutsPage());
		tabs.putClientProperty(FlatClientProperties.STYLE, "tabType: card");
		return tabs;
	}

	private JScrollPane createShortcutsPage() {
		final JPanel page = new JPanel(new BorderLayout());
		page.setOpaque(true);
		page.setBackground(PAGE_BACKGROUND);

		final JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(PAGE_CONTENT_BORDER);

		content.add(this.createHeading());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createShortcutLayout());

		page.add(content, BorderLayout.NORTH);

		final JScrollPane scroll = new JScrollPane(page);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(EMPTY_BORDER);
		scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
		scroll.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0");

		return scroll;
	}

	private JComponent createHeading() {
		final JPanel heading = new JPanel();
		heading.setOpaque(false);
		heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
		heading.setAlignmentX(Component.LEFT_ALIGNMENT);
		heading.setMaximumSize(HEADING_MAXIMUM_SIZE);

		final JPanel eyebrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		eyebrow.setOpaque(false);
		eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel title = new JLabel("Controls, shortcuts, and gestures");
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setForeground(TEXT_COLOR);
		title.setFont(HEADING_FONT);
		title.setBorder(TITLE_BORDER);

		heading.add(eyebrow);
		heading.add(title);

		return heading;
	}

	private JComponent createShortcutLayout() {
		final CardPanel shortcutsPanel = new CardPanel(new BorderLayout(0, 18));
		shortcutsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		shortcutsPanel.setMaximumSize(CARD_MAXIMUM_SIZE);
		shortcutsPanel.setBorder(CARD_BORDER);

		final JLabel title = new JLabel("Keyboard shortcuts");
		title.setForeground(TEXT_COLOR);
		title.setFont(TITLE_FONT);
		shortcutsPanel.add(title, BorderLayout.NORTH);

		final JPanel sections = new JPanel(new GridBagLayout());
		sections.setOpaque(false);

		int row = 0;
		int col = 0;
		this.addShortcutGroup(sections, this.createFileGroup(), col++, row, 1);
		this.addShortcutGroup(sections, this.createAddGroup(), col++, row, 1);
		col = 0;
		row++;
		this.addShortcutGroup(sections, this.createOtherGroup(), col++, row, 1);
		this.addShortcutGroup(sections, this.createCopyGroup(), col++, row, 1);
		col = 0;
		row++;
		this.addShortcutGroup(sections, this.createEditGroup(), col++, row, 1);
		this.addShortcutGroup(sections, this.createGestureGroup(), col++, row, 1);

		shortcutsPanel.add(sections, BorderLayout.CENTER);

		return shortcutsPanel;
	}

	private void addShortcutGroup(final JPanel parent, final JComponent group, final int x, final int y, final int width) {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = x == 0 ? SECTION_INSETS_LEFT : SECTION_INSETS_RIGHT;
		parent.add(group, constraints);
	}

	private JComponent createFileGroup() {
		return this.createShortcutGroup("File loading, creating, saving",
				List.of(new Shortcut(List.of(List.of("Ctrl", "N")), "New document"),
						new Shortcut(List.of(List.of("Ctrl", "O")), "Load a model file"),
						new Shortcut(List.of(List.of("Ctrl", "S")), "Save"),
						new Shortcut(List.of(List.of("Ctrl", "Shift", "S")), "Save as"),
						new Shortcut(List.of(List.of("Ctrl", "Shift", "E")), "Export as image")));
	}

	private JComponent createAddGroup() {
		return this.createShortcutGroup("Add table, field, comment, link",
				List.of(new Shortcut(List.of(List.of("Ctrl", "T")), "Add table"),
						new Shortcut(List.of(List.of("Ctrl", "F")), "Add field"),
						new Shortcut(List.of(List.of("Shift", "C")), "Add comment"),
						new Shortcut(List.of(List.of("Ctrl", "L")), "Add link")));
	}

	private JComponent createOtherGroup() {
		return this.createShortcutGroup("Others",
				List.of(new Shortcut(List.of(List.of("Ctrl", "A")), "Select all"),
						new Shortcut(List.of(List.of("Esc")), "Clear selection"),
						new Shortcut(List.of(List.of("↑", "↓")), "Move through fields", List.of(List.of("↑"), List.of("↓"))),
						new Shortcut(List.of(List.of("Alt", "↑"), List.of("Alt", "↓")), "Move a selected field in the list")));
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

	private JComponent createGestureGroup() {
		return this.createShortcutGroup("Gestures",
				List.of(new Shortcut(List.of(List.of("Drag right click")),
						"<html><b>Connect elements</b>, creates a link between classes. Attaches a comment to another element.</html>"),
						new Shortcut(List.of(List.of("Drag middle click")), "<html><b>Pan the view</b></html>"),
						new Shortcut(List.of(List.of("Scroll wheel")), "<html><b>Zoom in/out</b></html>"),
						new Shortcut(List.of(List.of("Drag left click")), "<html><b>Move selected elements</b></html>"),
						new Shortcut(List.of(List.of("Double left click")), "<html><b>Edit element</b></html>")));
	}

	private JComponent createShortcutGroup(final String title, final List<Shortcut> rows) {
		final CardPanel group = new CardPanel(new GridBagLayout());
		group.setBackground(GROUP_BACKGROUND);
		group.setBorder(GROUP_BORDER);

		final JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(TEXT_COLOR);
		titleLabel.setFont(GROUP_TITLE_FONT);

		final GridBagConstraints titleConstraints = new GridBagConstraints();
		titleConstraints.gridx = 0;
		titleConstraints.gridy = 0;
		titleConstraints.gridwidth = 2;
		titleConstraints.weightx = 1.0;
		titleConstraints.fill = GridBagConstraints.HORIZONTAL;
		titleConstraints.anchor = GridBagConstraints.NORTHWEST;
		titleConstraints.insets = TITLE_INSETS;
		group.add(titleLabel, titleConstraints);

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
			rowConstraints.insets = new Insets(0, 0, index == rows.size() - 1 ? 0 : 4, 0);
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

	private JComponent createShortcutRow(final Shortcut shortcut) {
		final ShortcutRowPanel row = new ShortcutRowPanel();
		row.setBorder(ROW_BORDER);

		final JComponent keys = this.createKeyGroup(shortcut.keys());
		final JLabel text = HelpDialog.createDescriptionLabel(shortcut.description());

		final GridBagConstraints keyConstraints = new GridBagConstraints();
		keyConstraints.gridx = 0;
		keyConstraints.gridy = 0;
		keyConstraints.weightx = 0.0;
		keyConstraints.anchor = GridBagConstraints.NORTHWEST;
		keyConstraints.fill = GridBagConstraints.NONE;
		keyConstraints.insets = KEY_COLUMN_INSETS;
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

	private JComponent createKeyGroup(final List<List<String>> keyLines) {
		final JPanel group = new JPanel();
		group.setOpaque(false);
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		for (int index = 0; index < keyLines.size(); index++) {
			final JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			line.setOpaque(false);
			line.setAlignmentX(Component.LEFT_ALIGNMENT);

			final List<String> keys = keyLines.get(index);
			if (keys.size() == 1 && HelpDialog.isGestureLabel(keys.get(0))) {
				line.add(HelpDialog.createGestureChip(keys.get(0)));
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

		final Dimension preferredSize = group.getPreferredSize();
		final Dimension size = new Dimension(KEY_COLUMN_WIDTH, preferredSize.height);
		group.setMinimumSize(size);
		group.setPreferredSize(size);

		return group;
	}

	private void registerShortcutBindings(final Shortcut shortcut, final ShortcutRowPanel row) {
		for (final List<String> keyLine : shortcut.bindings()) {
			if (keyLine.stream().anyMatch(HelpDialog::isGestureLabel)) {
				continue;
			}

			final Set<String> keys = new LinkedHashSet<>();
			for (final String key : keyLine) {
				keys.add(HelpDialog.normalizeKeyName(key));
			}

			if (!keys.isEmpty()) {
				this.shortcutBindings.add(new ShortcutBinding(keys, HelpDialog.shortcutKey(keys), row));
			}
		}
	}

	private KeyChip createKeyChip(final String text) {
		final KeyChip label = new KeyChip(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setMinimumSize(new Dimension(KEY_CHIP_MIN_WIDTH, KEY_CHIP_HEIGHT));
		label.setPreferredSize(new Dimension(Math.max(KEY_CHIP_MIN_WIDTH, label.getPreferredSize().width), KEY_CHIP_HEIGHT));

		final String normalizedKey = HelpDialog.normalizeKeyName(text);
		this.keyChipsByKey.computeIfAbsent(normalizedKey, ignored -> new ArrayList<>()).add(label);
		return label;
	}

	private boolean dispatchHelpKeyEvent(final KeyEvent event) {
		if (!this.isShowing() || !this.isActive()) {
			return false;
		}

		final String key = HelpDialog.normalizeKeyEvent(event);
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

	private void updatePressedChips() {
		for (final Map.Entry<String, List<KeyChip>> entry : this.keyChipsByKey.entrySet()) {
			final boolean pressed = this.activeKeys.contains(entry.getKey());
			for (final KeyChip chip : entry.getValue()) {
				chip.setPressed(pressed);
			}
		}
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

	private ShortcutBinding findMatchingShortcut() {
		for (final ShortcutBinding binding : this.shortcutBindings) {
			if (this.activeKeys.equals(binding.keys())) {
				return binding;
			}
		}
		return null;
	}

	private void registerKeyDispatcher() {
		if (this.keyDispatcherRegistered) {
			return;
		}

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.keyEventDispatcher);
		this.keyDispatcherRegistered = true;
	}

	private void unregisterKeyDispatcher() {
		if (!this.keyDispatcherRegistered) {
			return;
		}

		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.keyEventDispatcher);
		this.keyDispatcherRegistered = false;
		this.activeKeys.clear();
		this.updatePressedChips();
	}

	private static boolean isGestureLabel(final String text) {
		return text.indexOf(' ') >= 0 || text.indexOf('/') >= 0;
	}

	private static JLabel createDescriptionLabel(final String description) {
		final String value;
		if (description.startsWith("<html>")) {
			value = description.replace("<html>", "<html><body style='width: 210px'>").replace("</html>", "</body></html>");
		} else {
			value = description;
		}

		final JLabel label = new JLabel(value);
		label.setForeground(TEXT_COLOR);
		label.setFont(DESCRIPTION_FONT);
		label.setVerticalAlignment(SwingConstants.CENTER);
		return label;
	}

	private static JLabel createGestureChip(final String text) {
		final JLabel label = HelpDialog.createChip(text);
		label.setFont(GESTURE_CHIP_FONT);
		return label;
	}

	private static JLabel createChip(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(TEXT_COLOR);
		label.setBackground(CHIP_BACKGROUND);
		label.setOpaque(true);
		label.setBorder(CHIP_BORDER);
		label.putClientProperty(FlatClientProperties.STYLE, "arc: " + CHIP_ARC);
		return label;
	}

	private static JTextArea paragraph(final String text) {
		final JTextArea area = new JTextArea(text);
		area.setEditable(false);
		area.setFocusable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setForeground(MUTED_TEXT_COLOR);
		area.setFont(PARAGRAPH_FONT);
		return area;
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
			yield text == null ? "" : HelpDialog.normalizeKeyName(text.toUpperCase());
		}
		};
	}

	private static String shortcutKey(final Set<String> keys) {
		return String.join("+", keys);
	}

	private static Color uiColor(final String key, final Color fallback) {
		final Color color = UIManager.getColor(key);
		return color == null ? fallback : color;
	}

	private static Font uiFont(final String key, final Font fallback) {
		final Font font = UIManager.getFont(key);
		return font == null ? fallback : font;
	}

	private static Color blend(final Color foreground, final Color background, final float amount) {
		final float inverse = 1.0f - amount;
		final int red = Math.round(foreground.getRed() * amount + background.getRed() * inverse);
		final int green = Math.round(foreground.getGreen() * amount + background.getGreen() * inverse);
		final int blue = Math.round(foreground.getBlue() * amount + background.getBlue() * inverse);
		return new Color(red, green, blue);
	}

	private record Shortcut(List<List<String>> keys, String description, List<List<String>> bindings) {

		Shortcut(final List<List<String>> keys, final String description) {
			this(keys, description, keys);
		}
	}

	private record ShortcutBinding(Set<String> keys, String key, ShortcutRowPanel row) {
	}

	private static class KeyChip extends JLabel {

		private static final long serialVersionUID = -3120281287769826257L;

		private boolean pressed;

		KeyChip(final String text) {
			super(text);
			this.setForeground(TEXT_COLOR);
			this.setBackground(CHIP_BACKGROUND);
			this.setFont(KEY_CHIP_FONT);
			this.setOpaque(true);
			this.setBorder(CHIP_BORDER);
			this.putClientProperty(FlatClientProperties.STYLE, "arc: " + CHIP_ARC);
		}

		void setPressed(final boolean pressed) {
			if (this.pressed == pressed) {
				return;
			}

			this.pressed = pressed;
			this.setBackground(pressed ? PRESSED_CHIP_BACKGROUND : CHIP_BACKGROUND);
			this.setBorder(pressed ? PRESSED_CHIP_BORDER : CHIP_BORDER);
			this.repaint();
		}
	}

	private static class ShortcutRowPanel extends JPanel {

		private static final long serialVersionUID = -4690946944232659852L;

		private Timer highlightTimer;
		private boolean highlighted;

		ShortcutRowPanel() {
			super(new GridBagLayout());
			this.setOpaque(false);
		}

		void scrollToVisible() {
			this.scrollRectToVisible(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
		}

		void highlightTemporarily() {
			if (this.highlightTimer != null) {
				this.highlightTimer.stop();
			}

			this.highlighted = true;
			this.repaint();

			this.highlightTimer = new Timer(HIGHLIGHT_DURATION_MS, event -> {
				this.highlighted = false;
				this.repaint();
			});
			this.highlightTimer.setRepeats(false);
			this.highlightTimer.start();
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			if (this.highlighted) {
				final Graphics2D g2 = (Graphics2D) graphics.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				try {
					g2.setColor(HIGHLIGHT_BACKGROUND);
					g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, CHIP_ARC, CHIP_ARC);
					g2.setColor(HIGHLIGHT_BORDER);
					g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, CHIP_ARC, CHIP_ARC);
				} finally {
					g2.dispose();
				}
			}

			super.paintComponent(graphics);
		}
	}

	private static class CardPanel extends JPanel {

		private static final long serialVersionUID = 5726550619135877533L;

		CardPanel(final java.awt.LayoutManager layout) {
			super(layout);
			this.setOpaque(false);
			this.setBackground(CARD_BACKGROUND);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			final Graphics2D g2 = (Graphics2D) graphics.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			try {
				g2.setColor(this.getBackground());
				g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, CARD_ARC, CARD_ARC);
				g2.setColor(BORDER_COLOR);
				g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, CARD_ARC, CARD_ARC);
			} finally {
				g2.dispose();
			}

			super.paintComponent(graphics);
		}
	}

	public static void main(final String[] args) {
		if (SystemThemeDetector.isDark()) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}

		final HelpDialog hd = new HelpDialog();
		hd.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		hd.setVisible(true);
	}

}
