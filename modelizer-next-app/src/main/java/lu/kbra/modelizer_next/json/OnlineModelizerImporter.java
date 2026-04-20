package lu.kbra.modelizer_next.json;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.Cardinality;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.PanelType;

public final class OnlineModelizerImporter {

	private static void addNodeLayouts(final ModelDocument document, final JsonNode node, final String objectId, final boolean comment) {
		final JsonNode dataNode = node.path("data");
		final JsonNode viewPositions = dataNode.path("viewPositions");
		final JsonNode basePosition = node.path("position");
		final double width = node.path("width").asDouble(comment ? 220.0 : 180.0);
		final double height = node.path("height").asDouble(comment ? 80.0 : 0.0);

		OnlineModelizerImporter.addPanelLayout(document,
				PanelType.CONCEPTUAL,
				objectId,
				comment,
				viewPositions.path("conceptual"),
				basePosition,
				width,
				height);
		OnlineModelizerImporter.addPanelLayout(document,
				PanelType.LOGICAL,
				objectId,
				comment,
				viewPositions.path("logical"),
				viewPositions.path("conceptual").isMissingNode() ? basePosition : viewPositions.path("conceptual"),
				width,
				height);
		OnlineModelizerImporter.addPanelLayout(document,
				PanelType.PHYSICAL,
				objectId,
				comment,
				viewPositions.path("physical"),
				viewPositions.path("logical").isMissingNode()
						? viewPositions.path("conceptual").isMissingNode() ? basePosition : viewPositions.path("conceptual")
						: viewPositions.path("logical"),
				width,
				height);
	}

	private static void addPanelLayout(
			final ModelDocument document,
			final PanelType panelType,
			final String objectId,
			final boolean comment,
			final JsonNode preferredPosition,
			final JsonNode fallbackPosition,
			final double width,
			final double height) {
		final double x = preferredPosition.path("x").isNumber() ? preferredPosition.path("x").asDouble(80.0)
				: fallbackPosition.path("x").asDouble(80.0);
		final double y = preferredPosition.path("y").isNumber() ? preferredPosition.path("y").asDouble(80.0)
				: fallbackPosition.path("y").asDouble(80.0);

		if (comment) {
			ImportJsonSupport.addCommentLayout(document.getWorkspace().getPanels().get(panelType), objectId, x, y, width, height);
		} else {
			ImportJsonSupport.addClassLayout(document.getWorkspace().getPanels().get(panelType), objectId, x, y, width, height);
		}
	}

	private static CommentModel createComment(final JsonNode node) {
		final JsonNode dataNode = node.path("data");
		final String text = ImportJsonSupport.readText(dataNode, "text", ImportJsonSupport.readText(dataNode, "label", ""));
		if (text.isBlank()) {
			return null;
		}

		final CommentModel commentModel = new CommentModel();
		commentModel.setText(text);
		commentModel.setTextColor(Color.BLACK);
		commentModel.setBackgroundColor(ImportJsonSupport.parseColor(dataNode.get("color"), new Color(0xFFF7CC)));
		commentModel.setBorderColor(Color.BLACK);
		commentModel.setVisibleInConceptual(dataNode.path("visibility").path("conceptual").asBoolean(true));
		commentModel.setVisibleInLogical(dataNode.path("visibility").path("logical").asBoolean(true));
		commentModel.setVisibleInPhysical(dataNode.path("visibility").path("physical").asBoolean(true));
		return commentModel;
	}

