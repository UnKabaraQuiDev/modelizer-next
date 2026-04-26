package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.component.ColorButton;

public final class StylePaletteEditorDialog {

	private static final class Holder {
		private StylePalette palette;
	}

	private static final class StylePalettePreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
		private static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

		private StylePalette palette;

		private StylePalettePreviewPanel() {
			this.setPreferredSize(new Dimension(720, 280));
			this.setBackground(new Color(0xF2F2F2));
			this.setBorder(BorderFactory.createTitledBorder("Preview"));
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);

			if (this.palette == null) {
				return;
			}

			final Graphics2D g2 = (Graphics2D) graphics.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			try {
				final Rectangle2D classBounds = new Rectangle2D.Double(40, 40, 220, 100);
				final Rectangle2D commentBounds = new Rectangle2D.Double(430, 60, 210, 80);

				g2.setColor(this.palette.getLinkColor());
				g2.setStroke(new BasicStroke(1.2f));
				g2.draw(new Line2D.Double(classBounds.getMaxX(),
						classBounds.getCenterY(),
						commentBounds.getX(),
						commentBounds.getCenterY()));

				g2.setColor(this.palette.getClassBackgroundColor());
				g2.fill(classBounds);

				g2.setFont(StylePalettePreviewPanel.TITLE_FONT);
				g2.setColor(this.palette.getClassTextColor());
				g2.drawString("Example table", 48, 60);

				g2.setColor(this.palette.getFieldBackgroundColor());
				g2.fill(new Rectangle2D.Double(classBounds.getX(), classBounds.getY() + 28, classBounds.getWidth(), 22));
				g2.fill(new Rectangle2D.Double(classBounds.getX(), classBounds.getY() + 50, classBounds.getWidth(), 22));
				g2.fill(new Rectangle2D.Double(classBounds.getX(), classBounds.getY() + 72, classBounds.getWidth(), 22));

				g2.setColor(this.palette.getClassBorderColor());
				g2.draw(new Line2D.Double(classBounds.getX(), classBounds.getY() + 50, classBounds.getMaxX(), classBounds.getY() + 50));
				g2.draw(new Line2D.Double(classBounds.getX(), classBounds.getY() + 72, classBounds.getMaxX(), classBounds.getY() + 72));
				g2.draw(new Line2D.Double(classBounds.getX(), classBounds.getY() + 94, classBounds.getMaxX(), classBounds.getY() + 94));

				g2.setFont(StylePalettePreviewPanel.BODY_FONT);
				g2.setColor(this.palette.getFieldTextColor());
				g2.drawString("FIELD_ID [PK, NN]", 48, 88);
				g2.drawString("DISPLAY_NAME", 48, 110);
				g2.drawString("EMAIL [UQ]", 48, 132);

				g2.setColor(this.palette.getClassBorderColor());
				g2.draw(classBounds);
				g2.draw(new Line2D.Double(classBounds.getX(), classBounds.getY() + 28, classBounds.getMaxX(), classBounds.getY() + 28));

				g2.setColor(this.palette.getCommentBackgroundColor());
				g2.fill(commentBounds);
				g2.setColor(this.palette.getCommentBorderColor());
				g2.draw(commentBounds);

				g2.setColor(this.palette.getCommentTextColor());
				g2.drawString("Example comment", 442, 92);
				g2.drawString("Preview of the palette.", 442, 112);

				g2.setColor(this.palette.getLinkColor());
				g2.drawString("Example link", 305, 95);
			} finally {
				g2.dispose();
			}
		}

		private void setPalette(final StylePalette palette) {
			this.palette = palette;
			this.repaint();
		}
	}

	private StylePaletteEditorDialog() {
	}

	public static StylePalette showDialog(final Component parent) {
		return StylePaletteEditorDialog.showDialog(parent, null);
	}

	public static StylePalette showDialog(final Component parent, final StylePalette initialPalette) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final boolean editing = initialPalette != null;
		final JDialog dialog = new JDialog(owner,
				editing ? "Edit style palette" : "New style palette",
				Dialog.ModalityType.APPLICATION_MODAL);

		final StylePalettePreviewPanel previewPanel = new StylePalettePreviewPanel();

		final JTextField nameField = new JTextField(initialPalette == null ? "New palette" : initialPalette.getName(), 24);
		nameField.setEnabled(initialPalette == null ? true : !"Default".equals(initialPalette.getName()));

		final ColorButton classTextColorButton = new ColorButton("Class text",
				initialPalette == null ? Color.BLACK : initialPalette.getClassTextColor(),
				c -> {
					previewPanel.palette.setClassTextColor(c);
					previewPanel.repaint();
				});
		final ColorButton classBackgroundColorButton = new ColorButton("Class background",
				initialPalette == null ? new Color(0xFFF59D) : initialPalette.getClassBackgroundColor(),
				c -> {
					previewPanel.palette.setClassBackgroundColor(c);
					previewPanel.repaint();
				});
		final ColorButton classBorderColorButton = new ColorButton("Class border",
				initialPalette == null ? new Color(0x333333) : initialPalette.getClassBorderColor(),
				c -> {
					previewPanel.palette.setClassBorderColor(c);
					previewPanel.repaint();
				});

		final ColorButton fieldTextColorButton = new ColorButton("Field text",
				initialPalette == null ? Color.BLACK : initialPalette.getFieldTextColor(),
				c -> {
					previewPanel.palette.setFieldTextColor(c);
					previewPanel.repaint();
				});
		final ColorButton fieldBackgroundColorButton = new ColorButton("Field background",
				initialPalette == null ? Color.WHITE : initialPalette.getFieldBackgroundColor(),
				c -> {
					previewPanel.palette.setFieldBackgroundColor(c);
					previewPanel.repaint();
				});

		final ColorButton commentTextColorButton = new ColorButton("Comment text",
				initialPalette == null ? new Color(0x333333) : initialPalette.getCommentTextColor(),
				c -> {
					previewPanel.palette.setCommentTextColor(c);
					previewPanel.repaint();
				});
		final ColorButton commentBackgroundColorButton = new ColorButton("Comment background",
				initialPalette == null ? new Color(0xFFF8CC) : initialPalette.getCommentBackgroundColor(),
				c -> {
					previewPanel.palette.setCommentBackgroundColor(c);
					previewPanel.repaint();
				});
		final ColorButton commentBorderColorButton = new ColorButton("Comment border",
				initialPalette == null ? new Color(0x444444) : initialPalette.getCommentBorderColor(),
				c -> {
					previewPanel.palette.setClassBorderColor(c);
					previewPanel.repaint();
				});

		final ColorButton linkColorButton = new ColorButton("Link color",
				initialPalette == null ? new Color(0x555555) : initialPalette.getLinkColor(),
				c -> {
					previewPanel.palette.setLinkColor(c);
					previewPanel.repaint();
				});

		final Runnable refreshPreview = () -> previewPanel.setPalette(StylePaletteEditorDialog.buildPalette(nameField.getText(),
				classTextColorButton,
				classBackgroundColorButton,
				classBorderColorButton,
				fieldTextColorButton,
				fieldBackgroundColorButton,
				commentTextColorButton,
				commentBackgroundColorButton,
				commentBorderColorButton,
				linkColorButton));

		final java.awt.event.ActionListener previewListener = event -> refreshPreview.run();

		classTextColorButton.addActionListener(previewListener);
		classBackgroundColorButton.addActionListener(previewListener);
		classBorderColorButton.addActionListener(previewListener);
		fieldTextColorButton.addActionListener(previewListener);
		fieldBackgroundColorButton.addActionListener(previewListener);
		commentTextColorButton.addActionListener(previewListener);
		commentBackgroundColorButton.addActionListener(previewListener);
		commentBorderColorButton.addActionListener(previewListener);
		linkColorButton.addActionListener(previewListener);

		final JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		form.add(StylePaletteEditorDialog.row("Name", nameField));
		form.add(StylePaletteEditorDialog.row("Class",
				StylePaletteEditorDialog.flow(classTextColorButton, classBackgroundColorButton, classBorderColorButton)));
		form.add(StylePaletteEditorDialog.row("Field", StylePaletteEditorDialog.flow(fieldTextColorButton, fieldBackgroundColorButton)));
		form.add(StylePaletteEditorDialog.row("Comment",
				StylePaletteEditorDialog.flow(commentTextColorButton, commentBackgroundColorButton, commentBorderColorButton)));
		form.add(StylePaletteEditorDialog.row("Link", StylePaletteEditorDialog.flow(linkColorButton)));

		final JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(form, BorderLayout.NORTH);
		centerPanel.add(previewPanel, BorderLayout.CENTER);

		final Holder holder = new Holder();

		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(event -> {
			holder.palette = StylePaletteEditorDialog.buildPalette(nameField.getText(),
					classTextColorButton,
					classBackgroundColorButton,
					classBorderColorButton,
					fieldTextColorButton,
					fieldBackgroundColorButton,
					commentTextColorButton,
					commentBackgroundColorButton,
					commentBorderColorButton,
					linkColorButton);
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		dialog.getRootPane().setDefaultButton(saveButton);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		dialog.getRootPane().getActionMap().put("cancel", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dialog.setLayout(new BorderLayout(8, 8));
		dialog.add(centerPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		refreshPreview.run();

		dialog.pack();
		dialog.setMinimumSize(new Dimension(760, 520));
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return holder.palette;
	}

	private static StylePalette buildPalette(
			final String name,
			final ColorButton classTextColorButton,
			final ColorButton classBackgroundColorButton,
			final ColorButton classBorderColorButton,
			final ColorButton fieldTextColorButton,
			final ColorButton fieldBackgroundColorButton,
			final ColorButton commentTextColorButton,
			final ColorButton commentBackgroundColorButton,
			final ColorButton commentBorderColorButton,
			final ColorButton linkColorButton) {
		final StylePalette palette = new StylePalette();
		palette.setName(name == null || name.isBlank() ? "Unnamed palette" : name);
		palette.setClassTextColor(classTextColorButton.getSelectedColor());
		palette.setClassBackgroundColor(classBackgroundColorButton.getSelectedColor());
		palette.setClassBorderColor(classBorderColorButton.getSelectedColor());
		palette.setFieldTextColor(fieldTextColorButton.getSelectedColor());
		palette.setFieldBackgroundColor(fieldBackgroundColorButton.getSelectedColor());
		palette.setCommentTextColor(commentTextColorButton.getSelectedColor());
		palette.setCommentBackgroundColor(commentBackgroundColorButton.getSelectedColor());
		palette.setCommentBorderColor(commentBorderColorButton.getSelectedColor());
		palette.setLinkColor(linkColorButton.getSelectedColor());
		return palette;
	}

	private static JPanel flow(final Component... components) {
		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		for (final Component component : components) {
			panel.add(component);
		}
		return panel;
	}

	private static JPanel row(final String label, final Component component) {
		final JPanel panel = new JPanel(new BorderLayout(6, 6));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		panel.add(new JLabel(label), BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

}
