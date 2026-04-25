package lu.kbra.modelizer_next.bootstrap;

import java.nio.file.Path;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

record InstalledApplication(ParsedVersion version, String entryPoint, Path jarFile) {
}
