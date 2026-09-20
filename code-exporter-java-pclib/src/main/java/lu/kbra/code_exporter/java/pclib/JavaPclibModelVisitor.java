package lu.kbra.code_exporter.java.pclib;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.domain.document.ModelDocument;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.tuple.Pair;
import lu.kbra.pclib.datastructure.tuple.Pairs;

import lombok.Getter;

@Getter
public class JavaPclibModelVisitor implements ModelVisitor {

	private final JavaPclibExporterOptions options;

	public JavaPclibModelVisitor(final JavaPclibExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDocument(final ModelDocument file, final ExportUpdateCallback callback) {
		final DiagramModel model = file.getModel();
		final Map<ClassModel, Pair<File, File>> classes = model.getClasses().stream().collect(Collectors.toMap(Function.identity(), c -> {
			final String name = this.options.isFixNamingConvention() ? PCUtils.constantToCamelCase(c.getTechnicalName())
					: c.getTechnicalName();
			return Pairs.readOnly(new File(this.options.getDataPath().toFile(), name + "Data.java"),
					new File(this.options.getTablePath().toFile(), name + "Table.java"));
		}));

		if (this.options.isOverwriteFiles()) {
			classes.forEach((k, v) -> {
				v.getKey().delete();
				v.getValue().delete();
			});
		}
	}

}
