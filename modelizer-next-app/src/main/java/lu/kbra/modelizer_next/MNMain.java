package lu.kbra.modelizer_next;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import lu.kbra.modelizer_next.common.SystemThemeDetector;
import lu.kbra.modelizer_next.json.ColorModule;

public class MNMain {

	public static final ObjectMapper OBJECT_MAPPER = MNMain.createMapper();

	public static void applyConfiguredLookAndFeel() {
		try {
			final AppConfig config = App.loadConfig();
			final ThemeMode themeMode = config == null || config.getThemeMode() == null ? ThemeMode.SYSTEM : config.getThemeMode();

			switch (themeMode) {
			case LIGHT -> FlatLightLaf.setup();
			case DARK -> FlatDarkLaf.setup();
			case SYSTEM -> {
				if (SystemThemeDetector.isDark()) {
					FlatDarkLaf.setup();
				} else {
					FlatLightLaf.setup();
				}
			}
			}
		} catch (final Exception ignored) {
			ignored.printStackTrace();
		}
	}

	private static ObjectMapper createMapper() {
		final ObjectMapper mapper = new ObjectMapper(JsonFactory.builder()
				.configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
				.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
				.build());

		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new ColorModule());

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		return mapper;
	}

	public static void main(final String[] args) {
		new ModelizerAppEntryPoint().start(args);
	}

}
