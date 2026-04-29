package lu.kbra.modelizer_next.json;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;

public class LegacyModelizerImporter {

	private record LegacyRef(String tableName, String fieldName) {
	}

	private LegacyModelizerImporter() {
	}

	public static ModelDocument importFile(final File file) throws IOException {
		final JsonNode root = MNMain.OBJECT_MAPPER.readTree(file);
		if (!LegacyModelizerImporter.isLegacyRoot(root)) {
			throw new IOException("Unsupported legacy Modelizer file format.");
		}

		return LegacyModelizerImporter.importRoot(root);
	}

	public static boolean isLegacyFile(final File file) throws IOException {
		if (file == null) {
			return false;
		}

		final String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".mod")) {
			return true;
		}

		return LegacyModelizerImporter.isLegacyRoot(MNMain.OBJECT_MAPPER.readTree(file));
	}

	private static void addClassLayout(final PanelState panelState, final String classId, final double x, final double y) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.CLASS);
		layout.setObjectId(classId);
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(0.0, 0.0));
		panelState.getNodeLayouts().add(layout);
	}

	private static void addCommentLayout(final PanelState panelState, final String commentId, final double x, final double y) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.COMMENT);
		layout.setObjectId(commentId);
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(220.0, 80.0));
		panelState.getNodeLayouts().add(layout);
	}

	private static LinkModel createConceptualLink(final JsonNode linkNode, final Map<String, String> classIdsByName) {
		final JsonNode endpoints = linkNode.path("endpoints");
		if (!endpoints.isArray() || endpoints.size() < 2) {
			return null;
		}

		final LegacyRef fromRef = LegacyModelizerImporter.parseRef(endpoints.get(0).path("dob").asText(""));
		final LegacyRef toRef = LegacyModelizerImporter.parseRef(endpoints.get(1).path("dob").asText(""));
		if (fromRef == null || toRef == null) {
			return null;
		}

		final String fromClassId = classIdsByName.get(fromRef.tableName());
		final String toClassId = classIdsByName.get(toRef.tableName());
		if (fromClassId == null || toClassId == null) {
			return null;
		}

		final LinkModel linkModel = new LinkModel();
		linkModel.setName(LegacyModelizerImporter.readText(linkNode, "name", ""));
//		linkModel.setComment(LegacyModelizerImporter.readText(linkNode, "comment", ""));
		linkModel.setFrom(new LinkEnd(fromClassId, null));
		linkModel.setTo(new LinkEnd(toClassId, null));
		linkModel.setCardinalityFrom(LegacyModelizerImporter.parseCardinality(endpoints.get(0).path("cardinality").asText("")));
		linkModel.setCardinalityTo(LegacyModelizerImporter.parseCardinality(endpoints.get(1).path("cardinality").asText("")));

		if (endpoints.size() > 2) {
			final LegacyRef associationRef = LegacyModelizerImporter.parseRef(endpoints.get(2).path("dob").asText(""));
			if (associationRef != null && associationRef.fieldName() == null) {
				linkModel.setAssociationClassId(classIdsByName.get(associationRef.tableName()));
			}
		}

		return linkModel;
	}

	private static CommentModel createStandaloneComment(final JsonNode commentNode) {
		final String text = LegacyModelizerImporter
				.readText(commentNode, "text", LegacyModelizerImporter.readText(commentNode, "comment", ""));
		if (text.isBlank()) {
			return null;
		}

		final CommentModel commentModel = new CommentModel();
		commentModel.setText(text);
		commentModel.setTextColor(LegacyModelizerImporter.parseColor(commentNode.get("foreground"), Color.BLACK));
		commentModel.setBackgroundColor(LegacyModelizerImporter.parseColor(commentNode.get("background"), new Color(0xFFF7CC)));
		commentModel.setBorderColor(LegacyModelizerImporter.parseColor(commentNode.get("foreground"), Color.BLACK));
		commentModel.getVisibility()
				.set(commentNode.path("conceptual").asBoolean(true),
						commentNode.path("logical").asBoolean(true),
						commentNode.path("physical").asBoolean(true));
		return commentModel;
	}

	private static LinkModel createTechnicalLink(
			final JsonNode linkNode,
			final Map<String, String> classIdsByName,
			final Map<String, String> fieldIdsByQualifiedName) {
		final JsonNode endpoints = linkNode.path("endpoints");
		if (!endpoints.isArray() || endpoints.size() < 2) {
			return null;
		}

		final LegacyRef fromRef = LegacyModelizerImporter.parseRef(endpoints.get(0).path("dob").asText(""));
		final LegacyRef toRef = LegacyModelizerImporter.parseRef(endpoints.get(1).path("dob").asText(""));
		if (fromRef == null || toRef == null || fromRef.fieldName() == null || toRef.fieldName() == null) {
			return null;
		}

		final String fromClassId = classIdsByName.get(fromRef.tableName());
		final String toClassId = classIdsByName.get(toRef.tableName());
		final String fromFieldId = fieldIdsByQualifiedName.get(fromRef.tableName() + "." + fromRef.fieldName());
		final String toFieldId = fieldIdsByQualifiedName.get(toRef.tableName() + "." + toRef.fieldName());
		if (fromClassId == null || toClassId == null || fromFieldId == null || toFieldId == null) {
			return null;
		}

		final LinkModel linkModel = new LinkModel();
		linkModel.setName(LegacyModelizerImporter.readText(linkNode, "name", ""));
//		linkModel.setComment(LegacyModelizerImporter.readText(linkNode, "comment", ""));
		linkModel.setFrom(new LinkEnd(fromClassId, fromFieldId));
		linkModel.setTo(new LinkEnd(toClassId, toFieldId));
		return linkModel;
	}

	private static ModelDocument importRoot(final JsonNode root) {
		final ModelDocument document = new ModelDocument();
		final Map<String, String> classIdsByName = new HashMap<>();
		final Map<String, String> fieldIdsByQualifiedName = new HashMap<>();
		final Set<String> importedConceptualLinks = new HashSet<>();
		final Set<String> importedTechnicalLinks = new HashSet<>();

		for (final JsonNode tableNode : root.path("tables")) {
			final ClassModel classModel = new ClassModel();
			final String className = LegacyModelizerImporter.readText(tableNode, "name", "Unnamed table");
			final String technicalName = LegacyModelizerImporter.readText(tableNode, "secName", className);

			classModel.getNames().setConceptualName(className);
			classModel.getNames().setTechnicalName(technicalName);
			classModel.getVisibility().setConceptual(tableNode.path("conceptual").asBoolean(true));
			classModel.getVisibility().setLogical(tableNode.path("logical").asBoolean(true));
			classModel.getVisibility().setPhysical(tableNode.path("physical").asBoolean(true));
			classModel.getStyle().setTextColor(LegacyModelizerImporter.parseColor(tableNode.get("foreground"), Color.BLACK));
			classModel.getStyle().setBackgroundColor(LegacyModelizerImporter.parseColor(tableNode.get("background"), Color.WHITE));
			classModel.getStyle().setBorderColor(LegacyModelizerImporter.parseColor(tableNode.get("foreground"), Color.BLACK));

			for (final JsonNode fieldNode : tableNode.path("fields")) {
				final FieldModel fieldModel = new FieldModel();
				final String fieldName = LegacyModelizerImporter.readText(fieldNode, "name", "field");
				final String fieldTechnicalName = LegacyModelizerImporter.readText(fieldNode, "secName", fieldName);

				fieldModel.getNames().setConceptualName(fieldName);
				fieldModel.getNames().setTechnicalName(fieldTechnicalName);
				fieldModel.setNotConceptual(fieldNode.path("noConceptual").asBoolean(false));
				fieldModel.setPrimaryKey(fieldNode.path("primary").asBoolean(false));
				fieldModel.setUnique(fieldNode.path("unique").asBoolean(false));
				fieldModel.setNotNull(!fieldNode.path("null").asBoolean(true));
				fieldModel.getStyle().setTextColor(LegacyModelizerImporter.parseColor(fieldNode.get("foreground"), Color.BLACK));
				fieldModel.getStyle().setBackgroundColor(LegacyModelizerImporter.parseColor(fieldNode.get("background"), Color.WHITE));

				classModel.getFields().add(fieldModel);
				LegacyModelizerImporter.putFieldMapping(fieldIdsByQualifiedName,
						className,
						technicalName,
						fieldName,
						fieldTechnicalName,
						fieldModel.getId());
			}

			document.getModel().getClasses().add(classModel);
			LegacyModelizerImporter.putClassMapping(classIdsByName, className, technicalName, classModel.getId());

			LegacyModelizerImporter.addClassLayout(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL),
					classModel.getId(),
					tableNode.path("x0").asDouble(80.0),
					tableNode.path("y0").asDouble(80.0));
			LegacyModelizerImporter.addClassLayout(document.getWorkspace().getPanels().get(PanelType.LOGICAL),
					classModel.getId(),
					tableNode.path("x1").asDouble(tableNode.path("x0").asDouble(80.0)),
					tableNode.path("y1").asDouble(tableNode.path("y0").asDouble(80.0)));
			LegacyModelizerImporter.addClassLayout(document.getWorkspace().getPanels().get(PanelType.PHYSICAL),
					classModel.getId(),
					tableNode.path("x2").asDouble(tableNode.path("x1").asDouble(tableNode.path("x0").asDouble(80.0))),
					tableNode.path("y2").asDouble(tableNode.path("y1").asDouble(tableNode.path("y0").asDouble(80.0))));
		}

		for (final JsonNode tableNode : root.path("tables")) {
			for (final JsonNode linkNode : tableNode.path("links")) {
				final String rawSignature = "conceptual|" + linkNode.toString();
				if (!importedConceptualLinks.add(rawSignature)) {
					continue;
				}

				final LinkModel linkModel = LegacyModelizerImporter.createConceptualLink(linkNode, classIdsByName);
				if (linkModel != null) {
					document.getModel().getConceptualLinks().add(linkModel);
				}
			}

			for (final JsonNode fieldNode : tableNode.path("fields")) {
				for (final JsonNode linkNode : fieldNode.path("links")) {
					final String rawSignature = "technical|" + linkNode.toString();
					if (!importedTechnicalLinks.add(rawSignature)) {
						continue;
					}

					final LinkModel linkModel = LegacyModelizerImporter
							.createTechnicalLink(linkNode, classIdsByName, fieldIdsByQualifiedName);
					if (linkModel != null) {
						linkModel.setCardinalityFrom(null);
						linkModel.setCardinalityTo(null);
						document.getModel().getTechnicalLinks().add(linkModel);
					}
				}
			}
		}

		for (final JsonNode commentNode : root.path("comments")) {
			final CommentModel commentModel = LegacyModelizerImporter.createStandaloneComment(commentNode);
			if (commentModel == null) {
				continue;
			}

			document.getModel().getComments().add(commentModel);
			LegacyModelizerImporter.addCommentLayout(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL),
					commentModel.getId(),
					commentNode.path("x0").asDouble(80.0),
					commentNode.path("y0").asDouble(80.0));
			LegacyModelizerImporter.addCommentLayout(document.getWorkspace().getPanels().get(PanelType.LOGICAL),
					commentModel.getId(),
					commentNode.path("x1").asDouble(commentNode.path("x0").asDouble(80.0)),
					commentNode.path("y1").asDouble(commentNode.path("y0").asDouble(80.0)));
			LegacyModelizerImporter.addCommentLayout(document.getWorkspace().getPanels().get(PanelType.PHYSICAL),
					commentModel.getId(),
					commentNode.path("x2").asDouble(commentNode.path("x1").asDouble(commentNode.path("x0").asDouble(80.0))),
					commentNode.path("y2").asDouble(commentNode.path("y1").asDouble(commentNode.path("y0").asDouble(80.0))));
		}

		return document;
	}

	private static boolean isLegacyRoot(final JsonNode root) {
		return root != null && root.isObject() && root.has("tables") && !root.has("model") && !root.has("workspace");
	}

	private static Cardinality parseCardinality(final String rawCardinality) {
		return switch (rawCardinality == null ? "" : rawCardinality.trim()) {
		case "0..1" -> Cardinality.ZERO_OR_ONE;
		case "1..1" -> Cardinality.ONE;
		case "1..*" -> Cardinality.ONE_OR_MANY;
		case "0..*" -> Cardinality.ZERO_OR_MANY;
		default -> null;
		};
	}

	private static Color parseColor(final JsonNode colorNode, final Color fallback) {
		if (colorNode == null || colorNode.isNull()) {
			return fallback;
		}

		final String value = colorNode.asText("").trim();
		if (value.isEmpty()) {
			return fallback;
		}

		try {
			return Color.decode(value);
		} catch (final NumberFormatException ex) {
			return fallback;
		}
	}

	private static LegacyRef parseRef(final String rawRef) {
		if (rawRef == null || rawRef.isBlank()) {
			return null;
		}

		final String[] parts = rawRef.split("\\.", 2);
		if (parts.length == 1) {
			return new LegacyRef(parts[0], null);
		}
		return new LegacyRef(parts[0], parts[1]);
	}

	private static void putClassMapping(
			final Map<String, String> classIdsByName,
			final String conceptualName,
			final String technicalName,
			final String classId) {
		if (conceptualName != null && !conceptualName.isBlank()) {
			classIdsByName.put(conceptualName, classId);
		}
		if (technicalName != null && !technicalName.isBlank()) {
			classIdsByName.put(technicalName, classId);
		}
	}

	private static void putFieldAlias(
			final Map<String, String> fieldIdsByQualifiedName,
			final String className,
			final String fieldName,
			final String fieldId) {
		if (className == null || className.isBlank() || fieldName == null || fieldName.isBlank()) {
			return;
		}
		fieldIdsByQualifiedName.put(className + "." + fieldName, fieldId);
	}

	private static void putFieldMapping(
			final Map<String, String> fieldIdsByQualifiedName,
			final String conceptualClassName,
			final String technicalClassName,
			final String conceptualFieldName,
			final String technicalFieldName,
			final String fieldId) {
		LegacyModelizerImporter.putFieldAlias(fieldIdsByQualifiedName, conceptualClassName, conceptualFieldName, fieldId);
		LegacyModelizerImporter.putFieldAlias(fieldIdsByQualifiedName, conceptualClassName, technicalFieldName, fieldId);
		LegacyModelizerImporter.putFieldAlias(fieldIdsByQualifiedName, technicalClassName, conceptualFieldName, fieldId);
		LegacyModelizerImporter.putFieldAlias(fieldIdsByQualifiedName, technicalClassName, technicalFieldName, fieldId);
	}

	private static String readText(final JsonNode node, final String fieldName, final String fallback) {
		if (node == null || fieldName == null) {
			return fallback;
		}

		final JsonNode valueNode = node.get(fieldName);
		if (valueNode == null || valueNode.isNull()) {
			return fallback;
		}

		final String value = valueNode.asText();
		return value == null || value.isBlank() ? fallback : value;
	}

}
