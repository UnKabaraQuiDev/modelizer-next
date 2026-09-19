package lu.kbra.image_exporter.png.ui;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.UiProvider;

public class PngImageUiProvider implements UiProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("PNG");
	}

	@Override
	public boolean hasCustomUI() {
		return false;
	}

	@Override
	public JPanel buildUI() {
		return null;
	}

	@Override
	public ExporterOptions getOptions(final JPanel panel) {
		throw new IllegalArgumentException("No custom options defined.");
	}

	@Override
	public void restoreOptions(final JPanel panel, final ExporterOptions options) {
		throw new IllegalArgumentException("No custom options defined.");
	}

}
