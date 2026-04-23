package lu.kbra.modelizer_next.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lu.kbra.pclib.PCUtils;

public final class BootstrapApp {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String APP_DIR_PROPERTY = "APP_DIR";
	private static final String ENABLE_UPDATE_PROPERTY = "enableUpdate";
	public static boolean ENABLE_UPDATE = PCUtils.getBoolean(BootstrapApp.ENABLE_UPDATE_PROPERTY, true);
	private static final String APP_FOLDER_NAME = "modelizer-next";

	public static JsonNode JSON;

	public static String NAME;
	public static String VERSION;
	public static String REPOSITORY_URL;
	public static String RELEASES_URL;
	public static String UPDATES_MANIFEST_URL;
	public static String DISTRIBUTOR;

	public static BootstrapConfig BOOTSTRAP_CONFIG;

	public static void ensureDirectories() throws IOException {
		Files.createDirectories(BootstrapApp.getHomeDirectory().toPath());
		Files.createDirectories(BootstrapApp.getApplicationsDirectory().toPath());
		Files.createDirectories(BootstrapApp.getTempDirectory().toPath());
	}

	public static File getApplicationsDirectory() {
		return new File(BootstrapApp.getHomeDirectory(), "updates");
	}

	public static File getBootstrapConfigFile() {
		return new File(BootstrapApp.getHomeDirectory(), "bootstrap-config.json");
	}

	public static File getHomeDirectory() {
		final String override = System.getProperty(BootstrapApp.APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		final String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("win")) {
			final String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) {
				return new File(appData, BootstrapApp.APP_FOLDER_NAME);
			}
		}
		return new File(System.getProperty("user.home"), "." + BootstrapApp.APP_FOLDER_NAME);
	}

	public static File getTempDirectory() {
		return new File(BootstrapApp.getHomeDirectory(), "updates");
	}

	public static void init() throws IOException {
		BootstrapApp.JSON = BootstrapApp.MAPPER.readTree(PCUtils.readPackagedStringFile("/bootstrap.json"));

		BootstrapApp.NAME = BootstrapApp.JSON.path("name").asText("Modelizer Next Bootstrap");
		BootstrapApp.VERSION = BootstrapApp.JSON.path("version").asText("0.0.0");
		BootstrapApp.REPOSITORY_URL = BootstrapApp.JSON.path("repository").asText("https://github.com/UnKabaraQuiDev/modelizer-next");
		BootstrapApp.RELEASES_URL = BootstrapApp.JSON.path("releases").asText(BootstrapApp.REPOSITORY_URL + "/releases");
		BootstrapApp.UPDATES_MANIFEST_URL = BootstrapApp.JSON.path("updatesManifest")
				.asText("https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/registry/registry/versions.json");
		BootstrapApp.DISTRIBUTOR = BootstrapApp.JSON.path("distributor").asText();

		BootstrapApp.BOOTSTRAP_CONFIG = new BootstrapConfig(BootstrapApp.NAME,
				BootstrapApp.VERSION,
				BootstrapApp.REPOSITORY_URL,
				BootstrapApp.RELEASES_URL,
				BootstrapApp.UPDATES_MANIFEST_URL,
				BootstrapApp.DISTRIBUTOR);

		BootstrapApp.ensureDirectories();
	}

	public static boolean isFirstLaunch() {
		return !BootstrapApp.getBootstrapConfigFile().isFile();
	}

	public static BootstrapConfiguration loadConfiguration() {
		final File file = BootstrapApp.getBootstrapConfigFile();
		if (!file.isFile()) {
			return new BootstrapConfiguration();
		}
		try {
			return BootstrapApp.MAPPER.readValue(file, BootstrapConfiguration.class);
		} catch (final IOException ex) {
			return new BootstrapConfiguration();
		}
	}

	public static void saveConfiguration(final BootstrapConfiguration configuration) {
		try {
			BootstrapApp.ensureDirectories();
			BootstrapApp.MAPPER.writerWithDefaultPrettyPrinter().writeValue(BootstrapApp.getBootstrapConfigFile(), configuration);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private BootstrapApp() {
	}

}
