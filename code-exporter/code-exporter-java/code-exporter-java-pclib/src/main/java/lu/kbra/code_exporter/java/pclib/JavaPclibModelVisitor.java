package lu.kbra.code_exporter.java.pclib;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lu.kbra.code_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.tuple.Pair;
import lu.kbra.pclib.datastructure.tuple.Pairs;

@Getter
public class JavaPclibModelVisitor implements ModelVisitor {

	private final JavaPclibExporterOptions options;

	public JavaPclibModelVisitor(JavaPclibExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDiagram(DiagramModel file) {
		final Map<ClassModel, Pair<File, File>> classes = file.getClasses().stream().collect(Collectors.toMap(Function.identity(), c -> {
			final String name = options.isFixNamingConvention() ? PCUtils.constantToCamelCase(c.getTechnicalName()) : c.getTechnicalName();
			return Pairs.readOnly(new File(options.getDataPath().toFile(), name + "Data.java"),
					new File(options.getTablePath().toFile(), name + "Table.java"));
		}));

		if (options.isOverwriteFiles()) {
			classes.forEach((k, v) -> {
				v.getKey().delete();
				v.getValue().delete();
			});
		}
	}

}
