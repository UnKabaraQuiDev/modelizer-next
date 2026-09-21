package lu.kbra.modelizer_next.ui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.ImageOptionsManager;
import lu.kbra.model_exporter.api.MaxDimensionOwner;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.model_exporter.api.TransparencyOwner;
import lu.kbra.modelizer_next.common.ExporterOptionRef;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.ui.component.ImageExportPanel;
import lu.kbra.modelizer_next.ui.component.ImageSizePanel;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

public class ImageExportDialog extends ModelExportDialog {

	private static final long serialVersionUID = -1532485082925603796L;

	private ImageExportPanel imageExportPanel;
	private ImageSizePanel imageSizePanel;

	public ImageExportDialog(final MainFrame mainFrame, final ModelExporter service, final ExporterOptionRef ref) {
		super(mainFrame, service, ref);

		if (service.getExporterType() != ExporterType.IMAGE) {
			throw new IllegalArgumentException("Exporter: " + service.getExporterId() + " of type " + service.getExporterType() + ".");
		}
	}

	@Override
	protected void createOptionPanel() {
		final ImageOptionsManager optionsManager = (ImageOptionsManager) super.service.getOptionsManager();
		super.createOptionPanel();

		this.imageExportPanel = new ImageExportPanel();
		this.imageExportPanel.getChckbxTransparentBackground().setEnabled(optionsManager.supportsTransparency());
		this.imageExportPanel.getViewScope().addActionListener(this::updateSize);

		super.contentPanel = new JPanel();
		super.contentPanel.setLayout(new BoxLayout(super.contentPanel, BoxLayout.Y_AXIS));
		super.contentPanel.add(this.imageExportPanel);
		if (optionsManager.supportsFixedSize()) {
			super.contentPanel.add(Box.createVerticalStrut(5));
			this.imageSizePanel = new ImageSizePanel();
			this.imageSizePanel.getBtnUse().addActionListener(this::useSize);
			super.contentPanel.add(this.imageSizePanel);
		}
		if (super.hasCustomOptions()) {
			super.contentPanel.add(Box.createVerticalStrut(5));
			super.contentPanel.add(super.customOptionsPanel);
		}
	}

	@Override
	protected ExporterOptions parsePanelOptions() {
		final ImageOptionsManager optionsManager = (ImageOptionsManager) super.service.getOptionsManager();
		final ImageExporterOptions options;
		if (this.hasCustomOptions()) {
			options = (ImageExporterOptions) this.service.getUiProvider().getOptions(this.customOptionsPanel);
		} else {
			options = (ImageExporterOptions) optionsManager.blankOptions();
		}

		options.setNameFormat(this.imageExportPanel.getTextFilenamePattern().getText());
		options.setOutputPath(Paths.get(this.imageExportPanel.getTextOutputPath().getText()));
		options.setScope((ViewExportScope) this.imageExportPanel.getViewScope().getSelectedItem());
		options.setBackgroundColor(Optional.ofNullable(this.imageExportPanel.getClrbtnColor().getSelectedColor()));
		if (optionsManager.supportsTransparency()) {
			((TransparencyOwner) options).setTransparentBackground(this.imageExportPanel.getChckbxTransparentBackground().isSelected());
		}

		final Set<PanelType> panels = EnumSet.noneOf(PanelType.class);
		if (this.imageExportPanel.getChckbxConceptual().isSelected()) {
			panels.add(PanelType.CONCEPTUAL);
		}
		if (this.imageExportPanel.getChckbxLogical().isSelected()) {
			panels.add(PanelType.LOGICAL);
		}
		if (this.imageExportPanel.getChckbxPhysical().isSelected()) {
			panels.add(PanelType.PHYSICAL);
		}
		options.setPanels(panels);

		if (optionsManager.supportsFixedSize()) {
			final MaxDimensionOwner own = (MaxDimensionOwner) options;
			own.setMaxDimension(new Dimension((int) this.imageSizePanel.getSpinWidth().getValue(),
					(int) this.imageSizePanel.getSpinHeight().getValue()));
		}

		return options;
	}

	@Override
	protected void restorePanelOption(final ExporterOptions options2) {
		final ImageOptionsManager optionsManager = (ImageOptionsManager) super.service.getOptionsManager();

		if (this.hasCustomOptions()) {
			super.restorePanelOption(options2);
		}

		final ImageExporterOptions options = (ImageExporterOptions) options2;

		this.imageExportPanel.getTextFilenamePattern().setText(options.getNameFormat());
		this.imageExportPanel.getTextOutputPath().setText(options.getOutputPath().toString());
		this.imageExportPanel.getViewScope().setSelectedItem(options.getScope());

		final Set<PanelType> panels = options.getPanels();

		this.imageExportPanel.getChckbxConceptual().setSelected(panels.contains(PanelType.CONCEPTUAL));
		this.imageExportPanel.getChckbxLogical().setSelected(panels.contains(PanelType.LOGICAL));
		this.imageExportPanel.getChckbxPhysical().setSelected(panels.contains(PanelType.PHYSICAL));

		if (optionsManager.supportsFixedSize()) {
			final Dimension maxDimension = ((MaxDimensionOwner) options).getMaxDimension();
			this.imageSizePanel.getSpinWidth().setValue(maxDimension.width);
			this.imageSizePanel.getSpinHeight().setValue(maxDimension.height);
		}

		if (optionsManager.supportsTransparency()) {
			this.imageExportPanel.getChckbxTransparentBackground().setSelected(((TransparencyOwner) options2).isTransparentBackground());
		} else {
			this.imageExportPanel.getChckbxTransparentBackground().setSelected(false);
			this.imageExportPanel.getChckbxTransparentBackground().setEnabled(false);
		}
	}

	private void updateSize(final ActionEvent actionevent1) {
		if (this.imageSizePanel == null) {
			return;
		}

		final Dimension size = this.mainFrame.getActiveCanvas()
				.getExportSize((ViewExportScope) this.imageExportPanel.getViewScope().getSelectedItem());

		this.imageSizePanel.getLblWidth().setText(Integer.toString((int) size.getWidth()));
		this.imageSizePanel.getLblHeight().setText(Integer.toString((int) size.getHeight()));
	}

	private void useSize(final ActionEvent actionevent1) {
		if (this.imageSizePanel == null) {
			return;
		}

		this.imageSizePanel.getSpinWidth().setValue(Integer.parseInt(this.imageSizePanel.getLblWidth().getText()));
		this.imageSizePanel.getSpinHeight().setValue(Integer.parseInt(this.imageSizePanel.getLblHeight().getText()));
	}

}
