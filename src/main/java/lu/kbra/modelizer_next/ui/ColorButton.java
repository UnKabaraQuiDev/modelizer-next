package lu.kbra.modelizer_next.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JColorChooser;

public class ColorButton extends JButton {

	private static final long serialVersionUID = 1L;

	private Color selectedColor;
	private Consumer<Color> callback;

	public ColorButton(final String text, final Color initialColor) {
		super(text);
		this.selectedColor = initialColor == null ? Color.WHITE : initialColor;
		this.setOpaque(true);
		this.setPreferredSize(new Dimension(140, 28));
		this.refreshStyle();

		this.addActionListener(event -> {
			final Color chosen = JColorChooser.showDialog(this, this.getText(), this.selectedColor);
			if (chosen != null) {
				if (callback != null) {
					callback.accept(chosen);
				}
				this.selectedColor = chosen;
				this.refreshStyle();
			}
		});
	}

	public ColorButton(final String text, final Color initialColor, Consumer<Color> cb) {
		super(text);
		this.selectedColor = initialColor == null ? Color.WHITE : initialColor;
		this.callback = cb;
		this.setOpaque(true);
		this.setPreferredSize(new Dimension(140, 28));
		this.refreshStyle();

		this.addActionListener(event -> {
			final Color chosen = JColorChooser.showDialog(this, this.getText(), this.selectedColor);
			if (chosen != null) {
				if (callback != null) {
					callback.accept(chosen);
				}
				this.selectedColor = chosen;
				this.refreshStyle();
			}
		});
	}

	public Color getSelectedColor() {
		return this.selectedColor;
	}

	public void setSelectedColor(final Color selectedColor) {
		this.selectedColor = selectedColor;
		this.refreshStyle();
	}

	private void refreshStyle() {
		this.setBackground(this.selectedColor);
		final int brightness = (this.selectedColor.getRed() + this.selectedColor.getGreen()
				+ this.selectedColor.getBlue()) / 3;
		this.setForeground(brightness < 128 ? Color.WHITE : Color.BLACK);
	}

	@Override
	public String toString() {
		return "ColorButton@" + System.identityHashCode(this) + " [selectedColor=" + selectedColor + "]";
	}

}