	private static LinkModel createLink(
			final JsonNode edgeNode,
			final Map<String, String> classIdsBySourceId,
			final Map<String, String> fieldIdsBySourceId,
			final Map<String, String> classIdsByName,
			final Map<String, String> fieldIdsByQualifiedName) {
		final String rawSource = edgeNode.path("source").asText("");
		final String rawTarget = edgeNode.path("target").asText("");
		if (rawSource.isBlank() || rawTarget.isBlank()) {
			return null;
		}

		final String sourceClassId = OnlineModelizerImporter.resolveClassId(rawSource, classIdsBySourceId, classIdsByName);
		final String targetClassId = OnlineModelizerImporter.resolveClassId(rawTarget, classIdsBySourceId, classIdsByName);
		if (sourceClassId == null || targetClassId == null) {
			return null;
		}

		final String rawSourceHandle = edgeNode.path("sourceHandle").asText("");
		final String rawTargetHandle = edgeNode.path("targetHandle").asText("");
		final String sourceFieldId = OnlineModelizerImporter.resolveFieldId(rawSourceHandle, fieldIdsBySourceId, fieldIdsByQualifiedName);
		final String targetFieldId = OnlineModelizerImporter.resolveFieldId(rawTargetHandle, fieldIdsBySourceId, fieldIdsByQualifiedName);

		final JsonNode dataNode = edgeNode.path("data");
		final LinkModel linkModel = new LinkModel();
		linkModel.setName(OnlineModelizerImporter.readEdgeText(edgeNode, dataNode, "label", "name", ""));
		linkModel.setComment(OnlineModelizerImporter.readEdgeText(dataNode, dataNode, "comment", "description", ""));
		linkModel.setFrom(new LinkEnd(sourceClassId, sourceFieldId));
		linkModel.setTo(new LinkEnd(targetClassId, targetFieldId));
		linkModel.setCardinalityFrom(OnlineModelizerImporter
				.parseCardinality(OnlineModelizerImporter.readEdgeText(dataNode, dataNode, "cardinalityFrom", "sourceCardinality", "")));
		linkModel.setCardinalityTo(OnlineModelizerImporter
				.parseCardinality(OnlineModelizerImporter.readEdgeText(dataNode, dataNode, "cardinalityTo", "targetCardinality", "")));
		linkModel.setLineColor(ImportJsonSupport.parseColor(dataNode.get("color"),
				ImportJsonSupport.parseColor(edgeNode.path("style").get("stroke"), Color.BLACK)));

		final String associationRaw = OnlineModelizerImporter
				.readEdgeText(dataNode, dataNode, "associationClassId", "associationNodeId", "");
		if (!associationRaw.isBlank()) {
			linkModel.setAssociationClassId(OnlineModelizerImporter.resolveClassId(associationRaw, classIdsBySourceId, classIdsByName));
		}

		if (sourceFieldId == null && targetFieldId == null) {
			linkModel.setCardinalityFrom(linkModel.getCardinalityFrom());
			linkModel.setCardinalityTo(linkModel.getCardinalityTo());
		}

		return linkModel;
	}

	public static ModelDocument importFile(final File file) throws IOException {
		final JsonNode root = MNMain.OBJECT_MAPPER.readTree(file);
		if (!OnlineModelizerImporter.isOnlineRoot(root)) {
			throw new IOException("Unsupported Online Modelizer file format.");
		}

		return OnlineModelizerImporter.importRoot(root);
	}

