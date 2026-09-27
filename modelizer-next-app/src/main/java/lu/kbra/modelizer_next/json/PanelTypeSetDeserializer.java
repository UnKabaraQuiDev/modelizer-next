package lu.kbra.modelizer_next.json;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.domain.data.PanelType;

public class PanelTypeSetDeserializer extends JsonDeserializer<Set<PanelType>> {

	@Override
	public Set<PanelType> deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final JsonNode node = parser.getCodec().readTree(parser);

		final EnumSet<PanelType> result = EnumSet.noneOf(PanelType.class);

		if (node.isArray()) {
			for (final JsonNode element : node) {
				if (!element.isTextual()) {
					throw JsonMappingException.from(parser, "Panel types must be strings");
				}

				result.add(this.parsePanelType(element.textValue(), context));
			}

			return result;
		}

		if (node.isTextual()) {
			final String value = node.textValue().trim();

			if (value.isEmpty()) {
				return result;
			}

			for (final String part : value.split(",")) {
				result.add(this.parsePanelType(part.trim(), context));
			}

			return result;
		}

		throw JsonMappingException.from(parser, "Expected a panel type string or JSON array");
	}

	private PanelType parsePanelType(final String value, final DeserializationContext context) throws IOException {
		final String normalized = value.trim();

		for (final PanelType type : PanelType.values()) {
			if (type.name().equalsIgnoreCase(normalized.toUpperCase())) {
				return type;
			}
		}

		if (normalized.length() == 1) {
			return this.parsePanelType(normalized.charAt(0), context);
		}

		throw context.weirdStringException(value, PanelType.class, "Unknown panel type: " + value);
	}

	private PanelType parsePanelType(final char value, final DeserializationContext context) throws IOException {
		return switch (Character.toLowerCase(value)) {
		case 'c' -> PanelType.CONCEPTUAL;
		case 'l' -> PanelType.LOGICAL;
		case 'p' -> PanelType.PHYSICAL;

		default -> throw context.weirdStringException(String.valueOf(value), PanelType.class, "Unknown panel type");
		};
	}

}
