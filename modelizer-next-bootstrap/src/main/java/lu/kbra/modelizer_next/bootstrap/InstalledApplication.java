package lu.kbra.modelizer_next.bootstrap;

import java.nio.file.Path;

record InstalledApplication(String version, String entryPoint, Path jarFile) {
}
