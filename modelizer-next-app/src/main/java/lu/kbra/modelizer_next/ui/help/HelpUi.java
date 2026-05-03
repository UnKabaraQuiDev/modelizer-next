package lu.kbra.modelizer_next.ui.help;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatClientProperties;

final class HelpUi {

	private static final class ScrollablePage extends JPanel implements Scrollable {

		private static final long serialVersionUID = -764577319228338929L;

		ScrollablePage() {
			super(new java.awt.BorderLayout());
			this.setOpaque(true);
			this.setBackground(HelpUi.PAGE_BACKGROUND);
			this.addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(final ComponentEvent event) {
					SwingUtilities.invokeLater(() -> {
						ScrollablePage.this.revalidate();
						ScrollablePage.this.repaint();
					});
				}
			});
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return this.getPreferredSize();
		}

		@Override
		public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
			return Math.max(HelpUi.SCROLL_UNIT_INCREMENT, visibleRect.height - HelpUi.SCROLL_UNIT_INCREMENT);
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
			return HelpUi.SCROLL_UNIT_INCREMENT;
		}
	}

	private static final class WrappingTextArea extends JTextArea {

		private static final long serialVersionUID = -282941749559587952L;

		WrappingTextArea(final String text) {
			super(text);
		}

		@Override
		public Dimension getPreferredSize() {
			final Container parent = this.getParent();
			if (this.getLineWrap() && parent != null && parent.getWidth() > 0) {
				final int width = Math.max(1, parent.getWidth());
				this.setSize(width, Short.MAX_VALUE);
			}

			return super.getPreferredSize();
		}
	}

	static final class CardPanel extends JPanel {

		private static final long serialVersionUID = 5726550619135877533L;

		CardPanel(final java.awt.LayoutManager layout) {
			super(layout);
			this.setOpaque(false);
			this.setBackground(HelpUi.CARD_BACKGROUND);
			this.setAlignmentX(Component.LEFT_ALIGNMENT);
		}

		@Override
		public Dimension getMaximumSize() {
			return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			final Graphics2D g2 = (Graphics2D) graphics.create();
			g2.setComposite(AlphaComposite.SrcOver);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			try {
				g2.setColor(this.getBackground());
				g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpUi.CARD_ARC, HelpUi.CARD_ARC);
				g2.setColor(HelpUi.BORDER_COLOR);
				g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, HelpUi.CARD_ARC, HelpUi.CARD_ARC);
			} finally {
				g2.dispose();
			}

			super.paintComponent(graphics);
		}
	}

	static final int PAGE_WIDTH = 980;
	static final int CARD_ARC = 24;
	static final int CHIP_ARC = 10;
	static final int SCROLL_UNIT_INCREMENT = 18;
	static final int KEY_COLUMN_WIDTH = 150;

	static final int KEY_CHIP_HEIGHT = 26;
	static final int KEY_CHIP_MIN_WIDTH = 30;
	static final int HIGHLIGHT_DURATION_MS = 1000;

	static final Dimension FULL_WIDTH_MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	static final int RESPONSIVE_GRID_BREAKPOINT = 760;
	static final Insets SECTION_INSETS_SINGLE_COLUMN = new Insets(0, 0, 16, 0);
	static final Insets PAGE_CONTENT_INSETS = new Insets(28, 32, 32, 32);
	static final Insets CARD_INSETS = new Insets(22, 22, 24, 22);
	static final Insets GROUP_INSETS = new Insets(16, 16, 12, 16);
	static final Insets ROW_INSETS = new Insets(6, 0, 6, 0);
	static final Insets TITLE_INSETS = new Insets(0, 0, 8, 0);
	static final Insets SECTION_INSETS_LEFT = new Insets(0, 0, 16, 16);
	static final Insets SECTION_INSETS_RIGHT = new Insets(0, 0, 16, 0);
	static final Insets KEY_COLUMN_INSETS = new Insets(0, 0, 0, 16);
	static final Insets CHIP_INSETS = new Insets(4, 8, 4, 8);
	static final Insets PRESSED_CHIP_INSETS = new Insets(5, 8, 3, 8);
	static final Insets HEADING_TITLE_INSETS = new Insets(8, 0, 0, 0);

	static final Insets HEADING_DESCRIPTION_INSETS = new Insets(8, 0, 0, 0);
	static final Insets INFO_ROW_INSETS = new Insets(4, 0, 4, 0);
	static final Insets INFO_LABEL_INSETS = new Insets(0, 0, 0, 16);
	private static final Color DEFAULT_PAGE_BACKGROUND = new Color(0xF7F8FA);
	private static final Color DEFAULT_TEXT = new Color(0x1F2937);
	private static final Color DEFAULT_MUTED_TEXT = new Color(0x667085);
	private static final Color DEFAULT_ACCENT = new Color(0x3B82F6);
	private static final Color DEFAULT_BORDER = new Color(0xD0D5DD);

	private static final Color DEFAULT_CARD_BACKGROUND = Color.WHITE;
	private static final Color DEFAULT_CHIP_BACKGROUND = new Color(0xF2F4F7);
	private static final Color DEFAULT_PRESSED_CHIP_BACKGROUND = new Color(0xE5E7EB);
	static final Color PAGE_BACKGROUND = HelpUi.uiColor("Panel.background", HelpUi.DEFAULT_PAGE_BACKGROUND);
	static final Color TEXT_COLOR = HelpUi.uiColor("Label.foreground", HelpUi.DEFAULT_TEXT);
	static final Color MUTED_TEXT_COLOR = HelpUi.uiColor("Component.infoForeground",
			HelpUi.uiColor("textInactiveText", HelpUi.DEFAULT_MUTED_TEXT));
	static final Color ACCENT_COLOR = HelpUi.uiColor("Component.accentColor", HelpUi.DEFAULT_ACCENT);
	static final Color BORDER_COLOR = HelpUi.uiColor("Component.borderColor", HelpUi.DEFAULT_BORDER);
	static final Color CARD_BACKGROUND = HelpUi.uiColor("TextField.background",
			HelpUi.uiColor("Panel.background", HelpUi.DEFAULT_CARD_BACKGROUND));
	static final Color GROUP_BACKGROUND = HelpUi.uiColor("Panel.background", HelpUi.DEFAULT_CARD_BACKGROUND);
	static final Color CHIP_BACKGROUND = HelpUi.uiColor("Button.background",
			HelpUi.uiColor("Panel.background", HelpUi.DEFAULT_CHIP_BACKGROUND));

	static final Color PRESSED_CHIP_BACKGROUND = HelpUi.uiColor("Button.pressedBackground", HelpUi.DEFAULT_PRESSED_CHIP_BACKGROUND);
	static final Color HIGHLIGHT_BACKGROUND = HelpUi.uiColor("Table.selectionBackground",
			HelpUi.blend(HelpUi.ACCENT_COLOR, HelpUi.CARD_BACKGROUND, 0.18f));
	static final Color HIGHLIGHT_BORDER = HelpUi.ACCENT_COLOR;
	static final Font BASE_FONT = HelpUi.uiFont("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
	static final Font PARAGRAPH_FONT = HelpUi.BASE_FONT.deriveFont(Font.PLAIN, 14f);
	static final Font TITLE_FONT = HelpUi.BASE_FONT.deriveFont(Font.BOLD, 18f);
	static final Font HEADING_FONT = HelpUi.BASE_FONT.deriveFont(Font.BOLD, 28f);
	static final Font GROUP_TITLE_FONT = HelpUi.BASE_FONT.deriveFont(Font.BOLD, 14f);
	static final Font DESCRIPTION_FONT = HelpUi.BASE_FONT.deriveFont(Font.PLAIN, 13f);
	static final Font KEY_CHIP_FONT = HelpUi.BASE_FONT.deriveFont(Font.BOLD, 12f);

	static final Font GESTURE_CHIP_FONT = HelpUi.BASE_FONT.deriveFont(Font.PLAIN, 12f);
	static final Font INFO_LABEL_FONT = HelpUi.BASE_FONT.deriveFont(Font.BOLD, 13f);
	static final Font INFO_VALUE_FONT = HelpUi.BASE_FONT.deriveFont(Font.PLAIN, 13f);
	static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
	static final Border PAGE_CONTENT_BORDER = new EmptyBorder(HelpUi.PAGE_CONTENT_INSETS);
	static final Border CARD_BORDER = new EmptyBorder(HelpUi.CARD_INSETS);
	static final Border GROUP_BORDER = new EmptyBorder(HelpUi.GROUP_INSETS);
	static final Border ROW_BORDER = new EmptyBorder(HelpUi.ROW_INSETS);
	static final Border TITLE_BORDER = new EmptyBorder(HelpUi.HEADING_TITLE_INSETS);

	static final Border DESCRIPTION_BORDER = new EmptyBorder(HelpUi.HEADING_DESCRIPTION_INSETS);

	static final Border CHIP_BORDER = new CompoundBorder(new LineBorder(HelpUi.BORDER_COLOR, 1, true), new EmptyBorder(HelpUi.CHIP_INSETS));

	static final Border PRESSED_CHIP_BORDER = new CompoundBorder(new LineBorder(HelpUi.ACCENT_COLOR, 1, true),
			new EmptyBorder(HelpUi.PRESSED_CHIP_INSETS));

	private static String stripSimpleHtml(final String text) {
		return text.replace("<html>", "").replace("</html>", "").replace("<b>", "").replace("</b>", "");
	}

	static Color blend(final Color foreground, final Color background, final float amount) {
		final float inverse = 1.0f - amount;
		final int red = Math.round(foreground.getRed() * amount + background.getRed() * inverse);
		final int green = Math.round(foreground.getGreen() * amount + background.getGreen() * inverse);
		final int blue = Math.round(foreground.getBlue() * amount + background.getBlue() * inverse);
		return new Color(red, green, blue);
	}

	static JLabel cardTitle(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(HelpUi.TEXT_COLOR);
		label.setFont(HelpUi.TITLE_FONT);
		return label;
	}

	static JLabel chip(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(HelpUi.TEXT_COLOR);
		label.setBackground(HelpUi.CHIP_BACKGROUND);
		label.setOpaque(true);
		label.setBorder(HelpUi.CHIP_BORDER);
		label.putClientProperty(FlatClientProperties.STYLE, "arc: " + HelpUi.CHIP_ARC);
		return label;
	}

	static JComponent createHeading(final String title) {
		final JPanel heading = new JPanel();
		heading.setOpaque(false);
		heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
		heading.setAlignmentX(Component.LEFT_ALIGNMENT);
		heading.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);

		final JLabel titleLabel = new JLabel(title);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		titleLabel.setForeground(HelpUi.TEXT_COLOR);
		titleLabel.setFont(HelpUi.HEADING_FONT);
		titleLabel.setBorder(HelpUi.TITLE_BORDER);

		heading.add(titleLabel);

		return heading;
	}

	static JPanel createPageContent() {
		final JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(HelpUi.PAGE_CONTENT_BORDER);
		content.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		return content;
	}

	static JScrollPane createScrollPane(final JComponent content) {
		final ScrollablePage page = new ScrollablePage();
		page.add(content, java.awt.BorderLayout.NORTH);

		final JScrollPane scroll = new JScrollPane(page);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(HelpUi.EMPTY_BORDER);
		scroll.getVerticalScrollBar().setUnitIncrement(HelpUi.SCROLL_UNIT_INCREMENT);
		scroll.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0");
		return scroll;
	}

	static JTextArea descriptionLabel(final String description) {
		final JTextArea area = HelpUi.paragraph(HelpUi.stripSimpleHtml(description));
		area.setForeground(HelpUi.TEXT_COLOR);
		area.setFont(HelpUi.DESCRIPTION_FONT);
		area.setAlignmentX(Component.LEFT_ALIGNMENT);
		return area;
	}

	static String fallback(final String value, final String fallback) {
		return HelpUi.hasText(value) ? value : fallback;
	}

	static JLabel gestureChip(final String text) {
		final JLabel label = HelpUi.chip(text);
		label.setFont(HelpUi.GESTURE_CHIP_FONT);
		return label;
	}

	static JLabel groupTitle(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(HelpUi.TEXT_COLOR);
		label.setFont(HelpUi.GROUP_TITLE_FONT);
		return label;
	}

	static boolean hasText(final String value) {
		return value != null && !value.isBlank();
	}

	static JPanel infoRow(final String label, final String value) {
		final JPanel row = new JPanel(new GridBagLayout());
		row.setOpaque(false);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);

		final JLabel labelComponent = new JLabel(label);
		labelComponent.setForeground(HelpUi.TEXT_COLOR);
		labelComponent.setFont(HelpUi.INFO_LABEL_FONT);

		final JTextArea valueComponent = HelpUi.paragraph(value);
		valueComponent.setForeground(HelpUi.MUTED_TEXT_COLOR);
		valueComponent.setFont(HelpUi.INFO_VALUE_FONT);

		final GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.weightx = 0.0;
		labelConstraints.anchor = GridBagConstraints.NORTHWEST;
		labelConstraints.insets = HelpUi.INFO_LABEL_INSETS;
		row.add(labelComponent, labelConstraints);

		final GridBagConstraints valueConstraints = new GridBagConstraints();
		valueConstraints.gridx = 1;
		valueConstraints.gridy = 0;
		valueConstraints.weightx = 1.0;
		valueConstraints.fill = GridBagConstraints.HORIZONTAL;
		valueConstraints.anchor = GridBagConstraints.NORTHWEST;
		row.add(valueComponent, valueConstraints);

		return row;
	}

	static JButton linkButton(final String text, final String url) {
		final JButton button = new JButton(text);
		button.setFocusPainted(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.putClientProperty(FlatClientProperties.STYLE, "arc: 14; borderWidth: 1");

		if (HelpUi.hasText(url)) {
			button.setToolTipText(url);
			button.addActionListener(event -> HelpUi.openLink(url));
		} else {
			button.setEnabled(false);
		}

		return button;
	}

	static void openLink(final String url) {
		if (!HelpUi.hasText(url)) {
			return;
		}
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(URI.create(url));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
			JOptionPane.showMessageDialog(null,
					"The link was copied to your clipboard:\n" + url,
					"Cannot open browser",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	static JTextArea paragraph(final String text) {
		final WrappingTextArea area = new WrappingTextArea(text);
		area.setEditable(false);
		area.setFocusable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setForeground(HelpUi.MUTED_TEXT_COLOR);
		area.setFont(HelpUi.PARAGRAPH_FONT);
		area.setBorder(HelpUi.EMPTY_BORDER);
		area.setAlignmentX(Component.LEFT_ALIGNMENT);
		area.setMaximumSize(HelpUi.FULL_WIDTH_MAXIMUM_SIZE);
		return area;
	}

	static Color uiColor(final String key, final Color fallback) {
		final Color color = UIManager.getColor(key);
		return color == null ? fallback : color;
	}

	static Font uiFont(final String key, final Font fallback) {
		final Font font = UIManager.getFont(key);
		return font == null ? fallback : font;
	}

	private HelpUi() {
	}

}
