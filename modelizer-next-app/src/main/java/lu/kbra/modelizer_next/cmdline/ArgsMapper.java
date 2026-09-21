package lu.kbra.modelizer_next.cmdline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ArgsMapper {

	private ArgsMapper() {
	}

	public static <T> T parse(final String[] args, final Class<T> targetClass, final ObjectMapper mapper) {
		final ObjectNode root = mapper.createObjectNode();

		for (String arg : args) {
			if (arg.startsWith("--")) {
				arg = arg.substring(2);
			}

			final int equals = arg.indexOf('=');

			if (equals < 0) {
				throw new IllegalArgumentException("Expected argument in the form name=value: " + arg);
			}

			final String path = arg.substring(0, equals);
			final String value = arg.substring(equals + 1);

			ArgsMapper.setNestedValue(root, path, value, mapper);
		}

		try {
			return mapper.treeToValue(root, targetClass);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not convert arguments to " + targetClass.getSimpleName(), e);
		}
	}

	private static void setNestedValue(final ObjectNode root, final String path, final String value, final ObjectMapper mapper) {
		final String[] parts = path.split("\\.");

		ObjectNode current = root;
		for (int i = 0; i < parts.length - 1; i++) {
			final String part = parts[i];

			final JsonNode existing = current.get(part);

			if (existing == null) {
				final ObjectNode child = mapper.createObjectNode();
				current.set(part, child);
				current = child;
			} else if (existing.isObject()) {
				current = (ObjectNode) existing;
			} else {
				throw new IllegalArgumentException("Cannot create nested property '" + path + "': '" + part + "' is already a value");
			}
		}

		current.put(parts[parts.length - 1], value);
	}

}
