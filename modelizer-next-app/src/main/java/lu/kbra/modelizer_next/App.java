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
	public static String DISTRIBUTOR;
	public static boolean PORTABLE;

	public static String ISSUES_URL;
	public static String WEBSITE_URL;
	public static String ENTRY_POINT;
	public static String AUTHOR_WEBSITE_URL;

	public static void ensureDirsExists() {
		App.getAppDirectory().mkdirs();
		App.getStylesDirectory().mkdirs();
		App.getUpdateDownloadsDirectory().mkdirs();
	}

	public static File getAppDirectory() {
		final String override = System.getProperty(App.APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		if (App.PORTABLE) {
			return new File(".");
		}

		final String os = System.getProperty("os.name", "").toLowerCase();
		final String home = System.getProperty("user.home");

		if (os.contains("win")) {
			final String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) {
				return new File(appData, App.APP_FOLDER_NAME);
			}
		} else if (os.contains("mac")) {
			return new File(home, "Library/Application Support/" + App.APP_FOLDER_NAME);
		}

		return new File(home, "." + App.APP_FOLDER_NAME);
	}

	public static File getConfigFile() {
		return new File(App.getAppDirectory(), "config.json");
	}

	public static File getStylesDirectory() {
		return new File(App.getAppDirectory(), "styles");
	}

	public static File getUpdateDownloadsDirectory() {
		return new File(App.getAppDirectory(), "updates");
	}

	public static void init() throws JsonProcessingException {
		final String fileContent = PCUtils.readPackagedStringFile("/app.json");
		App.JSON = MNMain.OBJECT_MAPPER.readTree(fileContent);

		App.NAME = App.JSON.path("name").asText();
		App.DESCRIPTION = App.JSON.path("description").asText();
		App.VERSION = App.JSON.path("version").asText();
		App.DISTRIBUTOR = App.JSON.path("distributor").asText();
		App.ISSUES_URL = App.JSON.path("issues").asText();
		App.ENTRY_POINT = App.JSON.path("entryPoint").asText();
		App.PORTABLE = App.JSON.path("portable").asBoolean(false);
		App.WEBSITE_URL = App.JSON.path("website").asText();
		App.AUTHOR_WEBSITE_URL = App.JSON.path("authorWebsite").asText();

		App.ensureDirsExists();
	}

	public static AppConfig loadConfig() {
		App.ensureDirsExists();

		final File file = App.getConfigFile();
		if (!file.isFile()) {
			final AppConfig config = new AppConfig();
			App.saveConfig(config);
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

	public static String title(final String title) {
		return App.NAME + " - " + title;
	}

}
