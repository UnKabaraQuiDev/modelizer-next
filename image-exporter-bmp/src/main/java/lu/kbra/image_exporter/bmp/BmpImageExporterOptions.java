package lu.kbra.image_exporter.bmp;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lu.kbra.model_exporter.api.ImageExporterOptions;
import lu.kbra.model_exporter.api.MaxSize2DiOwner;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.domain.layout.Size2Di;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

@Data
@EqualsAndHashCode
public class BmpImageExporterOptions implements ImageExporterOptions, MaxSize2DiOwner {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath = Paths.get(".");

	private Size2Di maxSize2D = new Size2Di(0, 0);

	private String nameFormat = ImageExporterOptions.DEFAULT_NAME_FORMAT;
	private ViewExportScope scope = ViewExportScope.EVERYTHING;
	private Set<PanelType> panels = EnumSet.allOf(PanelType.class);

	private Optional<Color> backgroundColor = Optional.empty();

	@Override
	public String getExporterId() {
		return BmpImageExporter.EXPORTER_ID;
	}

	@Override
	public String getExtension() {
		return "bmp";
	}

	@Override
	public BmpImageExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