	private static ModelDocument importRoot(final JsonNode root) {
		final ModelDocument document = new ModelDocument();
		final Map<String, String> classIdsBySourceId = new HashMap<>();
		final Map<String, String> fieldIdsBySourceId = new HashMap<>();
		final Map<String, String> classIdsByName = new HashMap<>();
		final Map<String, String> fieldIdsByQualifiedName = new HashMap<>();

		for (final JsonNode node : root.path("nodes")) {
			final String nodeType = node.path("type").asText("class");
			if ("comment".equalsIgnoreCase(nodeType) || "note".equalsIgnoreCase(nodeType)) {
				final CommentModel commentModel = OnlineModelizerImporter.createComment(node);
				if (commentModel == null) {
					continue;
				}

				document.getModel().getComments().add(commentModel);
				OnlineModelizerImporter.addNodeLayouts(document, node, commentModel.getId(), true);
				continue;
			}

			final ClassModel classModel = new ClassModel();
			final JsonNode dataNode = node.path("data");
			final String conceptualName = ImportJsonSupport.readText(dataNode, "label", "Unnamed class");
			final String technicalName = ImportJsonSupport.readText(dataNode, "logicalName", conceptualName);

			classModel.getNames().setConceptualName(conceptualName);
			classModel.getNames().setTechnicalName(technicalName);
			classModel.setComment(ImportJsonSupport.readText(dataNode, "comment", ""));
			classModel.setGroup(ImportJsonSupport.readText(dataNode, "group", ""));
			classModel.getVisibility().setConceptual(dataNode.path("visibility").path("conceptual").asBoolean(true));
			classModel.getVisibility().setLogical(dataNode.path("visibility").path("logical").asBoolean(true));
			classModel.getVisibility().setPhysical(dataNode.path("visibility").path("physical").asBoolean(true));
			classModel.getStyle().setTextColor(Color.BLACK);
			classModel.getStyle().setBackgroundColor(ImportJsonSupport.parseColor(dataNode.get("color"), Color.WHITE));
			classModel.getStyle().setBorderColor(Color.BLACK);

			for (final JsonNode attributeNode : dataNode.path("attributes")) {
				final FieldModel fieldModel = new FieldModel();
				final String fieldName = ImportJsonSupport.readText(attributeNode, "name", "field");
				final String logicalName = ImportJsonSupport.readText(attributeNode, "logicalName", fieldName);

				fieldModel.getNames().setName(fieldName);
				fieldModel.getNames().setTechnicalName(logicalName);
				fieldModel.setComment(ImportJsonSupport.readText(attributeNode, "comment", ""));
				fieldModel.setNotConceptual(!attributeNode.path("visibility").path("conceptual").asBoolean(true));
				fieldModel.setPrimaryKey(attributeNode.path("primaryKey").asBoolean(false));
				fieldModel.setUnique(attributeNode.path("unique").asBoolean(false));
				fieldModel.setNotNull(!attributeNode.path("nullable").asBoolean(true));
				fieldModel.getStyle().setTextColor(Color.BLACK);
				fieldModel.getStyle().setBackgroundColor(Color.WHITE);

				classModel.getFields().add(fieldModel);

				final String sourceAttributeId = attributeNode.path("id").asText("");
				if (!sourceAttributeId.isBlank()) {
					fieldIdsBySourceId.put(sourceAttributeId, fieldModel.getId());
				}
				ImportJsonSupport.putFieldMapping(fieldIdsByQualifiedName,
						conceptualName,
						technicalName,
						fieldName,
						logicalName,
						fieldModel.getId());
			}

			document.getModel().getClasses().add(classModel);
			final String sourceNodeId = node.path("id").asText("");
			if (!sourceNodeId.isBlank()) {
				classIdsBySourceId.put(sourceNodeId, classModel.getId());
			}
			ImportJsonSupport.putClassMapping(classIdsByName, conceptualName, technicalName, classModel.getId());
			OnlineModelizerImporter.addNodeLayouts(document, node, classModel.getId(), false);
		}

		for (final JsonNode edgeNode : root.path("edges")) {
			final LinkModel linkModel = OnlineModelizerImporter
					.createLink(edgeNode, classIdsBySourceId, fieldIdsBySourceId, classIdsByName, fieldIdsByQualifiedName);
			if (linkModel == null) {
				continue;
			}

			if (linkModel.getFrom().getFieldId() != null || linkModel.getTo().getFieldId() != null) {
				linkModel.setCardinalityFrom(null);
				linkModel.setCardinalityTo(null);
				document.getModel().getTechnicalLinks().add(linkModel);
			} else {
				document.getModel().getConceptualLinks().add(linkModel);
			}
		}

		return document;
	}

	private static boolean isOnlineRoot(final JsonNode root) {
		return root != null && root.isObject() && root.has("nodes") && root.has("edges") && root.has("modelName") && !root.has("model")
				&& !root.has("workspace");
	}

	private static Cardinality parseCardinality(final String rawCardinality) {
		return switch (rawCardinality == null ? "" : rawCardinality.trim()) {
		case "0..1" -> Cardinality.ZERO_OR_ONE;
		case "1..1", "1" -> Cardinality.ONE;
		case "1..*" -> Cardinality.ONE_OR_MANY;
		case "0..*", "*" -> Cardinality.ZERO_OR_MANY;
		default -> null;
		};
	}

	private static String readEdgeText(
			final JsonNode primaryNode,
			final JsonNode secondaryNode,
			final String primaryField,
			final String secondaryField,
			final String fallback) {
		final String primary = ImportJsonSupport.readText(primaryNode, primaryField, "");
		if (!primary.isBlank()) {
			return primary;
		}
		return ImportJsonSupport.readText(secondaryNode, secondaryField, fallback);
	}

	private static String resolveClassId(
			final String rawClassRef,
			final Map<String, String> classIdsBySourceId,
			final Map<String, String> classIdsByName) {
		if (rawClassRef == null || rawClassRef.isBlank()) {
			return null;
		}

		final String bySourceId = classIdsBySourceId.get(rawClassRef);
		if (bySourceId != null) {
			return bySourceId;
		}
		return classIdsByName.get(rawClassRef);
	}

	private static String resolveFieldId(
			final String rawFieldRef,
			final Map<String, String> fieldIdsBySourceId,
			final Map<String, String> fieldIdsByQualifiedName) {
		if (rawFieldRef == null || rawFieldRef.isBlank()) {
			return null;
		}

		final String bySourceId = fieldIdsBySourceId.get(rawFieldRef);
		if (bySourceId != null) {
			return bySourceId;
		}
		return fieldIdsByQualifiedName.get(rawFieldRef);
	}

	private OnlineModelizerImporter() {
	}
}
