package lu.kbra.modelizer_next.utils;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RelativePathSerializer extends JsonSerializer<Path> {

	@Override
	public void serialize(final Path value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		if (value.isAbsolute()) {
			gen.writeString(value.toAbsolutePath().toString());
		} else {
			gen.writeString(value.toString());
		}
	}

}
