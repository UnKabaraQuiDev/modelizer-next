package lu.kbra.code_exporter.java.pclib;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lu.kbra.code_exporter.java.common.JavaTypesUiPanel;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ModelVisitResult;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.document.ModelDocument;
import lu.kbra.modelizer_next.domain.shared.ElementNames;
import lu.kbra.pclib.PCUtils;
import lu.kbra.pclib.datastructure.tuple.Pair;
import lu.kbra.pclib.datastructure.tuple.Pairs;
import lu.kbra.pclib.db.annotations.entry.Column;
import lu.kbra.pclib.db.annotations.entry.DefaultValue;
import lu.kbra.pclib.db.annotations.entry.ForeignKey;
import lu.kbra.pclib.db.annotations.entry.Nullable;
import lu.kbra.pclib.db.annotations.entry.PrimaryKey;
import lu.kbra.pclib.db.annotations.entry.Unique;
import lu.kbra.pclib.db.annotations.queryable.def.DefinedName;
import lu.kbra.pclib.db.base.Database;
import lu.kbra.pclib.db.impl.DatabaseEntry;
import lu.kbra.pclib.db.table.DatabaseTable;
import lu.kbra.pclib.db.table.DeferredDatabaseTable;

@Getter
public class JavaPclibModelVisitor implements ModelVisitor {

	private final JavaPclibExporterOptions options;

