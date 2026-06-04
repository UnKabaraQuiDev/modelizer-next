package lu.kbra.modelizer_next.bootstrap.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lu.kbra.modelizer_next.bootstrap.BootstrapConfig;
import lu.kbra.modelizer_next.common.ParsedVersionModule;
import lu.kbra.modelizer_next.common.Platform;
import lu.kbra.pclib.PCUtils;

/**
 * Constants and JSON mapper setup for the bootstrap module.
 */
public final class BootstrapApp {

	public static final ObjectMapper MAPPER = BootstrapApp.createMapper();
	public static final String APP_DIR_PROPERTY = "APP_DIR";
	public static final String ENABLE_UPDATE_PROPERTY = BootstrapApp.class.getSimpleName() + ".enableUpdate";
	public static boolean ENABLE_UPDATE = PCUtils.getBoolean(BootstrapApp.ENABLE_UPDATE_PROPERTY, true);
	public static final String APP_FOLDER_NAME = "modelizer-next";
	public static final String FORCE_JAR_NAME_PROPERTY = BootstrapApp.class.getSimpleName() + ".forceJarName";
	public static String FORCE_JAR_NAME = System.getProperty(BootstrapApp.FORCE_JAR_NAME_PROPERTY);
	public static final String FORCE_BOOTSTRAP_UPDATE_PROPERTY = BootstrapApp.class.getSimpleName() + ".forceBootstrapUpdate";
	public static boolean FORCE_BOOTSTRAP_UPDATE = Boolean.getBoolean(BootstrapApp.FORCE_BOOTSTRAP_UPDATE_PROPERTY);

	public static JsonNode JSON;

	public static String NAME;
	public static String VERSION;
	public static String REPOSITORY_URL;
	public static String RELEASES_URL;
	public static String UPDATES_MANIFEST_URL;
	public static String DISTRIBUTOR;
	public static String RELEASES_MANIFEST_URL;

	public static BootstrapConfig BOOTSTRAP_CONFIG;

	/**
	 * Ensures that the directories exists or is up to date during bootstrap/update processing.
	 *
	 * @throws IOException if the operation cannot be completed
	 */
	public static void ensureDirectories() throws IOException {
		BootstrapApp.getSharedDirectory().mkdirs();
		BootstrapApp.getUpdatesDirectory().mkdirs();
//		BootstrapApp.getTempDirectory().mkdirs();
	}

	/**
	 * Returns the updates directory.
	 *
	 * @return the updates directory
	 */
	public static File getUpdatesDirectory() {
		return new File(BootstrapApp.getSharedDirectory(), "updates");
	}

	@Deprecated
	public static File getOldUpdatesDirectory() {
		return new File(BootstrapApp.getOldSharedDirectory(), "updates");
	}

	/**
	 * Returns the bootstrap config file.
	 *
	 * @return the bootstrap config file
	 */
	public static File getBootstrapConfigFile() {
		return new File(BootstrapApp.getSharedDirectory(), "bootstrap-config.json");
	}

	@Deprecated
	public static File getOldBootstrapConfigFile() {
		return new File(BootstrapApp.getOldSharedDirectory(), "bootstrap-config.json");
	}

