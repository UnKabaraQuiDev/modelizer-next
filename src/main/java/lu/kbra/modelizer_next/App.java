package lu.kbra.modelizer_next;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.pclib.PCUtils;

public class App {

	private static final String APP_DIR_PROPERTY = "APP_DIR";
	private static final String APP_FOLDER_NAME = "modelizer-next";

	public static JsonNode JSON;

	public static String NAME;
	public static String DESCRIPTION;
	public static String VERSION;
	public static String REVISION;

	public static String ISSUES_URL = "https://github.com/UnKabaraQuiDev/modelizer-next/issues/";

	public static void init() throws JsonProcessingException {
		final String fileContent = PCUtils.readPackagedStringFile("/app.json");
		JSON = MNMain.OBJECT_MAPPER.readTree(fileContent);

		NAME = JSON.path("name").asText();
		DESCRIPTION = JSON.path("description").asText();
		VERSION = JSON.path("version").asText();
		REVISION = JSON.path("revision").asText();
		ISSUES_URL = JSON.path("issues").asText(ISSUES_URL);

		ensureDirsExists();
	}

	public static File getAppDirectory() {
		final String override = System.getProperty(APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		final String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("win")) {
			final String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) {
				return new File(appData, APP_FOLDER_NAME);
			}
		}

		return new File(System.getProperty("user.home"), "." + APP_FOLDER_NAME);
	}

	public static File getConfigFile() {
		return new File(getAppDirectory(), "config.json");
	}

	public static File getStylesDirectory() {
		return new File(getAppDirectory(), "styles");
	}

	public static void ensureDirsExists() {
		getAppDirectory().mkdirs();
		getStylesDirectory().mkdirs();
	}

	public static String title(String title) {
		return App.NAME + " " + App.VERSION + " - " + title;
	}

	public static AppConfig loadConfig() {
		App.ensureDirsExists();

		final File file = App.getConfigFile();
		if (!file.isFile()) {
			final AppConfig config = new AppConfig();
			saveConfig(config);
			return config;
		}

		try {
			return MNMain.OBJECT_MAPPER.readValue(file, AppConfig.class);
		} catch (final IOException e) {
			e.printStackTrace();
			return new AppConfig();
		}
	}

	public static void saveConfig(final AppConfig config) {
		App.ensureDirsExists();

		try {
			MNMain.OBJECT_MAPPER.writeValue(App.getConfigFile(), config);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
