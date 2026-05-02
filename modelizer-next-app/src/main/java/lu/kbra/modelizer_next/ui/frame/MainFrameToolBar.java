package lu.kbra.modelizer_next.ui.frame;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.pclib.PCUtils;

final class MainFrameToolBar extends JToolBar {

	private final Map<String, ImageIcon> toolbarIconCache = new HashMap<>();

	private static final long serialVersionUID = 1L;
	JButton undoButton;
	JButton redoButton;

	MainFrameToolBar(final MainFrame frame) {
		this.setFloatable(false);
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());

		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		buttons.add(this.undoButton = this.createToolbarButton(frame, "undo.png", "Undo", "undo"));
		buttons.add(this.redoButton = this.createToolbarButton(frame, "redo.png", "Redo", "redo"));
		buttons.add(this.createToolbarButton(frame, "add-table.png", "New table", "addTable"));
		buttons.add(this.createToolbarButton(frame, "add-field.png", "New field", "addField"));
		buttons.add(this.createToolbarButton(frame, "add-comment.png", "New comment", "addComment"));
		buttons.add(this.createToolbarButton(frame, "add-link.png", "New link", "addLink"));
		buttons.add(this.createToolbarButton(frame, "delete.png", "Delete", "deleteSelection"));
		buttons.add(this.createToolbarButton(frame, "duplicate.png", "Duplicate", "duplicateSelection"));

		this.add(buttons, BorderLayout.WEST);
	}

	private JButton createToolbarButton(final MainFrame frame, final String icon, final String description, final String actionKey) {
		final JButton button = new JButton();
		button.setIcon(this.getToolbarIcon(frame, icon));

		button.putClientProperty("baseText", description);
		button.putClientProperty("actionKey", actionKey);

		button.addActionListener(event -> {
			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final ActionEvent actionEvent = new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey);

			final Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(actionEvent);
				canvas.requestFocusInWindow();
			}
		});

		final DiagramCanvas canvas = frame.getActiveCanvas();
		if (canvas != null) {
			final String shortcutText = frame.findShortcutText(canvas, actionKey);
			if (!shortcutText.isBlank()) {
				button.setToolTipText(description + " (" + shortcutText + ")");
			}
		}

		button.setPreferredSize(new Dimension(40, 40));
		return button;
	}

	private ImageIcon getToolbarIcon(final MainFrame frame, final String icon) {
		return this.toolbarIconCache.computeIfAbsent(icon, key -> {
			final ImageIcon rawIcon = new ImageIcon(PCUtils.readPackagedBytesFile(frame.getClass(), "/icons/" + key));

			return MainFrameToolBar.scaleIcon(rawIcon, 34, 34);
		});
	}

	private static ImageIcon scaleIcon(final ImageIcon icon, final int targetWidth, final int targetHeight) {
		BufferedImage current = MainFrameToolBar.toBufferedImage(icon.getImage());
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

	private static BufferedImage toBufferedImage(final Image image) {
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
