package lu.kbra.code_exporter.java.pclib;

import java.nio.file.Path;

import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.model_exporter.api.OptionsManager;

public class JavaPclibOptionsManager implements OptionsManager {

	@Override
	public void relativizePaths(final ExporterOptions options, final Path path) {
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
