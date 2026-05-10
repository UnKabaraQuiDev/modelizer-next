package lu.kbra.modelizer_next.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Jackson module for VersionComparator.ParsedVersion values.
 */
public class ParsedVersionModule extends SimpleModule {

	/**
	 * Creates a parsed version module instance.
	 */
	public ParsedVersionModule() {
		this.addDeserializer(ParsedVersion.class, new ParsedVersionDeserializer());
	}

	/**
	 * Represents a parsed version deserializer in the shared utility part of the application.
	 */
	public class ParsedVersionDeserializer extends JsonDeserializer<ParsedVersion> {

		/**
		 * Reads the value from JSON.
		 *
		 * @param p    p value used by the operation
		 * @param ctxt ctxt value used by the operation
		 * @return the deserialize result
		 * @throws IOException      if the operation cannot be completed
		 * @throws JacksonException if the operation cannot be completed
		 */
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
