package lu.kbra.modelizer_next.ui.dialogs;

import lu.kbra.model_exporter.api.ExporterType;
import lu.kbra.model_exporter.api.ModelExporter;
import lu.kbra.modelizer_next.common.ExporterOptionRef;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

public class CodeExportDialog extends ModelExportDialog {

	private static final long serialVersionUID = -7781548246931909377L;

	public CodeExportDialog(final MainFrame mainFrame, final ModelExporter service, final ExporterOptionRef ref) {
		super(mainFrame, service, ref);

		if (service.getExporterType() != ExporterType.CODE) {
			throw new IllegalArgumentException("Exporter: " + service.getExporterId() + " of type " + service.getExporterType() + ".");
		}
	}

}
