package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.App;
import lu.kbra.pclib.PCUtils;

public class HelpDialog extends JFrame {

	private static final long serialVersionUID = -2242189520928100036L;

	public HelpDialog() {
		super(App.title("Help"));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		final JTabbedPane tabs = new JTabbedPane();

		tabs.addTab("Gestures", this.createScrollPage("/illustrations/Page 1.png", "/illustrations/Page 2.png"));

		this.add(tabs, BorderLayout.CENTER);

		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
	}

	private JScrollPane createScrollPage(final String... imagePaths) {

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (final String path : imagePaths) {
			final ImagePanel label = new ImagePanel(path);
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(label);
		}

		final JScrollPane scroll = new JScrollPane(panel);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		return scroll;
	}

	static class ImagePanel extends JPanel {

		private static final long serialVersionUID = 6562168351687457107L;
		private final Image image;

		public ImagePanel(final String path) {
			this.image = new ImageIcon(PCUtils.readPackagedBytesFile(this.getClass(), path)).getImage();

			this.setOpaque(false);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);

			if (this.image == null) {
				return;
			}

			final int panelWidth = this.getWidth();

			final int imgW = this.image.getWidth(null);
			final int imgH = this.image.getHeight(null);

			if (imgW <= 0 || imgH <= 0) {
				return;
			}

			final int newH = (int) ((double) imgH / imgW * panelWidth);

			g.drawImage(this.image, 0, 0, panelWidth, newH, this);
		}

		@Override
		public Dimension getPreferredSize() {
			final int imgW = this.image.getWidth(null);
			final int imgH = this.image.getHeight(null);

			if (imgW <= 0 || imgH <= 0) {
				return new Dimension(800, 600);
			}

			return new Dimension(800, (int) ((double) imgH / imgW * 800));
		}
	}

	public static void main(final String[] args) {
		final HelpDialog hd = new HelpDialog();
		hd.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		hd.setVisible(true);
	}

}
