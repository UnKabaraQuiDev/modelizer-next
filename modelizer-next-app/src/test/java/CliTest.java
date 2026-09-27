import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Disabled
public class CliTest {

	private static final Path JAR;
	static {
		try {
			final Path target = Path.of("target");

			try (final var files = Files.list(target)) {
				JAR = files.filter(path -> path.getFileName().toString().endsWith("-with-dependencies.jar"))
						.findFirst()
						.orElseThrow(
								() -> new IllegalStateException("Could not find *-with-dependencies.jar in " + target.toAbsolutePath()));
			}

			System.out.println("Using: " + JAR);
		} catch (final Exception e) {
			throw new RuntimeException("Could not determine application JAR", e);
		}
	}

	private int runApplication(final String... args) throws IOException, InterruptedException {
		final ProcessBuilder processBuilder = new ProcessBuilder();

		processBuilder.command("java", "-jar", CliTest.JAR.toString());

		Collections.addAll(processBuilder.command(), args);

		processBuilder.inheritIO();

		final Process process = processBuilder.start();

		return process.waitFor();
	}

	@Test
	void applicationShouldExitSuccessfullyWithValidArguments() throws IOException, InterruptedException {
		final int exitCode = this.runApplication("-h");

		Assertions.assertEquals(0, exitCode);
	}

	@ParameterizedTest
	@ValueSource(strings = { "png", "svg", "webp", "tif", "tiff", "jpeg", "bmp" })
	void applicationShouldExitSuccessfullyWithValidArguments(String param) throws IOException, InterruptedException {
		final int exitCode = this.runApplication("-h", param);

		Assertions.assertEquals(0, exitCode);
	}

	@ParameterizedTest
	@ValueSource(strings = { "png", "svg", "webp", "tif", "tiff", "jpeg", "bmp" })
	void applicationShouldExportWithValidArguments(String param) throws IOException, InterruptedException {
		final int exitCode = this.runApplication("-t", param, "-e", "~/Downloads/sample.mn");

		Assertions.assertEquals(0, exitCode);
		final File file = Path.of(System.getProperty("user.home"), "Downloads", "sample-CONCEPTUAL." + (switch (param) {
		case "tiff" -> "tif";
		case "jpeg" -> "jpg";
		default -> param;
		})).toFile();
		Assertions.assertTrue(file.exists(), "File: " + file + " does not exists.");
	}

}
