package lu.kbra.modelizer_next.ui.dialogs;

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.modelizer_next.data.ExporterOptionRef;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.ui.component.ImageExportPanel;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

public class ImageExportDialog extends ModelExportDialog {

	private static final long serialVersionUID = -1532485082925603796L;

	private ImageExportPanel imagePanel;

	public ImageExportDialog(final MainFrame mainFrame, final ModelExporter service, final ExporterOptionRef ref) {
		super(mainFrame, service, ref);

		if (service.getExporterType() != ExporterType.IMAGE) {
			throw new IllegalArgumentException("Exporter: " + service.getExporterId() + " of type " + service.getExporterType() + ".");
		}
	}

	@Override
	protected void createOptionPanel() {
		super.createOptionPanel();
		this.imagePanel = new ImageExportPanel();
		super.contentPanel = new JPanel();
		super.contentPanel.setLayout(new BoxLayout(super.contentPanel, BoxLayout.Y_AXIS));
		super.contentPanel.add(this.imagePanel);
		if (super.hasCustomOptions()) {
			super.contentPanel.add(super.customOptionsPanel);
		}
	}

	@Override
	protected ExporterOptions parsePanelOptions() {
		final ImageExporterOptions options;
		if (this.hasCustomOptions()) {
			options = (ImageExporterOptions) this.service.getUiProvider().getOptions(this.customOptionsPanel);
		} else {
			options = (ImageExporterOptions) this.service.getOptionsManager().blankOptions();
		}

		options.setNameFormat(this.imagePanel.getTextFilenamePattern().getText());
		options.setOutputPath(Paths.get(this.imagePanel.getTextOutputPath().getText()));
		options.setScope((ViewExportScope) this.imagePanel.getViewScope().getSelectedItem());

		final Set<PanelType> panels = EnumSet.noneOf(PanelType.class);
		if (this.imagePanel.getChckbxConceptual().isSelected()) {
			panels.add(PanelType.CONCEPTUAL);
		}
		if (this.imagePanel.getChckbxLogical().isSelected()) {
			panels.add(PanelType.LOGICAL);
		}
		if (this.imagePanel.getChckbxPhysical().isSelected()) {
			panels.add(PanelType.PHYSICAL);
		}
		options.setPanels(panels);

		return options;
	}

	@Override
	protected void restorePanelOption(final ExporterOptions options2) {
		if (this.hasCustomOptions()) {
			super.restorePanelOption(options2);
		}

		final ImageExporterOptions options = (ImageExporterOptions) options2;

		this.imagePanel.getTextFilenamePattern().setText(options.getNameFormat());
		this.imagePanel.getTextOutputPath().setText(options.getOutputPath().toString());
		this.imagePanel.getViewScope().setSelectedItem(options.getScope());

		final Set<PanelType> panels = options.getPanels();

		this.imagePanel.getChckbxConceptual().setSelected(panels.contains(PanelType.CONCEPTUAL));
		this.imagePanel.getChckbxLogical().setSelected(panels.contains(PanelType.LOGICAL));
		this.imagePanel.getChckbxPhysical().setSelected(panels.contains(PanelType.PHYSICAL));
	}

}
