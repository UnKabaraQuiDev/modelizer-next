package lu.kbra.modelizer_next;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.pclib.PCUtils;

public class App {

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
	}

	public static String title(String title) {
		return App.NAME + " " + App.VERSION + " - " + title;
	}

}
