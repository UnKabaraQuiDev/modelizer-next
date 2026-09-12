package lu.kbra.code_exporter.java.pclib;

import java.nio.file.Path;

import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.code_exporter.api.ui.OptionsManager;

public class JavaPclibOptionsManager implements OptionsManager {

	@Override
	public ExporterOptions relativizePaths(final ExporterOptions options, final Path path) {
		if (!(options instanceof final JavaPclibExporterOptions pclibOptions)) {
			throw new IllegalArgumentException(
					"Options type not supported (" + (options == null ? "null" : options.getClass().getName()) + ").");
		}

		if (pclibOptions.getDataPath() != null) {
			final Path dataPath = pclibOptions.getDataPath();

			if (dataPath.isAbsolute()) {
				pclibOptions.setDataPath(path.relativize(dataPath));
			}
		}

		if (pclibOptions.getTablePath() != null) {
			final Path tablePath = pclibOptions.getTablePath();

			if (tablePath.isAbsolute()) {
				pclibOptions.setTablePath(path.relativize(tablePath));
			}
		}

		if (pclibOptions.getAttachedFile() != null) {
			final Path attachedFile = pclibOptions.getAttachedFile();

			if (attachedFile.isAbsolute()) {
				pclibOptions.setAttachedFile(path.relativize(attachedFile));
			}
		}

		return options;
	}

	@Override
	public ExporterOptions blankOptions() {
		return new JavaPclibExporterOptions();
	}

	@Override
	public Class<? extends ExporterOptions> getClassType() {
		return JavaPclibExporterOptions.class;
	}

}
