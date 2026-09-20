package lu.kbra.image_exporter.svg.ui;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.UiProvider;

public class SvgImageUiProvider implements UiProvider {

	@Override
	public JMenuItem buildMenuItem() {
		return new JMenuItem("SVG");
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
