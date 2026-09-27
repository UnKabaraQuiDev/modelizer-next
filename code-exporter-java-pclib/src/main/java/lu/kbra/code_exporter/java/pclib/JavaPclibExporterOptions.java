package lu.kbra.code_exporter.java.pclib;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lu.kbra.code_exporter.java.common.JavaTypesUiPanel.AnnotationData;
import lu.kbra.code_exporter.java.common.JavaTypesUiPanel.MappingData;
import lu.kbra.model_exporter.api.ExporterOptions;
import lu.kbra.modelizer_next.utils.RelativePathSerializer;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.db.annotations.entry.def.DecimalParam;
import lu.kbra.pclib.db.annotations.entry.def.FixedLength;
import lu.kbra.pclib.db.annotations.entry.def.MaxLength;

@Data
@EqualsAndHashCode
public class JavaPclibExporterOptions implements ExporterOptions {

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path attachedFile;

	private boolean exportDataClasses = true;
	private boolean exportTableClasses = true;
	private boolean splitBySchema = false;

	@JsonSerialize(using = RelativePathSerializer.class)
	private Path tablePath = Paths.get("table");
	@JsonSerialize(using = RelativePathSerializer.class)
	private Path dataPath = Paths.get("data");

	private String tablePackage = "table";
	private String dataPackage = "data";

	private boolean fixNamingConvention = true;
	private boolean keepSimpleNames = true;

	private boolean useLombok = true;
	private boolean useSpring = true;
	private String springDatabaseBean = "";

	private boolean overwriteFiles = false;
	private boolean mergeFiles = false;

	private String dbms = "MySQL";

	private List<MappingData> types = new ArrayList<>();

	public JavaPclibExporterOptions() {
		this.types.add(
				new MappingData("CHAR\\((\\d+)\\)", String.class, new ArrayList<>(List.of(new AnnotationData(FixedLength.class, "($1)")))));
		this.types.add(new MappingData("VARCHAR\\((\\d+)\\)",
				String.class,
				new ArrayList<>(List.of(new AnnotationData(MaxLength.class, "($1)")))));
		this.types.add(new MappingData("TINYINT", Byte.class, new ArrayList<>()));
		this.types.add(new MappingData("SMALLINT", Short.class, new ArrayList<>()));
		this.types.add(new MappingData("INT(?:EGER)?", Integer.class, new ArrayList<>()));
		this.types.add(new MappingData("BIGINT", Long.class, new ArrayList<>()));
		this.types.add(new MappingData("DECIMAL\\((\\d+)\\s*,\\s*(\\d+)\\)",
				BigDecimal.class,
				new ArrayList<>(List.of(new AnnotationData(DecimalParam.class, "(precision = $1, scale = $2)")))));
		this.types.add(new MappingData("DECIMAL", BigDecimal.class, new ArrayList<>()));
		this.types.add(new MappingData("DOUBLE", Double.class, new ArrayList<>()));
		this.types.add(new MappingData("FLOAT", Float.class, new ArrayList<>()));
		this.types.add(new MappingData("BOOLEAN|BOOL", Boolean.class, new ArrayList<>()));
		this.types.add(new MappingData("UUID", UUID.class, new ArrayList<>()));
		this.types.add(new MappingData("BINARY\\((\\d+)\\)",
				byte[].class,
				new ArrayList<>(List.of(new AnnotationData(FixedLength.class, "($1)")))));
		this.types.add(new MappingData("VARBINARY\\((\\d+)\\)",
				byte[].class,
				new ArrayList<>(List.of(new AnnotationData(MaxLength.class, "($1)")))));
		this.types.add(new MappingData("BLOB", byte[].class, new ArrayList<>()));
		this.types.add(new MappingData("TIMESTAMP", Timestamp.class, new ArrayList<>()));
		this.types.add(new MappingData("INSTANT", Instant.class, new ArrayList<>()));
		this.types.add(new MappingData("TIME\\s+WITH\\s+TIME\\s+ZONE", OffsetTime.class, new ArrayList<>()));
		this.types.add(new MappingData("TIMESTAMP\\s+WITH\\s+TIME\\s+ZONE", OffsetDateTime.class, new ArrayList<>()));
		this.types.add(new MappingData("DATETIME\\s+WITH\\s+TIME\\s+ZONE", ZonedDateTime.class, new ArrayList<>()));
		this.types.add(new MappingData("DATE", java.sql.Date.class, new ArrayList<>()));
		this.types.add(new MappingData("TIME", java.sql.Time.class, new ArrayList<>()));
		this.types.add(new MappingData("DATETIME", java.util.Date.class, new ArrayList<>()));
		this.types.add(new MappingData("PERIOD", Period.class, new ArrayList<>()));
		this.types.add(new MappingData("INTERVAL", Duration.class, new ArrayList<>()));
		this.types.add(new MappingData("LOCALDATETIME", LocalDateTime.class, new ArrayList<>()));
		this.types.add(new MappingData("LOCALDATE", LocalDate.class, new ArrayList<>()));
		this.types.add(new MappingData("LOCALTIME", LocalTime.class, new ArrayList<>()));
		this.types.add(new MappingData("MONTHDAY", MonthDay.class, new ArrayList<>()));
		this.types.add(new MappingData("YEAR", Year.class, new ArrayList<>()));
		this.types.add(new MappingData("YEARMONTH", YearMonth.class, new ArrayList<>()));
		this.types.add(new MappingData("JSON", JSONObject.class, new ArrayList<>()));
		this.types.add(new MappingData("JSON_ARRAY", JSONArray.class, new ArrayList<>()));
		this.types.add(new MappingData("TEXT", String.class, new ArrayList<>()));
	}

	@Override
	public String getExporterId() {
		return JavaPclibCodeExporter.EXPORTER_ID;
	}

	@Override
	public JavaPclibExporterOptions clone() {
		return PCUtils.safeClone(super::clone);
	}

}
