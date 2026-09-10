package lu.kbra.code_exporter.java.pclib;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lu.kbra.code_exporter.api.ui.ExporterOptions;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;

@Data
@EqualsAndHashCode
public class JavaPclibExporterOptions implements ExporterOptions {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path attachedFile;

	private boolean exportDataClasses;
	private boolean exportTableClasses;
	private boolean splitBySchema = false;

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path tablePath;
	@JsonSerialize(using = RelativePathSerializer.class)
	private Path dataPath;

	private String tablePackage;
	private String dataPackage;
	
	private boolean fixNamingConvention = true;
	private boolean keepSimpleNames = true;
	
	private boolean useSpring = true;
	private String springDeatabaseBean;
	
	private boolean overwriteFiles = false;
	private boolean mergeFiles = false;
	
	private String dbms;

	@Override
	public String getExporterId() {
		return JavaPclibCodeExporter.EXPORTER_ID;
	}

	@Override
	public JavaPclibExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
