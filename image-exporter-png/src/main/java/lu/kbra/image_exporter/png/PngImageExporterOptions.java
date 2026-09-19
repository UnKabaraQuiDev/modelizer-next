package lu.kbra.image_exporter.png;

import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

@Data
@EqualsAndHashCode
public class PngImageExporterOptions implements ImageExporterOptions {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath;

	private String nameFormat;
	private ViewExportScope scope;
	private Set<PanelType> panels;

	@Override
	public String getExporterId() {
		return PngImageExporter.EXPORTER_ID;
	}

	@Override
	public String getExtension() {
		return "png";
	}

	@Override
	public PngImageExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
