package lu.kbra.modelizer_next.bootstrap.subapp;

import java.nio.file.Path;

import lu.kbra.modelizer_next.common.VersionComparator.ParsedVersion;

/**
 * Installed application version together with the jar path that should be launched.
 * @param version version value used by the operation
 * @param entryPoint point in canvas coordinates
 * @param jarFile file to read or write
 */
public record InstalledApplication(ParsedVersion version, String entryPoint, Path jarFile) {
}
