package lu.kbra.modelizer_next.ui.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import lombok.Getter;

@Getter
public class ImageSizePanel extends JPanel {

	private static final long serialVersionUID = 8679328691170005077L;

	private final JSpinner spinWidth;
	private final JSpinner spinHeight;
	private final JLabel lblWidth;
	private final JLabel lblHeight;
	private final JButton btnUse;

	public ImageSizePanel() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {120, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		final JLabel lblNewLabel = new JLabel("Max. size");
		final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		this.add(lblNewLabel, gbc_lblNewLabel);

		final JLabel lblWidth1 = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 1;
		gbc_lblWidth.gridy = 0;
		this.add(lblWidth1, gbc_lblWidth);

		this.spinWidth = new JSpinner();
		final GridBagConstraints gbc_spinWidth = new GridBagConstraints();
		gbc_spinWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinWidth.insets = new Insets(0, 0, 5, 5);
		gbc_spinWidth.gridx = 2;
		gbc_spinWidth.gridy = 0;
		this.add(this.spinWidth, gbc_spinWidth);

		final JLabel lblHeight1 = new JLabel("Height");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 3;
		gbc_lblHeight.gridy = 0;
		this.add(lblHeight1, gbc_lblHeight);

		this.spinHeight = new JSpinner();
		final GridBagConstraints gbc_spinHeight = new GridBagConstraints();
		gbc_spinHeight.insets = new Insets(0, 0, 5, 0);
		gbc_spinHeight.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinHeight.gridx = 4;
		gbc_spinHeight.gridy = 0;
		this.add(this.spinHeight, gbc_spinHeight);

		final JLabel lblComputedSize = new JLabel("Computed size");
		final GridBagConstraints gbc_lblComputedSize = new GridBagConstraints();
		gbc_lblComputedSize.anchor = GridBagConstraints.EAST;
		gbc_lblComputedSize.insets = new Insets(0, 0, 0, 5);
		gbc_lblComputedSize.gridx = 0;
		gbc_lblComputedSize.gridy = 1;
		this.add(lblComputedSize, gbc_lblComputedSize);
		
				this.lblWidth = new JLabel(".");
				final GridBagConstraints gbc_lblWidth_1 = new GridBagConstraints();
				gbc_lblWidth_1.insets = new Insets(0, 0, 0, 5);
				gbc_lblWidth_1.gridx = 1;
				gbc_lblWidth_1.gridy = 1;
				this.add(this.lblWidth, gbc_lblWidth_1);

		this.lblHeight = new JLabel(".");
		final GridBagConstraints gbc_lblHeight_1 = new GridBagConstraints();
		gbc_lblHeight_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblHeight_1.gridx = 3;
		gbc_lblHeight_1.gridy = 1;
		this.add(this.lblHeight, gbc_lblHeight_1);

		this.btnUse = new JButton("Use");
		final GridBagConstraints gbc_btnUse = new GridBagConstraints();
		gbc_btnUse.gridx = 4;
		gbc_btnUse.gridy = 1;
		this.add(this.btnUse, gbc_btnUse);
	}

}
