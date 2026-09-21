package lu.kbra.modelizer_next.json;

import java.awt.Dimension;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class DimensionDeserializer extends JsonDeserializer<Dimension> {

	@Override
	public Dimension deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final JsonNode node = parser.getCodec().readTree(parser);

		if (node.isTextual()) {
			return this.parseString(node.textValue(), context);
		}

		if (node.isObject()) {
			final JsonNode width = node.get("width");
			final JsonNode height = node.get("height");

			if (width == null || height == null) {
				throw context.weirdStringException(node.toString(), Dimension.class, "Dimension requires both width and height");
			}

			return new Dimension(width.asInt(), height.asInt());
		}

		throw context
				.weirdStringException(node.toString(), Dimension.class, "Expected '(width, height)', '[width, height]' or 'width, height' or an object with width and height");
	}

	private Dimension parseString(final String value, final DeserializationContext context) throws IOException {

		String input = value.trim();

		if (input.startsWith("(") && input.endsWith(")") || input.startsWith("[") && input.endsWith("]")) {
			input = input.substring(1, input.length() - 1);
		}

		final String[] parts = input.split(",");

		if (parts.length != 2) {
			throw context.weirdStringException(value, Dimension.class, "Expected '(width, height)' or '[width, height]' or 'width, height'");
		}

		try {
			final int width = Integer.parseInt(parts[0].trim());
			final int height = Integer.parseInt(parts[1].trim());

			return new Dimension(width, height);
		} catch (final NumberFormatException e) {
			throw context.weirdStringException(value, Dimension.class, "Width and height must be integers");
		}
	}

}