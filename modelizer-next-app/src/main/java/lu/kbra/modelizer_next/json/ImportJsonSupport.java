package lu.kbra.modelizer_next.json;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;

/**
 * Shared JSON helper methods used by document importers.
 */
final class ImportJsonSupport {

	/**
	 * Adds the class layout while converting JSON data.
	 * @param panelState panel state value used by the operation
	 * @param classId id of the class to look up or modify
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	static void addClassLayout(final PanelState panelState, final String classId, final double x, final double y) {
		ImportJsonSupport.addClassLayout(panelState, classId, x, y, 0.0, 0.0);
	}

	/**
	 * Adds the class layout while converting JSON data.
	 * @param panelState panel state value used by the operation
	 * @param classId id of the class to look up or modify
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param width width value
	 * @param height height value
	 */
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

	/**
	 * Adds the comment layout while converting JSON data.
	 * @param panelState panel state value used by the operation
	 * @param commentId id of the comment to look up or modify
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	static void addCommentLayout(final PanelState panelState, final String commentId, final double x, final double y) {
		ImportJsonSupport.addCommentLayout(panelState, commentId, x, y, 220.0, 80.0);
	}

	/**
	 * Adds the comment layout while converting JSON data.
	 * @param panelState panel state value used by the operation
	 * @param commentId id of the comment to look up or modify
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param width width value
	 * @param height height value
	 */
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

	/**
	 * Parses the color from the supplied input while converting JSON data.
	 * @param colorNode color node value used by the operation
	 * @param fallback fallback value used by the operation
	 * @return the parsed color
	 */
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

	/**
	 * Stores the mapping between a source class id and the imported class model.
	 * @param classIdsByName name value to use
	 * @param conceptualName name value to use
	 * @param technicalName name value to use
	 * @param classId id of the class to look up or modify
	 */
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

	/**
	 * Stores an alternate field id that points to the imported field model.
	 * @param fieldIdsByQualifiedName name value to use
	 * @param className name value to use
	 * @param fieldName name value to use
	 * @param fieldId id of the field to look up or modify
	 */
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

	/**
	 * Stores the mapping between a source field id and the imported field model.
	 * @param fieldIdsByQualifiedName name value to use
	 * @param conceptualClassName name value to use
	 * @param technicalClassName name value to use
	 * @param conceptualFieldName name value to use
	 * @param technicalFieldName name value to use
	 * @param fieldId id of the field to look up or modify
	 */
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

	/**
	 * Reads the text while converting JSON data.
	 * @param node JSON node to read
	 * @param fieldName name value to use
	 * @param fallback text value for fallback
	 * @return the read text result
	 */
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

	/**
	 * Creates an import JSON support instance.
	 */
	private ImportJsonSupport() {
	}
}
