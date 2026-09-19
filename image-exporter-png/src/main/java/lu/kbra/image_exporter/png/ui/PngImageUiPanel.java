package lu.kbra.image_exporter.png.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.Getter;
import lombok.Setter;
import lu.kbra.model_exporter.api.UiProvider;
import lu.kbra.pclib.PCUtils;

@Getter
@Setter
public class PngImageUiPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField txtOuputPath;

	/**
	 * Create the panel.
	 */
	public PngImageUiPanel() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		final JLabel lblDataClassesPath = new JLabel("Output path");
		final GridBagConstraints gbc_lblDataClassesPath = new GridBagConstraints();
		gbc_lblDataClassesPath.anchor = GridBagConstraints.EAST;
		gbc_lblDataClassesPath.insets = new Insets(0, 0, 0, 5);
		gbc_lblDataClassesPath.gridx = 0;
		gbc_lblDataClassesPath.gridy = 0;
		this.add(lblDataClassesPath, gbc_lblDataClassesPath);

		this.txtOuputPath = new JTextField();
		this.txtOuputPath.setText(".");
		final GridBagConstraints gbc_txtDataPath = new GridBagConstraints();
		gbc_txtDataPath.insets = new Insets(0, 0, 0, 5);
		gbc_txtDataPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDataPath.gridx = 1;
		gbc_txtDataPath.gridy = 0;
		this.add(this.txtOuputPath, gbc_txtDataPath);
		this.txtOuputPath.setColumns(10);

		final ImageIcon openFolderIcon = UiProvider
				.scaleIcon(new ImageIcon(PCUtils.readPackagedBytesFile(this.getClass(), "/icons/open-folder.png")), 20, 20);

		final JButton btnSelectTablePath = new JButton();
		btnSelectTablePath.setIcon(openFolderIcon);
		final GridBagConstraints gbc_btnSelectTablePath = new GridBagConstraints();
		gbc_btnSelectTablePath.gridx = 2;
		gbc_btnSelectTablePath.gridy = 0;
		this.add(btnSelectTablePath, gbc_btnSelectTablePath);
		btnSelectTablePath.addActionListener(e -> UiProvider.selectDir(PngImageUiPanel.this, this.txtOuputPath));
	}

}
