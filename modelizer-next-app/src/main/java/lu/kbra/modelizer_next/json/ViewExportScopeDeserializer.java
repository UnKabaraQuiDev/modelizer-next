package lu.kbra.modelizer_next.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.cmdline.CommandLineExportParser.MissingArgumentException;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;

public class ViewExportScopeDeserializer extends JsonDeserializer<ViewExportScope> {

	@Override
	public ViewExportScope deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final JsonNode node = parser.getCodec().readTree(parser);

		if (node.isTextual()) {
			final String value = node.textValue().trim();

			return switch (value.toLowerCase()) {
			case "selection", "e" -> ViewExportScope.SELECTION;
			case "view", "v" -> ViewExportScope.VIEW;
			case "everything", "all", "a" -> ViewExportScope.EVERYTHING;
			default -> throw new MissingArgumentException("Unsupported export scope: " + value);
			};

		}

		throw JsonMappingException.from(parser, "Expected a view export scope type string.");
	}

}