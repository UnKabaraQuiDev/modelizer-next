package lu.kbra.modelizer_next.bootstrap.subapp;

import java.nio.file.Path;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

public record InstalledApplication(ParsedVersion version, String entryPoint, Path jarFile) {
}
