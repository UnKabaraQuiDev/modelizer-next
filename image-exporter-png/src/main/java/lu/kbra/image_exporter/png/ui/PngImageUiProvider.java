package lu.kbra.image_exporter.png.ui;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.image_exporter.png.PngImageExporterOptions;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.UiProvider;

public class PngImageUiProvider implements UiProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("PNG");
	}

	@Override
	public boolean hasCustomUI() {
		return true;
	}

	@Override
	public JPanel buildUI() {
		return new PngImageUiPanel();
	}

	@Override
	public ExporterOptions getOptions(final JPanel panel) {
		if (!(panel instanceof final PngImageUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		final PngImageExporterOptions options = new PngImageExporterOptions();

		options.setCompressionLevel(optionPanel.getCompressionLevel().getValue());

		return options;
	}

	@Override
	public void restoreOptions(final JPanel panel, final ExporterOptions options) {
		if (!(panel instanceof final PngImageUiPanel optionPanel)) {
			throw new IllegalArgumentException("Panel type not supported (" + (panel == null ? "null" : panel.getClass().getName()) + ").");
		}

		if (!(options instanceof final PngImageExporterOptions pngOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		optionPanel.getCompressionLevel().setValue(pngOptions.getCompressionLevel());
	}

}
