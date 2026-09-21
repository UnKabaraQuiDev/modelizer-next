package lu.kbra.image_exporter.webp;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.MaxDimensionOwner;
import lu.kbra.model_exporter.api.TransparencyOwner;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

@Data
@EqualsAndHashCode
public class WebpImageExporterOptions implements ImageExporterOptions, TransparencyOwner, MaxDimensionOwner {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath;

	private int compressionLevel;

	private Dimension maxDimension;

	private String nameFormat;
	private ViewExportScope scope;
	private Set<PanelType> panels;

	private Optional<Color> backgroundColor;

	private boolean transparentBackground = true;

	@Override
	public String getExporterId() {
		return WebpImageExporter.EXPORTER_ID;
	}

	@Override
	public String getExtension() {
		return "webp";
	}

	@Override
	public WebpImageExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
