package lu.kbra.modelizer_next.common;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.pclib.PCUtils;

/**
 * Application-wide constants, directories, mapper setup, and metadata used by the desktop client.
 */
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

	public static AppConfig CONFIG;

	/**
	 * Ensures that the dirs exists exists or is up to date.
	 */
	public static void ensureDirsExists() {
		App.getAppDirectory().mkdirs();
		App.getStylesDirectory().mkdirs();
		App.getUpdateDownloadsDirectory().mkdirs();
	}

	/**
	 * Returns the application directory.
	 *
	 * @return the application directory
	 */
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

	/**
	 * Returns the config file.
	 *
	 * @return the config file
	 */
	public static File getConfigFile() {
		return new File(App.getAppDirectory(), "config.json");
	}

	/**
	 * Returns the styles directory.
	 *
	 * @return the styles directory
	 */
	public static File getStylesDirectory() {
		return new File(App.getAppDirectory(), "styles");
	}

	/**
	 * Returns the update downloads directory.
	 *
	 * @return the update downloads directory
	 */
	public static File getUpdateDownloadsDirectory() {
		return new File(App.getAppDirectory(), "updates");
	}

	/**
	 * Initializes shared state required before the object is used.
	 *
	 * @throws JsonProcessingException if the operation cannot be completed
	 */
	public static void init() throws JsonProcessingException {
		final String fileContent = PCUtils.readPackagedStringFile(App.class, "/app.json");
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

	/**
	 * Loads the config.
	 */
	public static synchronized void loadConfig() {
		App.ensureDirsExists();

		final File file = App.getConfigFile();
		if (!file.isFile()) {
			final AppConfig config = new AppConfig();
			App.CONFIG = config;
			App.saveConfig();
			return;
		}

		try {
			App.CONFIG = MNMain.OBJECT_MAPPER.readValue(file, AppConfig.class);
		} catch (final IOException e) {
			e.printStackTrace();
			App.CONFIG = new AppConfig();
		}
	}

	/**
	 * Saves the config.
	 */
	public static synchronized void saveConfig() {
		App.ensureDirsExists();

		try {
			MNMain.OBJECT_MAPPER.writeValue(App.getConfigFile(), App.CONFIG);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the application window title from the app metadata.
	 *
	 * @param title title text to display
	 * @return the title result
	 */
	public static String title(final String title) {
		return App.NAME + " - " + title;
	}

}
