package lu.kbra.image_exporter.bmp.ui;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.UiProvider;

public class BmpImageUiProvider implements UiProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("BMP");
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
		return null;
	}

	@Override
	public void restoreOptions(final JPanel panel, final ExporterOptions options) {
	}

}