	/**
	 * Returns the shared file directory, system-wide..
	 *
	 * @return the shared directory
	 */
	public static File getSharedDirectory() {
		final String override = System.getProperty(BootstrapApp.APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		return switch (Platform.get()) {
		case WINDOWS -> {
			final String programData = System.getenv("ProgramData");
			if (programData != null && !programData.isBlank()) {
				yield new File(programData, BootstrapApp.APP_FOLDER_NAME);
			}
			yield new File("C:\\ProgramData", BootstrapApp.APP_FOLDER_NAME);
		}
		case MACOS -> new File("/Library/Application Support/" + BootstrapApp.APP_FOLDER_NAME);
		case LINUX -> new File("/var/lib/" + BootstrapApp.APP_FOLDER_NAME);
		case UNSUPPORTED -> new File("." + File.pathSeparator + BootstrapApp.APP_FOLDER_NAME);
		default -> throw new IllegalArgumentException("Unexpected value: " + Platform.get());
		};
	}

	@Deprecated
	public static File getOldSharedDirectory() {
		final String override = System.getProperty(BootstrapApp.APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		final String os = System.getProperty("os.name", "").toLowerCase();
		final String home = System.getProperty("user.home");

		if (os.contains("win")) {
			final String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) {
				return new File(appData, BootstrapApp.APP_FOLDER_NAME);
			}
		} else if (os.contains("mac")) {
			return new File(home, "Library/Application Support/" + BootstrapApp.APP_FOLDER_NAME);
		}

		return new File(home, "." + BootstrapApp.APP_FOLDER_NAME);
	}

	/**
	 * Returns a temp directory.
	 *
	 * @return the temp directory
	 */
	public static File getTempDirectory() {
		final String override = System.getProperty(BootstrapApp.APP_DIR_PROPERTY);
		if (override != null && !override.isBlank()) {
			return new File(override);
		}

		return switch (Platform.get()) {
		case WINDOWS -> {
			final String temp = System.getenv("TEMP");
			if (temp != null && !temp.isBlank()) {
				yield new File(temp, BootstrapApp.APP_FOLDER_NAME);
			}
			yield new File("C:\\Windows\\Temp", BootstrapApp.APP_FOLDER_NAME);
		}
		case MACOS -> {
			final String tmp = System.getProperty("java.io.tmpdir");
			if (tmp != null && !tmp.isBlank()) {
				yield new File(tmp, BootstrapApp.APP_FOLDER_NAME);
			}
			yield new File("/tmp", BootstrapApp.APP_FOLDER_NAME);
		}
		case LINUX -> new File("/tmp/" + BootstrapApp.APP_FOLDER_NAME);
		case UNSUPPORTED -> new File("." + File.pathSeparator + BootstrapApp.APP_FOLDER_NAME);
		default -> throw new IllegalArgumentException("Unexpected value: " + Platform.get());
		};
	}

	/**
	 * Initializes shared state required before the object is used during bootstrap/update processing.
	 *
	 * @throws IOException if the operation cannot be completed
	 */
	public static void init() throws IOException {
		BootstrapApp.JSON = BootstrapApp.MAPPER.readTree(PCUtils.readPackagedStringFile("/bootstrap.json"));

		BootstrapApp.NAME = BootstrapApp.JSON.path("name").asText("Modelizer Next Bootstrap");
		BootstrapApp.VERSION = BootstrapApp.JSON.path("version").asText("0.0.0");
		BootstrapApp.REPOSITORY_URL = BootstrapApp.JSON.path("repository").asText("https://github.com/UnKabaraQuiDev/modelizer-next");
		BootstrapApp.RELEASES_URL = BootstrapApp.JSON.path("releases").asText(BootstrapApp.REPOSITORY_URL + "/releases");
		BootstrapApp.UPDATES_MANIFEST_URL = BootstrapApp.JSON.path("updatesManifest")
				.asText("https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/registry/registry/versions.json");
		BootstrapApp.DISTRIBUTOR = BootstrapApp.JSON.path("distributor").asText();
		BootstrapApp.RELEASES_MANIFEST_URL = BootstrapApp.JSON.path("releasesManifestUrl")
				.asText("https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/registry/registry/release.json");

		BootstrapApp.BOOTSTRAP_CONFIG = new BootstrapConfig(BootstrapApp.NAME,
				BootstrapApp.VERSION,
				BootstrapApp.REPOSITORY_URL,
				BootstrapApp.RELEASES_URL,
				BootstrapApp.UPDATES_MANIFEST_URL,
				BootstrapApp.DISTRIBUTOR);

		BootstrapApp.ensureDirectories();
	}

	/**
	 * Checks whether first launch is enabled or applies.
	 *
	 * @return {@code true} if first launch is enabled or applies; otherwise {@code false}
	 */
	public static boolean isFirstLaunch() {
		return !BootstrapApp.getBootstrapConfigFile().exists();
	}

	public static boolean shouldMoveUserToSystem() {
		return isFirstLaunch() && BootstrapApp.getOldBootstrapConfigFile().exists();
	}

	/**
	 * Loads the configuration during bootstrap/update processing.
	 *
	 * @return the load configuration result
	 */
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

	/**
	 * Saves the configuration during bootstrap/update processing.
	 *
	 * @param configuration configuration value used by the operation
	 */
	public static void saveConfiguration(final BootstrapConfiguration configuration) {
		try {
			BootstrapApp.ensureDirectories();
			BootstrapApp.MAPPER.writerWithDefaultPrettyPrinter().writeValue(BootstrapApp.getBootstrapConfigFile(), configuration);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Creates a mapper during bootstrap/update processing.
	 *
	 * @return the created mapper
	 */
	private static ObjectMapper createMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new ParsedVersionModule());
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	/**
	 * Creates a bootstrap application instance.
	 */
	private BootstrapApp() {
	}

}
