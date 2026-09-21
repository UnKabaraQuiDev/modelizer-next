package lu.kbra.code_exporter.java.pclib.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.Getter;
import lombok.Setter;
import lu.kbra.model_exporter.api.UiProvider;

@Getter
@Setter
public class JavaPclibUiPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField txtDataPath;
	private final JTextField txtTablePath;
	private final JTextField dataClassesPackage;
	private final JTextField tableClassesPackage;
	private final JCheckBox chckbxSplitBySchema;
	private final JCheckBox chckbxOverwriteFiles;
	private final JCheckBox chckbxExportTableClasses;
	private final JCheckBox chckbxExportDataClasses;
	private final JCheckBox chckbxMergeFiles;
	private final JCheckBox chckbxCorrectNaming;
	private final JCheckBox chckbxUseSpring;
	private final JCheckBox chckbxKeepSimpleNames;
	private final JTextField springDatabaseBean;
	private final JLabel lblDbms;
	private final JComboBox<String> comboBoxDbms;

	/**
	 * Create the panel.
	 */
	public JavaPclibUiPanel() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		final JLabel lblDataClassesPath = new JLabel("Data classes path");
		final GridBagConstraints gbc_lblDataClassesPath = new GridBagConstraints();
		gbc_lblDataClassesPath.anchor = GridBagConstraints.EAST;
		gbc_lblDataClassesPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblDataClassesPath.gridx = 0;
		gbc_lblDataClassesPath.gridy = 0;
		this.add(lblDataClassesPath, gbc_lblDataClassesPath);

		this.txtDataPath = new JTextField();
		this.txtDataPath.setText("data/");
		final GridBagConstraints gbc_txtDataPath = new GridBagConstraints();
		gbc_txtDataPath.gridwidth = 2;
		gbc_txtDataPath.insets = new Insets(0, 0, 5, 5);
		gbc_txtDataPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDataPath.gridx = 1;
		gbc_txtDataPath.gridy = 0;
		this.add(this.txtDataPath, gbc_txtDataPath);
		this.txtDataPath.setColumns(10);

		final JButton btnSelectDataPath = new JButton();
		btnSelectDataPath.setIcon(UiProvider.OPEN_FOLDER_ICON);
		final GridBagConstraints gbc_btnSelectTablePath = new GridBagConstraints();
		gbc_btnSelectTablePath.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectTablePath.gridx = 3;
		gbc_btnSelectTablePath.gridy = 0;
		this.add(btnSelectDataPath, gbc_btnSelectTablePath);

		final JLabel lblTablePath = new JLabel("Tables classes path");
		final GridBagConstraints gbc_lblTablePath = new GridBagConstraints();
		gbc_lblTablePath.anchor = GridBagConstraints.EAST;
		gbc_lblTablePath.insets = new Insets(0, 0, 5, 5);
		gbc_lblTablePath.gridx = 0;
		gbc_lblTablePath.gridy = 1;
		this.add(lblTablePath, gbc_lblTablePath);

		this.txtTablePath = new JTextField();
		this.txtTablePath.setText("table/");
		final GridBagConstraints gbc_txtTablePath = new GridBagConstraints();
		gbc_txtTablePath.gridwidth = 2;
		gbc_txtTablePath.insets = new Insets(0, 0, 5, 5);
		gbc_txtTablePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTablePath.gridx = 1;
		gbc_txtTablePath.gridy = 1;
		this.add(this.txtTablePath, gbc_txtTablePath);
		this.txtTablePath.setColumns(10);

		final JButton btnSelectTablePath = new JButton();
		btnSelectTablePath.setIcon(UiProvider.OPEN_FOLDER_ICON);
		final GridBagConstraints gbc_btnSelectDataPath = new GridBagConstraints();
		gbc_btnSelectDataPath.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectDataPath.gridx = 3;
		gbc_btnSelectDataPath.gridy = 1;
		this.add(btnSelectTablePath, gbc_btnSelectDataPath);

		final JLabel lblDataClassesPackage = new JLabel("Data classes package");
		final GridBagConstraints gbc_lblDataClassesPackage = new GridBagConstraints();
		gbc_lblDataClassesPackage.anchor = GridBagConstraints.EAST;
		gbc_lblDataClassesPackage.insets = new Insets(0, 0, 5, 5);
		gbc_lblDataClassesPackage.gridx = 0;
		gbc_lblDataClassesPackage.gridy = 2;
		this.add(lblDataClassesPackage, gbc_lblDataClassesPackage);

		this.dataClassesPackage = new JTextField();
		final GridBagConstraints gbc_dataClassesPackage = new GridBagConstraints();
		gbc_dataClassesPackage.gridwidth = 3;
		gbc_dataClassesPackage.insets = new Insets(0, 0, 5, 0);
		gbc_dataClassesPackage.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataClassesPackage.gridx = 1;
		gbc_dataClassesPackage.gridy = 2;
		this.add(this.dataClassesPackage, gbc_dataClassesPackage);
		this.dataClassesPackage.setColumns(10);

		final JLabel lblTablesClassesPackage = new JLabel("Tables classes package");
		final GridBagConstraints gbc_lblTablesClassesPackage = new GridBagConstraints();
		gbc_lblTablesClassesPackage.anchor = GridBagConstraints.EAST;
		gbc_lblTablesClassesPackage.insets = new Insets(0, 0, 5, 5);
		gbc_lblTablesClassesPackage.gridx = 0;
		gbc_lblTablesClassesPackage.gridy = 3;
		this.add(lblTablesClassesPackage, gbc_lblTablesClassesPackage);

		this.tableClassesPackage = new JTextField();
		final GridBagConstraints gbc_tableClassesPackage = new GridBagConstraints();
		gbc_tableClassesPackage.insets = new Insets(0, 0, 5, 0);
		gbc_tableClassesPackage.gridwidth = 3;
		gbc_tableClassesPackage.fill = GridBagConstraints.HORIZONTAL;
		gbc_tableClassesPackage.gridx = 1;
		gbc_tableClassesPackage.gridy = 3;
		this.add(this.tableClassesPackage, gbc_tableClassesPackage);
		this.tableClassesPackage.setColumns(10);

		this.chckbxExportDataClasses = new JCheckBox("Export data classes");
		this.chckbxExportDataClasses.setSelected(true);
		final GridBagConstraints gbc_chckbxExportDataClasses = new GridBagConstraints();
		gbc_chckbxExportDataClasses.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExportDataClasses.gridx = 1;
		gbc_chckbxExportDataClasses.gridy = 4;
		this.add(this.chckbxExportDataClasses, gbc_chckbxExportDataClasses);

		this.chckbxExportTableClasses = new JCheckBox("Export table classes");
		this.chckbxExportTableClasses.setSelected(true);
		final GridBagConstraints gbc_chckbxExportTableClasses = new GridBagConstraints();
		gbc_chckbxExportTableClasses.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExportTableClasses.gridx = 2;
		gbc_chckbxExportTableClasses.gridy = 4;
		this.add(this.chckbxExportTableClasses, gbc_chckbxExportTableClasses);

		this.chckbxSplitBySchema = new JCheckBox("Split by schema");
		this.chckbxSplitBySchema.setEnabled(false);
		final GridBagConstraints gbc_chckbxSplitBySchema = new GridBagConstraints();
		gbc_chckbxSplitBySchema.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSplitBySchema.gridx = 1;
		gbc_chckbxSplitBySchema.gridy = 5;
		this.add(this.chckbxSplitBySchema, gbc_chckbxSplitBySchema);

		this.chckbxOverwriteFiles = new JCheckBox("Overwrite files");
		this.chckbxOverwriteFiles.setToolTipText("Completely overwrite existing files.");
		final GridBagConstraints gbc_chckbxOverwriteFiles = new GridBagConstraints();
		gbc_chckbxOverwriteFiles.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxOverwriteFiles.gridx = 2;
		gbc_chckbxOverwriteFiles.gridy = 5;
		this.add(this.chckbxOverwriteFiles, gbc_chckbxOverwriteFiles);

		this.chckbxCorrectNaming = new JCheckBox("Correct naming");
		this.chckbxCorrectNaming.setToolTipText("first_example -> FirstExampleData/FirstExampleTable");
		final GridBagConstraints gbc_chckbxCorrectNaming = new GridBagConstraints();
		gbc_chckbxCorrectNaming.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxCorrectNaming.gridx = 1;
		gbc_chckbxCorrectNaming.gridy = 6;
		this.add(this.chckbxCorrectNaming, gbc_chckbxCorrectNaming);

		this.chckbxMergeFiles = new JCheckBox("Merge files");
		this.chckbxMergeFiles.setSelected(true);
		this.chckbxMergeFiles.setToolTipText("Tries to merge the existing file with the new one.");
		final GridBagConstraints gbc_chckbxMergeFiles = new GridBagConstraints();
		gbc_chckbxMergeFiles.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMergeFiles.gridx = 2;
		gbc_chckbxMergeFiles.gridy = 6;
		this.add(this.chckbxMergeFiles, gbc_chckbxMergeFiles);

		this.chckbxUseSpring = new JCheckBox("Use Spring");
		final GridBagConstraints gbc_chckbxUseSpring = new GridBagConstraints();
		gbc_chckbxUseSpring.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseSpring.gridx = 1;
		gbc_chckbxUseSpring.gridy = 7;
		this.add(this.chckbxUseSpring, gbc_chckbxUseSpring);

		this.chckbxKeepSimpleNames = new JCheckBox("Keep simple names");
		final GridBagConstraints gbc_chckbxKeepSimpleNames = new GridBagConstraints();
		gbc_chckbxKeepSimpleNames.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxKeepSimpleNames.gridx = 2;
		gbc_chckbxKeepSimpleNames.gridy = 7;
		this.add(this.chckbxKeepSimpleNames, gbc_chckbxKeepSimpleNames);

		final JLabel lblDatabaseBeanName = new JLabel("Database bean name");
		final GridBagConstraints gbc_lblDatabaseBeanName = new GridBagConstraints();
		gbc_lblDatabaseBeanName.insets = new Insets(0, 0, 5, 5);
		gbc_lblDatabaseBeanName.anchor = GridBagConstraints.EAST;
		gbc_lblDatabaseBeanName.gridx = 0;
		gbc_lblDatabaseBeanName.gridy = 8;
		this.add(lblDatabaseBeanName, gbc_lblDatabaseBeanName);

		this.springDatabaseBean = new JTextField();
		this.springDatabaseBean.setEnabled(false);
		final GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 8;
		this.add(this.springDatabaseBean, gbc_textField);
		this.springDatabaseBean.setColumns(10);

		this.lblDbms = new JLabel("DBMS");
		final GridBagConstraints gbc_lblDbms = new GridBagConstraints();
		gbc_lblDbms.anchor = GridBagConstraints.EAST;
		gbc_lblDbms.insets = new Insets(0, 0, 0, 5);
		gbc_lblDbms.gridx = 0;
		gbc_lblDbms.gridy = 9;
		this.add(this.lblDbms, gbc_lblDbms);

		this.comboBoxDbms = new JComboBox<>();
		this.comboBoxDbms.setEditable(true);
		this.comboBoxDbms.setModel(new DefaultComboBoxModel(new String[] { "MySQL", "SQLite", "PostgreSQL" }));
		final GridBagConstraints gbc_comboBoxDbms = new GridBagConstraints();
		gbc_comboBoxDbms.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxDbms.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxDbms.gridx = 1;
		gbc_comboBoxDbms.gridy = 9;
		this.add(this.comboBoxDbms, gbc_comboBoxDbms);

		this.chckbxUseSpring.addActionListener(a -> this.springDatabaseBean.setEnabled(this.chckbxUseSpring.isSelected()));
		btnSelectDataPath.addActionListener(e -> UiProvider.selectDir(this, this.txtDataPath));
		btnSelectTablePath.addActionListener(e -> UiProvider.selectDir(this, this.txtTablePath));
		this.comboBoxDbms.addActionListener(
				a -> this.chckbxSplitBySchema.setEnabled("PostgreSQL".equals(this.comboBoxDbms.getSelectedItem().toString())));
	}

}
