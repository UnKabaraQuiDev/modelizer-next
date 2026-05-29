package lu.kbra.modelizer_next.bootstrap;

/**
 * Minimal configuration exposed to the application by the bootstrap runtime.
 *
 * @param name               name value to read, write, or display
 * @param version            text value for version
 * @param repositoryUrl      URL to use
 * @param releasesUrl        URL to use
 * @param updatedManifestUrl URL to use
 * @param distributor        text value for distributor
 */
public record BootstrapConfig(
		String name,
		String version,
		String repositoryUrl,
		String releasesUrl,
		String updatedManifestUrl,
		String distributor) {

}