	public JavaPclibModelVisitor(final JavaPclibExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public ModelVisitResult visitDocument(final ModelDocument file, final ExportUpdateCallback callback) {
		final DiagramModel model = file.getModel();

		final Map<ClassModel, Pair<File, File>> classes = model.getClasses().stream().collect(Collectors.toMap(Function.identity(), c -> {
			final String name = this.options.isFixNamingConvention() ? PCUtils.constantToUpperCamelCase(c.getTechnicalName())
					: c.getTechnicalName();

			return Pairs.readOnly(ModelVisitor.getPath(this.options.getDataPath(), name + "Data", "java", null).toAbsolutePath().toFile(),
					ModelVisitor.getPath(this.options.getTablePath(), name + "Table", "java", null).toAbsolutePath().toFile());
		}));

		if (this.options.isOverwriteFiles()) {
			classes.forEach((k, v) -> {
				v.getKey().delete();
				v.getValue().delete();
			});
		}

		/*
		 * Generate the classes.
		 */
		for (final ClassModel classModel : model.getClasses()) {
			final Pair<File, File> files = classes.get(classModel);

			final String dataClassName = PCUtils.removeFileExtension(files.getKey().getName());
			final String tableClassName = PCUtils.removeFileExtension(files.getValue().getName());

			/*
			 * Data class
			 */

			final TypeSpec.Builder dataClass = TypeSpec.classBuilder(dataClassName)
					.addModifiers(Modifier.PUBLIC)
					.addSuperinterface(ClassName.get(DatabaseEntry.class));
			int uniqueIndex = 0;

			if (this.options.isUseLombok()) {
				dataClass.addAnnotation(Data.class);
				dataClass.addAnnotation(NoArgsConstructor.class);
				dataClass.addAnnotation(AllArgsConstructor.class);
			}

			for (final FieldModel field : classModel.getFields()) {
				final String fieldName = this.resolveFieldName(field);
				final ResolvedFieldType fieldType = this.resolveFieldType(field);

				final FieldSpec.Builder fieldSpec = FieldSpec.builder(fieldType.type(), fieldName).addModifiers(Modifier.PRIVATE);
				fieldType.annotations().forEach(fieldSpec::addAnnotation);

				fieldSpec.addAnnotation(ClassName.get(Column.class));

				if (field.isPrimaryKey()) {
					fieldSpec.addAnnotation(ClassName.get(PrimaryKey.class));
				}

				if (!field.isNonNull()) {
					fieldSpec.addAnnotation(ClassName.get(Nullable.class));
				}

				if (field.isUnique()) {
					fieldSpec.addAnnotation(
							AnnotationSpec.builder(ClassName.get(Unique.class)).addMember("index", "$L", uniqueIndex).build());

					uniqueIndex++;
				}

				if (field.getDefaultValue() != null && !field.getDefaultValue().isBlank()) {
					fieldSpec.addAnnotation(AnnotationSpec.builder(ClassName.get(DefaultValue.class))
							.addMember("value", "$L", field.getDefaultValue())
							.build());
				}

				final ClassModel foreignKeyTarget = this.resolveForeignKeyTarget(field, model);

				if (foreignKeyTarget != null) {
					final String targetName = PCUtils.removeFileExtension(classes.get(foreignKeyTarget).getKey().getName());

					fieldSpec.addAnnotation(AnnotationSpec.builder(ClassName.get(ForeignKey.class))
							.addMember("table", "$T.class", ClassName.get(this.options.getTablePackage(), targetName))
							.build());
				}

				dataClass.addField(fieldSpec.build());
			}

			this.addDataConstructors(dataClass, classModel);

			final JavaFile dataJavaFile = JavaFile.builder(this.options.getDataPackage(), dataClass.build()).build();

			this.writeJavaFile(dataJavaFile, files.getKey());

			/*
			 * Table class
			 */

			final ClassName dataClassType = ClassName.get(this.options.getDataPackage(), dataClassName);

			final TypeSpec.Builder builder = TypeSpec.classBuilder(tableClassName).addModifiers(Modifier.PUBLIC);
			final ClassName tableBase;

			if (this.options.isUseSpring()) {
				tableBase = ClassName.get(DeferredDatabaseTable.class);
				builder.addAnnotation(Component.class);
			} else {
				tableBase = ClassName.get(DatabaseTable.class);
			}
			if (this.options.isKeepSimpleNames()) {
				builder.addAnnotation(
						AnnotationSpec.builder(DefinedName.class).addMember("value", "$S", classModel.getTechnicalName()).build());
			}

			final ParameterSpec.Builder databaseParameter = ParameterSpec.builder(Database.class, "database");

			if (this.options.isUseSpring() && this.options.getSpringDatabaseBean() != null
					&& !this.options.getSpringDatabaseBean().isBlank()) {
				databaseParameter.addAnnotation(
						AnnotationSpec.builder(Qualifier.class).addMember("value", "$S", this.options.getSpringDatabaseBean()).build());
			}

			final MethodSpec constructor = MethodSpec.constructorBuilder()
					.addModifiers(Modifier.PUBLIC)
					.addParameter(databaseParameter.build())
					.addStatement("super(database)")
					.build();

			builder.addMethod(constructor);

			final TypeSpec tableClass = builder.superclass(ParameterizedTypeName.get(tableBase, dataClassType)).build();

			final JavaFile tableJavaFile = JavaFile.builder(this.options.getTablePackage(), tableClass).build();

			this.writeJavaFile(tableJavaFile, files.getValue());
		}

		return new ModelVisitResult(classes.values()
				.stream()
				.<Path>flatMap(c -> Arrays.asList(c.getValue().toPath(), c.getValue().toPath()).stream())
				.toList());
	}

	private void addDataConstructors(final TypeSpec.Builder dataClass, final ClassModel classModel) {

		final List<FieldModel> fields = classModel.getFields();

		final List<List<FieldModel>> constructorFields = new ArrayList<>();

		/*
		 * 1. All primary keys
		 */
		final List<FieldModel> primaryKeys = fields.stream().filter(FieldModel::isPrimaryKey).toList();

		if (!primaryKeys.isEmpty()) {
			constructorFields.add(primaryKeys);
		}

		/*
		 * 2. Each unique group.
		 *
		 * Group by the Unique index first.
		 */
		final Map<Integer, List<FieldModel>> uniqueGroups = new LinkedHashMap<>();

		for (final FieldModel field : fields) {
			if (!field.isUnique()) {
				continue;
			}

			// TODO: implement #46
			uniqueGroups.computeIfAbsent(0, ignored -> new ArrayList<>()).add(field);
		}

		constructorFields.addAll(uniqueGroups.values());

		/*
		 * 3. All fields that are individually unique.
		 *
		 * Only add this if this is actually intended to be different from the unique groups.
		 */
		final List<FieldModel> uniqueFields = fields.stream().filter(FieldModel::isUnique).toList();

		if (!uniqueFields.isEmpty()) {
			constructorFields.add(uniqueFields);
		}

		/*
		 * 4. All explicitly supplied fields.
		 */
		final List<FieldModel> requiredFields = fields.stream().filter(this::requiresExplicitValue).toList();

		if (!requiredFields.isEmpty()) {
			constructorFields.add(requiredFields);
		}

		/*
		 * Remove duplicate constructor signatures.
		 */
		final Set<List<String>> seen = new HashSet<>();

		for (final List<FieldModel> constructor : constructorFields) {
			final List<String> signature = constructor.stream().map(FieldModel::getId).toList();

			if (!seen.add(signature)) {
				continue;
			}

			dataClass.addMethod(createConstructor(constructor));
		}
	}

	private boolean requiresExplicitValue(final FieldModel field) {
		if (field.isPrimaryKey() && field.isAutoIncrement()) {
			return false;
		}

		final String defaultValue = field.getDefaultValue();

		return !(defaultValue != null && !defaultValue.isBlank() || field.isGenerated());
	}

	private MethodSpec createConstructor(final List<FieldModel> fields) {
		final MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

		for (final FieldModel field : fields) {
			final String fieldName = this.resolveFieldName(field);
			final ResolvedFieldType resolvedType = this.resolveFieldType(field);

			constructor.addParameter(ParameterSpec.builder(resolvedType.type(), fieldName).build());
		}

		for (final FieldModel field : fields) {
			final String fieldName = this.resolveFieldName(field);

			constructor.addStatement("this.$L = $L", fieldName, fieldName);
		}

		return constructor.build();
	}

	private record ResolvedFieldType(TypeName type, List<AnnotationSpec> annotations) {
	}

	private ResolvedFieldType resolveFieldType(final FieldModel field) {
		final String fieldType = field.getType();

		for (final JavaTypesUiPanel.MappingData mapping : this.options.getTypes()) {
			final Pattern pattern = Pattern.compile(mapping.regex());
			final Matcher matcher = pattern.matcher(fieldType);

			if (!matcher.matches()) {
				continue;
			}

			final TypeName type = ClassName.bestGuess(mapping.className());

			final List<AnnotationSpec> annotations = mapping.annotations()
					.stream()
					.map(annotation -> this.createAnnotation(annotation, matcher))
					.toList();

			return new ResolvedFieldType(type, annotations);
		}

		return new ResolvedFieldType(TypeName.OBJECT, List.of());
	}

	private AnnotationSpec createAnnotation(final JavaTypesUiPanel.AnnotationData annotation, final Matcher matcher) {
		String parameters = annotation.parameters();

		for (int i = 1; i <= matcher.groupCount(); i++) {
			final String group = matcher.group(i);

			parameters = parameters.replace("$" + i, group != null ? group : "");
		}

		final ClassName annotationClass = ClassName.bestGuess(annotation.className());

		if (parameters.isBlank()) {
			return AnnotationSpec.builder(annotationClass).build();
		}

		return AnnotationSpec.builder(annotationClass).addMember("value", "$L", parameters).build();
	}

	private String resolveFieldName(final FieldModel field) {
		final ElementNames names = field.getNames();

		if (names == null) {
			throw new NullPointerException("Invalid field: " + field + " has no name.");
		}

		if (this.options.isFixNamingConvention()) {
			return PCUtils.constantToLowerCamelCase(names.getTechnicalName());
		}

		return names.getTechnicalName();
	}

	private ClassModel resolveForeignKeyTarget(final FieldModel field, final DiagramModel model) {

		if (field.getId() == null || field.getId().isBlank()) {
			return null;
		}

		final String fieldId = field.getId();

		for (final LinkModel link : model.getTechnicalLinks()) {
			final LinkEnd from = link.getFrom();
			final LinkEnd to = link.getTo();

			if (from != null && fieldId.equals(from.getFieldId())) {
				if (to == null || to.getClassId() == null) {
					return null;
				}

				return model.validateClassByIdIndex().get(to.getClassId());
			}

			if (to != null && fieldId.equals(to.getFieldId())) {
				if (from == null || from.getClassId() == null) {
					return null;
				}

				return model.validateClassByIdIndex().get(from.getClassId());
			}
		}

		return null;
	}

	private void writeJavaFile(final JavaFile javaFile, final File file) {
		try {
			final File parent = file.getParentFile();

			if (parent != null) {
				parent.mkdirs();
			}

			Files.writeString(file.toPath(), javaFile.toString(), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to generate " + file, e);
		}
	}

}
