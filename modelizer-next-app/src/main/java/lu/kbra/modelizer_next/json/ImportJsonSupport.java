package lu.kbra.modelizer_next.json;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;

final class ImportJsonSupport {

	private ImportJsonSupport() {
	}

	static void addClassLayout(final PanelState panelState, final String classId, final double x, final double y) {
		ImportJsonSupport.addClassLayout(panelState, classId, x, y, 0.0, 0.0);
	}

	static void addClassLayout(
			final PanelState panelState,
			final String classId,
			final double x,
			final double y,
			final double width,
			final double height) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.CLASS);
		layout.setObjectId(classId);
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(width, height));
		panelState.getNodeLayouts().add(layout);
	}

	static void addCommentLayout(final PanelState panelState, final String commentId, final double x, final double y) {
		ImportJsonSupport.addCommentLayout(panelState, commentId, x, y, 220.0, 80.0);
	}

	static void addCommentLayout(
			final PanelState panelState,
			final String commentId,
			final double x,
			final double y,
			final double width,
			final double height) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.COMMENT);
		layout.setObjectId(commentId);
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(width, height));
		panelState.getNodeLayouts().add(layout);
	}

	static Color parseColor(final JsonNode colorNode, final Color fallback) {
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

	static void putClassMapping(
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

	static void putFieldAlias(
			final Map<String, String> fieldIdsByQualifiedName,
			final String className,
			final String fieldName,
			final String fieldId) {
		if (className == null || className.isBlank() || fieldName == null || fieldName.isBlank()) {
			return;
		}
		fieldIdsByQualifiedName.put(className + "." + fieldName, fieldId);
	}

	static void putFieldMapping(
			final Map<String, String> fieldIdsByQualifiedName,
			final String conceptualClassName,
			final String technicalClassName,
			final String conceptualFieldName,
			final String technicalFieldName,
			final String fieldId) {
		ImportJsonSupport.putFieldAlias(fieldIdsByQualifiedName, conceptualClassName, conceptualFieldName, fieldId);
		ImportJsonSupport.putFieldAlias(fieldIdsByQualifiedName, conceptualClassName, technicalFieldName, fieldId);
		ImportJsonSupport.putFieldAlias(fieldIdsByQualifiedName, technicalClassName, conceptualFieldName, fieldId);
		ImportJsonSupport.putFieldAlias(fieldIdsByQualifiedName, technicalClassName, technicalFieldName, fieldId);
	}

	static String readText(final JsonNode node, final String fieldName, final String fallback) {
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
