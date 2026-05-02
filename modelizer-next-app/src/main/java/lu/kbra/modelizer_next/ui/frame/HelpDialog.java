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
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatClientProperties;

import lu.kbra.modelizer_next.App;

public class HelpDialog extends JFrame {

	private static final long serialVersionUID = -2242189520928100036L;

	private static final int PAGE_WIDTH = 980;
	private static final int CARD_ARC = 24;
	private static final int CHIP_ARC = 10;

	private static final Font PARAGRAPH_FONT = UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 14f);
	private static final Font TITLE_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f);
	private static final Font HEADING_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 28f);

	public HelpDialog() {
		super(App.title("Help"));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.add(this.createShortcutsPage(), BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(760, 560));
		this.setSize(980, 720);
		this.setLocationRelativeTo(null);
	}

	private JScrollPane createShortcutsPage() {
		final JPanel page = new JPanel(new BorderLayout());
		page.setOpaque(true);
		page.setBackground(HelpDialog.uiColor("Panel.background", new Color(0xF7F8FA)));

		final JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(new EmptyBorder(28, 32, 32, 32));

		content.add(this.createHeading());
		content.add(Box.createVerticalStrut(18));
		content.add(this.createShortcutLayout());

		page.add(content, BorderLayout.NORTH);

		final JScrollPane scroll = new JScrollPane(page);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(18);
		scroll.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0");

		return scroll;
	}

	private JComponent createHeading() {
		final JPanel heading = new JPanel();
		heading.setOpaque(false);
		heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
		heading.setAlignmentX(Component.LEFT_ALIGNMENT);
		heading.setMaximumSize(new Dimension(HelpDialog.PAGE_WIDTH, Integer.MAX_VALUE));

		final JPanel eyebrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		eyebrow.setOpaque(false);
		eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel eyebrowText = new JLabel("Controls, shortcuts and gestures");
		eyebrowText.setFont(HEADING_FONT);
		eyebrow.add(eyebrowText);

		heading.add(eyebrow);

		return heading;
	}

	private JComponent createShortcutLayout() {
		final CardPanel shortcutsPanel = new CardPanel(new BorderLayout(0, 18));
		shortcutsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		shortcutsPanel.setMaximumSize(new Dimension(HelpDialog.PAGE_WIDTH, Integer.MAX_VALUE));
		shortcutsPanel.setBorder(new EmptyBorder(22, 22, 24, 22));

		final JLabel title = new JLabel("Keyboard shortcuts");
		title.setForeground(HelpDialog.textColor());
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
		constraints.insets = new Insets(0, 0, 16, x == 0 ? 16 : 0);
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
						new Shortcut(List.of(List.of("↑", "↓")), "Move through fields"),
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
				List.of(new Shortcut(List.of(List.of("Ctrl", "E"), List.of("Double left click")), "Edit selected item"),
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
		group.setBackground(HelpDialog.groupBackgroundColor());
		group.setBorder(new EmptyBorder(16, 16, 12, 16));

		final JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(HelpDialog.textColor());
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

		final GridBagConstraints titleConstraints = new GridBagConstraints();
		titleConstraints.gridx = 0;
		titleConstraints.gridy = 0;
		titleConstraints.gridwidth = 2;
		titleConstraints.weightx = 1.0;
		titleConstraints.fill = GridBagConstraints.HORIZONTAL;
		titleConstraints.anchor = GridBagConstraints.NORTHWEST;
		titleConstraints.insets = new Insets(0, 0, 8, 0);
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
		final JPanel row = new JPanel(new GridBagLayout());
		row.setOpaque(false);
		row.setBorder(new EmptyBorder(6, 0, 6, 0));

		final JComponent keys = this.createKeyGroup(shortcut.keys());
		final JLabel text = HelpDialog.createDescriptionLabel(shortcut.description());

		final GridBagConstraints keyConstraints = new GridBagConstraints();
		keyConstraints.gridx = 0;
		keyConstraints.gridy = 0;
		keyConstraints.weightx = 0.0;
		keyConstraints.anchor = GridBagConstraints.NORTHWEST;
		keyConstraints.fill = GridBagConstraints.NONE;
		keyConstraints.insets = new Insets(0, 0, 0, 16);
		row.add(keys, keyConstraints);

		final GridBagConstraints textConstraints = new GridBagConstraints();
		textConstraints.gridx = 1;
		textConstraints.gridy = 0;
		textConstraints.weightx = 1.0;
		textConstraints.anchor = GridBagConstraints.WEST;
		textConstraints.fill = GridBagConstraints.HORIZONTAL;
		row.add(text, textConstraints);

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
					line.add(HelpDialog.createKeyChip(key));
				}
			}

			group.add(line);
			if (index < keyLines.size() - 1) {
				group.add(Box.createVerticalStrut(4));
			}
		}

		final Dimension preferredSize = group.getPreferredSize();
		group.setMinimumSize(new Dimension(150, preferredSize.height));
		group.setPreferredSize(new Dimension(150, preferredSize.height));

		return group;
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
		label.setForeground(HelpDialog.textColor());
		label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
		label.setVerticalAlignment(SwingConstants.CENTER);
		return label;
	}

	private static JLabel createKeyChip(final String text) {
		final JLabel label = HelpDialog.createChip(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setMinimumSize(new Dimension(30, 26));
		label.setPreferredSize(new Dimension(Math.max(30, label.getPreferredSize().width), 26));
		return label;
	}

	private static JLabel createGestureChip(final String text) {
		final JLabel label = HelpDialog.createChip(text);
		label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
		return label;
	}

	private static JLabel createChip(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(HelpDialog.textColor());
		label.setBackground(HelpDialog.chipBackgroundColor());
		label.setOpaque(true);
		label.setBorder(new CompoundBorder(new LineBorder(HelpDialog.borderColor(), 1, true), new EmptyBorder(4, 8, 4, 8)));
		label.putClientProperty(FlatClientProperties.STYLE, "arc: " + HelpDialog.CHIP_ARC);
		return label;
	}

	private static JTextArea paragraph(final String text) {
		final JTextArea area = new JTextArea(text);
		area.setEditable(false);
		area.setFocusable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setForeground(HelpDialog.mutedTextColor());
		area.setFont(PARAGRAPH_FONT);
		return area;
	}

	private static Color textColor() {
		return HelpDialog.uiColor("Label.foreground", new Color(0x1F2937));
	}

	private static Color mutedTextColor() {
		return HelpDialog.uiColor("Component.infoForeground", HelpDialog.uiColor("textInactiveText", new Color(0x667085)));
	}

	private static Color accentColor() {
		return HelpDialog.uiColor("Component.accentColor", new Color(0x3B82F6));
	}

	private static Color borderColor() {
		return HelpDialog.uiColor("Component.borderColor", new Color(0xD0D5DD));
	}

	private static Color cardBackgroundColor() {
		return HelpDialog.uiColor("TextField.background", HelpDialog.uiColor("Panel.background", Color.WHITE));
	}

	private static Color groupBackgroundColor() {
		return HelpDialog.uiColor("Panel.background", new Color(0xFFFFFF));
	}

	private static Color chipBackgroundColor() {
		return HelpDialog.uiColor("Button.background", HelpDialog.uiColor("Panel.background", new Color(0xF2F4F7)));
	}

	private static Color uiColor(final String key, final Color fallback) {
		final Color color = UIManager.getColor(key);
		return color == null ? fallback : color;
	}

	private record Shortcut(List<List<String>> keys, String description) {
	}

	private static class CardPanel extends JPanel {

		private static final long serialVersionUID = 5726550619135877533L;

		CardPanel(final java.awt.LayoutManager layout) {
			super(layout);
			this.setOpaque(false);
			this.setBackground(HelpDialog.cardBackgroundColor());
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			final Graphics2D g2 = (Graphics2D) graphics.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			try {
				g2.setColor(this.getBackground());
				g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpDialog.CARD_ARC, HelpDialog.CARD_ARC);
				g2.setColor(HelpDialog.borderColor());
				g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpDialog.CARD_ARC, HelpDialog.CARD_ARC);
			} finally {
				g2.dispose();
			}

			super.paintComponent(graphics);
		}
	}

	public static void main(final String[] args) {
		final HelpDialog hd = new HelpDialog();
		hd.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		hd.setVisible(true);
	}

}
