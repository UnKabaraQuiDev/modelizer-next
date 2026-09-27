package lu.kbra.image_exporter.jpeg;

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
import lu.kbra.model_exporter.api.MaxSize2DOwner;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;
import lu.kbra.modelizer_next.domain.layout.Size2D;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

@Data
@EqualsAndHashCode
public class JpegImageExporterOptions implements ImageExporterOptions, MaxSize2DOwner {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path outputPath = Paths.get(".");

	private Size2D maxSize2D = new Size2D(0, 0);

	private String nameFormat = ImageExporterOptions.DEFAULT_NAME_FORMAT;
	private ViewExportScope scope = ViewExportScope.EVERYTHING;
	private Set<PanelType> panels = EnumSet.allOf(PanelType.class);

	private Optional<Color> backgroundColor = Optional.empty();

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
