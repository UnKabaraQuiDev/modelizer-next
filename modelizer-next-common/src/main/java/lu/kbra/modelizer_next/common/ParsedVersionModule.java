package lu.kbra.modelizer_next.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public class ParsedVersionModule extends SimpleModule {

	public ParsedVersionModule() {
		this.addDeserializer(ParsedVersion.class, new ParsedVersionDeserializer());
	}

	public class ParsedVersionDeserializer extends JsonDeserializer<ParsedVersion> {

		@Override
		public ParsedVersion deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JacksonException {
			final String rawVersion = p.getValueAsString();

			if (rawVersion == null || rawVersion.isBlank()) {
				return null;
			}

			return VersionComparator.parse(rawVersion);
		}

	}

}
