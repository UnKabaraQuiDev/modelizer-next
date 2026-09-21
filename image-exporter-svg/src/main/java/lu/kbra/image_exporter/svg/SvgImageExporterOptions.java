package lu.kbra.image_exporter.svg;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.TransparencyOwner;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SvgImageExporterOptions implements ImageExporterOptions, TransparencyOwner {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath;

	private String nameFormat;
	private ViewExportScope scope;
	private Set<PanelType> panels;

	private Optional<Color> backgroundColor;

	private boolean transparentBackground = true;

	@Override
	public String getExporterId() {
		return SvgImageExporter.EXPORTER_ID;
	}

	@Override
	public String getExtension() {
		return "svg";
	}

	@Override
	public SvgImageExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
