package lu.kbra.image_exporter.webp.ui;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.image_exporter.webp.WebpImageExporterOptions;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.UiProvider;

public class WebpImageUiProvider implements UiProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("WEBP");
	}

	@Override
	public boolean hasCustomUI() {
		return true;
	}

	@Override
	public JPanel buildUI() {
		return new WebpImageUiPanel();
	}

	@Override
	public ExporterOptions getOptions(final JPanel panel) {
		if (!(panel instanceof final WebpImageUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		final WebpImageExporterOptions options = new WebpImageExporterOptions();

		options.setCompressionLevel(optionPanel.getCompressionLevel().getValue());

		return options;
	}

	@Override
	public void restoreOptions(final JPanel panel, final ExporterOptions options) {
		if (!(panel instanceof final WebpImageUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		if (!(options instanceof final WebpImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		optionPanel.getCompressionLevel().setValue(pngOptions.getCompressionLevel());
	}

}
