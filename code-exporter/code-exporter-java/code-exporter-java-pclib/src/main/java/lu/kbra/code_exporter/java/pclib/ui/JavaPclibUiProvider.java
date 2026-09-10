package lu.kbra.code_exporter.java.pclib.ui;

import java.io.File;
import java.nio.file.Paths;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.code_exporter.api.ui.UIProvider;
import lu.kbra.code_exporter.java.pclib.JavaPclibExporterOptions;

public class JavaPclibUiProvider implements UIProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("Java > PCLib");
	}

	@Override
	public JPanel buildUI() {
		return new JavaPclibUiPanel();
	}

	@Override
	public ExporterOptions getOptions(final JPanel panel) {
		if (!(panel instanceof final JavaPclibUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		final JavaPclibExporterOptions options = new JavaPclibExporterOptions();

		options.setDataPackage(optionPanel.getDataClassesPackage().getText());
		options.setTablePackage(optionPanel.getTableClassesPackage().getText());
		options.setSpringDeatabaseBean(optionPanel.getSpringDatabaseBean().getText());
		options.setDbms(optionPanel.getComboBoxDbms().getSelectedItem().toString());

		options.setSplitBySchema("PostgreSQL".equals(options.getDbms()) && optionPanel.getChckbxSplitBySchema().isSelected());
		options.setExportDataClasses(optionPanel.getChckbxExportDataClasses().isSelected());
		options.setExportTableClasses(optionPanel.getChckbxExportTableClasses().isSelected());
		options.setOverwriteFiles(optionPanel.getChckbxOverwriteFiles().isSelected());
		options.setMergeFiles(optionPanel.getChckbxMergeFiles().isSelected());
		options.setUseSpring(optionPanel.getChckbxUseSpring().isSelected());
		options.setFixNamingConvention(optionPanel.getChckbxCorrectNaming().isSelected());
		options.setKeepSimpleNames(optionPanel.getChckbxKeepSimpleNames().isSelected());

		options.setDataPath(Paths.get(optionPanel.getTxtDataPath().getText()));
		options.setTablePath(Paths.get(optionPanel.getTxtTablePath().getText()));

		return options;
	}

	@Override
	public void restoreOptions(
			final JPanel panel,
			final File currentDocumentFile,
			final File currentConfigFile,
			final ExporterOptions options) {
		if (!(panel instanceof final JavaPclibUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		if (!(options instanceof final JavaPclibExporterOptions pclibOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		optionPanel.setCurrentConfigFile(currentConfigFile);
		optionPanel.setCurrentDocumentFile(currentDocumentFile);

		optionPanel.getDataClassesPackage().setText(pclibOptions.getDataPackage());
		optionPanel.getTableClassesPackage().setText(pclibOptions.getTablePackage());
		optionPanel.getSpringDatabaseBean().setText(pclibOptions.getSpringDeatabaseBean());
		optionPanel.getComboBoxDbms().setSelectedItem(pclibOptions.getDbms());

		optionPanel.getChckbxSplitBySchema().setSelected(pclibOptions.isSplitBySchema());
		optionPanel.getChckbxExportDataClasses().setSelected(pclibOptions.isExportDataClasses());
		optionPanel.getChckbxExportTableClasses().setSelected(pclibOptions.isExportTableClasses());
		optionPanel.getChckbxOverwriteFiles().setSelected(pclibOptions.isOverwriteFiles());
		optionPanel.getChckbxMergeFiles().setSelected(pclibOptions.isMergeFiles());
		optionPanel.getChckbxUseSpring().setSelected(pclibOptions.isUseSpring());
		optionPanel.getChckbxKeepSimpleNames().setSelected(pclibOptions.isKeepSimpleNames());

		optionPanel.getTxtDataPath().setText(pclibOptions.getDataPath() == null ? "" : pclibOptions.getDataPath().toString());
		optionPanel.getTxtTablePath().setText(pclibOptions.getTablePath() == null ? "" : pclibOptions.getTablePath().toString());
	}

}
