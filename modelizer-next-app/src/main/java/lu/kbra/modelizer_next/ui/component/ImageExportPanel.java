package lu.kbra.modelizer_next.ui.component;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.Getter;
import lu.kbra.model_exporter.api.ColorButton;
import lu.kbra.model_exporter.api.UiProvider;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;

@Getter
public class ImageExportPanel extends JPanel {

	private static final long serialVersionUID = 8406840794490744635L;

	private final JTextField textOutputPath;
	private final JTextField textFilenamePattern;
	private final JComboBox<ViewExportScope> viewScope;

	private final JCheckBox chckbxConceptual;
	private final JCheckBox chckbxLogical;
	private final JCheckBox chckbxPhysical;
	private final JLabel lblBackgroundColor;
	private final ColorButton clrbtnColor;
	private final JLabel lblPanels;
	private final JCheckBox chckbxTransparentBackground;

	public ImageExportPanel() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 120, 136, 164, 71, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		final JLabel lblOutputPath = new JLabel("Output path");
		final GridBagConstraints gbc_lblOutputPath = new GridBagConstraints();
		gbc_lblOutputPath.anchor = GridBagConstraints.EAST;
		gbc_lblOutputPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputPath.gridx = 0;
		gbc_lblOutputPath.gridy = 0;
		this.add(lblOutputPath, gbc_lblOutputPath);

		this.textOutputPath = new JTextField();
		this.textOutputPath.setText(".");
		final GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		this.add(this.textOutputPath, gbc_textField);
		this.textOutputPath.setColumns(10);

		final JButton button = new JButton(UiProvider.OPEN_FOLDER_ICON);
		final GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.insets = new Insets(0, 0, 5, 0);
		gbc_button.gridx = 4;
		gbc_button.gridy = 0;
		this.add(button, gbc_button);

		final JLabel lblFileNamePattern = new JLabel("File name pattern");
		final GridBagConstraints gbc_lblFileNamePattern = new GridBagConstraints();
		gbc_lblFileNamePattern.anchor = GridBagConstraints.EAST;
		gbc_lblFileNamePattern.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileNamePattern.gridx = 0;
		gbc_lblFileNamePattern.gridy = 1;
		this.add(lblFileNamePattern, gbc_lblFileNamePattern);

		this.textFilenamePattern = new JTextField();
		this.textFilenamePattern.setText("{FILENAME}-{PANEL}");
		final GridBagConstraints gbc_txtfilenamepanel = new GridBagConstraints();
		gbc_txtfilenamepanel.fill = GridBagConstraints.BOTH;
		gbc_txtfilenamepanel.gridwidth = 3;
		gbc_txtfilenamepanel.insets = new Insets(0, 0, 5, 5);
		gbc_txtfilenamepanel.gridx = 1;
		gbc_txtfilenamepanel.gridy = 1;
		this.add(this.textFilenamePattern, gbc_txtfilenamepanel);
		this.textFilenamePattern.setColumns(10);

		final JLabel lblScope = new JLabel("Scope");
		final GridBagConstraints gbc_lblScope = new GridBagConstraints();
		gbc_lblScope.anchor = GridBagConstraints.EAST;
		gbc_lblScope.insets = new Insets(0, 0, 5, 5);
		gbc_lblScope.gridx = 0;
		gbc_lblScope.gridy = 2;
		this.add(lblScope, gbc_lblScope);

		this.viewScope = new JComboBox<>(ViewExportScope.values());
		this.viewScope.setSelectedItem(ViewExportScope.EVERYTHING);
		final GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridwidth = 3;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		this.add(this.viewScope, gbc_comboBox);

		this.lblPanels = new JLabel("Panels");
		final GridBagConstraints gbc_lblPanels = new GridBagConstraints();
		gbc_lblPanels.anchor = GridBagConstraints.EAST;
		gbc_lblPanels.insets = new Insets(0, 0, 5, 5);
		gbc_lblPanels.gridx = 0;
		gbc_lblPanels.gridy = 3;
		this.add(this.lblPanels, gbc_lblPanels);

		this.chckbxLogical = new JCheckBox("Logical");
		this.chckbxLogical.setSelected(true);
		final GridBagConstraints gbc_chckbxLogical = new GridBagConstraints();
		gbc_chckbxLogical.weightx = 1.0;
		gbc_chckbxLogical.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxLogical.gridx = 1;
		gbc_chckbxLogical.gridy = 3;
		this.add(this.chckbxLogical, gbc_chckbxLogical);

		this.chckbxPhysical = new JCheckBox("Physical");
		this.chckbxPhysical.setSelected(true);
		final GridBagConstraints gbc_chckbxPhysical = new GridBagConstraints();
		gbc_chckbxPhysical.weightx = 1.0;
		gbc_chckbxPhysical.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxPhysical.gridx = 2;
		gbc_chckbxPhysical.gridy = 3;
		this.add(this.chckbxPhysical, gbc_chckbxPhysical);

		this.chckbxConceptual = new JCheckBox("Conceptual");
		this.chckbxConceptual.setSelected(true);
		final GridBagConstraints gbc_chckbxConceptual = new GridBagConstraints();
		gbc_chckbxConceptual.gridwidth = 2;
		gbc_chckbxConceptual.weightx = 1.0;
		gbc_chckbxConceptual.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxConceptual.gridx = 3;
		gbc_chckbxConceptual.gridy = 3;
		this.add(this.chckbxConceptual, gbc_chckbxConceptual);

		this.lblBackgroundColor = new JLabel("Background color");
		final GridBagConstraints gbc_lblBackgroundColor = new GridBagConstraints();
		gbc_lblBackgroundColor.anchor = GridBagConstraints.EAST;
		gbc_lblBackgroundColor.insets = new Insets(0, 0, 0, 5);
		gbc_lblBackgroundColor.gridx = 0;
		gbc_lblBackgroundColor.gridy = 4;
		this.add(this.lblBackgroundColor, gbc_lblBackgroundColor);

		this.clrbtnColor = new ColorButton((String) null, (Color) null);
		final GridBagConstraints gbc_clrbtnColor = new GridBagConstraints();
		gbc_clrbtnColor.fill = GridBagConstraints.BOTH;
		gbc_clrbtnColor.insets = new Insets(0, 0, 0, 5);
		gbc_clrbtnColor.gridx = 1;
		gbc_clrbtnColor.gridy = 4;
		this.add(this.clrbtnColor, gbc_clrbtnColor);

		this.chckbxTransparentBackground = new JCheckBox("Transparent background");
		final GridBagConstraints gbc_chckbxTransparentBackground = new GridBagConstraints();
		gbc_chckbxTransparentBackground.gridwidth = 2;
		gbc_chckbxTransparentBackground.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxTransparentBackground.gridx = 2;
		gbc_chckbxTransparentBackground.gridy = 4;
		this.add(this.chckbxTransparentBackground, gbc_chckbxTransparentBackground);

		button.addActionListener(a -> UiProvider.selectDir(this, this.textOutputPath));
	}

}
