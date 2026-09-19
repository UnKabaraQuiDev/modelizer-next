package lu.kbra.image_exporter.png.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import lombok.Getter;

@Getter
public class PngImageUiPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JSlider compressionLevel;

	/**
	 * Create the panel.
	 */
	public PngImageUiPanel() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {120, 0, 0};
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		final JLabel lblCompression = new JLabel("Compression");
		final GridBagConstraints gbc_lblCompression = new GridBagConstraints();
		gbc_lblCompression.anchor = GridBagConstraints.EAST;
		gbc_lblCompression.insets = new Insets(0, 0, 0, 5);
		gbc_lblCompression.gridx = 0;
		gbc_lblCompression.gridy = 0;
		this.add(lblCompression, gbc_lblCompression);

		this.compressionLevel = new JSlider(0, 9, 2);
		final GridBagConstraints gbc_compressionLevel = new GridBagConstraints();
		gbc_compressionLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_compressionLevel.gridx = 1;
		gbc_compressionLevel.gridy = 0;
		this.add(this.compressionLevel, gbc_compressionLevel);
	}

}
