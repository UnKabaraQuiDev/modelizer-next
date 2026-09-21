package lu.kbra.image_exporter.jpeg;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.MaxDimensionOwner;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class JpegImageExporterOptions implements ImageExporterOptions, MaxDimensionOwner {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath;

	private Dimension maxDimension;

	private String nameFormat;
	private ViewExportScope scope;
	private Set<PanelType> panels;

	private Optional<Color> backgroundColor;

	@Override
	public String getExporterId() {
		return JpegImageExporter.EXPORTER_ID;
	}

	@Override
	public String getExtension() {
		return "jpg";
	}

	@Override
	public JpegImageExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
