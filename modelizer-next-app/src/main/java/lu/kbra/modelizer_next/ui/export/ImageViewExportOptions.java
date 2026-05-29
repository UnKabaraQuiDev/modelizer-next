package lu.kbra.modelizer_next.ui.export;

/**
 * Options used by image based exporters.
 *
 * @param transparentBackground whether formats that support alpha should keep a transparent background
 */
public record ImageViewExportOptions(boolean transparentBackground) implements ViewExportOptions {

	/**
	 * Returns default image export options.
	 *
	 * @return the default options
	 */
	public static ImageViewExportOptions defaults() {
		return new ImageViewExportOptions(false);
	}

}
