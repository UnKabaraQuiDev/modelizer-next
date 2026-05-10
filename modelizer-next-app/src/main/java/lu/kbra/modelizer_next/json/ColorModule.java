package lu.kbra.modelizer_next.json;

import java.awt.Color;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module that serializes and deserializes AWT colors in Modelizer JSON files.
 */
public class ColorModule extends SimpleModule {

	/**
	 * Represents an AWT color deserializer in the JSON import/export part of the application.
	 */
	public class AwtColorDeserializer extends JsonDeserializer<Color> {

		/**
		 * Reads the value from JSON.
		 * @param p p value used by the operation
		 * @param ctxt ctxt value used by the operation
		 * @return the deserialize result
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public Color deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
			final String raw = p.getValueAsString();
			if (raw == null || raw.isBlank()) {
				return null;
			}

			final String value = raw.startsWith("#") ? raw.substring(1) : raw;

			try {
				return switch (value.length()) {
				case 6 -> new Color(Integer.parseInt(value.substring(0, 2), 16),
						Integer.parseInt(value.substring(2, 4), 16),
						Integer.parseInt(value.substring(4, 6), 16));
				case 8 -> new Color(Integer.parseInt(value.substring(0, 2), 16),
						Integer.parseInt(value.substring(2, 4), 16),
						Integer.parseInt(value.substring(4, 6), 16),
						Integer.parseInt(value.substring(6, 8), 16));
				default -> throw ctxt.weirdStringException(raw, Color.class, "Expected color in format #RRGGBB or #RRGGBBAA");
				};
			} catch (final NumberFormatException ex) {
				throw ctxt.weirdStringException(raw, Color.class, "Invalid hex color");
			}
		}
	}

	/**
	 * Represents an AWT color serializer in the JSON import/export part of the application.
	 */
	public class AwtColorSerializer extends JsonSerializer<Color> {

		/**
		 * Handles the d type while converting JSON data.
		 * @return the handled type result
		 */
		@Override
		public Class<Color> handledType() {
			return Color.class;
		}

		/**
		 * Writes the value to JSON.
		 * @param value value to process
		 * @param gen gen value used by the operation
		 * @param serializers serializers value used by the operation
		 * @throws IOException if the operation cannot be completed
		 */
		@Override
		public void serialize(final Color value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}

			if (value.getAlpha() == 255) {
				gen.writeString(String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue()));
				return;
			}

			gen.writeString(String.format("#%02X%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha()));
		}

	}

	private static final long serialVersionUID = -6363765641985882615L;

	/**
	 * Creates a color module instance while converting JSON data.
	 */
	public ColorModule() {
		this.addSerializer(new AwtColorSerializer());
		this.addDeserializer(Color.class, new AwtColorDeserializer());
	}

}
