package lu.kbra.model_exporter.api;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lu.kbra.pclib.PCUtils;

public interface UiProvider {

	JMenuItem buildMenuItem();

	boolean hasCustomUI();

	JPanel buildUI();

	ExporterOptions getOptions(JPanel panel);

	void restoreOptions(JPanel panel, ExporterOptions options);

	ImageIcon OPEN_FOLDER_ICON = UiProvider
			.scaleIcon(new ImageIcon(PCUtils.readPackagedBytesFile(UiProvider.class, "/icons/open-folder.png")), 20, 20);

	static ImageIcon scaleIcon(final ImageIcon icon, final int targetWidth, final int targetHeight) {
		BufferedImage current = UiProvider.toBufferedImage(icon.getImage());
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

	static void selectDir(final JComponent parent, final JTextField field) {
		final JFileChooser fileChooser = new JFileChooser();

		final String currentPath = field.getText();

		final URI currentDocumentFile = ExporterApiContext.getApiContext().getCurrentDocument();

		if (!currentPath.isBlank() && currentDocumentFile != null) {
			final Path configDir = Paths.get(currentDocumentFile).getParent();
			final Path selectedPath = Paths.get(currentPath);

			final Path resolvedPath = selectedPath.isAbsolute() ? selectedPath : configDir.resolve(selectedPath);

			File currentDir = resolvedPath.toFile();
			while (!currentDir.isDirectory() && currentDir.getParentFile() != null) {
				currentDir = currentDir.getParentFile();
			}

			if (currentDir.isDirectory()) {
				fileChooser.setCurrentDirectory(currentDir);
			}
		}

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			final String newPath = currentDocumentFile != null
					? Paths.get(currentDocumentFile)
							.getParent()
							.relativize(fileChooser.getSelectedFile().toPath().toAbsolutePath())
							.toString()
					: fileChooser.getSelectedFile().getAbsolutePath();
			field.setText(newPath.isBlank() ? "." : newPath);
		}
	}

}
