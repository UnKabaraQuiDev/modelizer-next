package lu.kbra.code_exporter.api.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public interface UIProvider {

	JMenuItem buildMenuItem();

	JPanel buildUI();

	ExporterOptions getOptions(JPanel panel);

	void restoreOptions(JPanel panel, File currentDocumentFile, File currentConfigFile, ExporterOptions options);

	static ImageIcon scaleIcon(final ImageIcon icon, final int targetWidth, final int targetHeight) {
		BufferedImage current = toBufferedImage(icon.getImage());
		int width = current.getWidth();
		int height = current.getHeight();
		while (width > targetWidth || height > targetHeight) {
			width = Math.max(targetWidth, width / 2);
			height = Math.max(targetHeight, height / 2);
			final BufferedImage next = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = next.createGraphics();
			try {
				g.setComposite(AlphaComposite.Src);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(current, 0, 0, width, height, null);
			} finally {
				g.dispose();
			}
			current = next;
		}
		return new ImageIcon(current);
	}

	static BufferedImage toBufferedImage(final Image image) {
		if (image instanceof final BufferedImage bufferedImage) {
			return bufferedImage;
		}
		final BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = buffered.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(image, 0, 0, null);
		} finally {
			g.dispose();
		}
		return buffered;
	}

}
