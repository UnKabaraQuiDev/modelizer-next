package lu.kbra.modelizer_next.common;

import java.awt.Color;

import lu.kbra.pclib.PCUtils;

/**
 * Utility methods for serializing, deserializing, and deriving AWT colors.
 */
public class ColorUtils {

	/**
	 * Creates a value from the supplied argb.
	 *
	 * @param argb numeric argb value
	 * @return the of argb result
	 */
	public static Color ofArgb(final int argb) {
		return new Color(argb);
	}

	/**
	 * Creates a value from the supplied hex.
	 *
	 * @param string text value for string
	 * @return the of hex result
	 */
	public static Color ofHex(final String string) {
		return PCUtils.hexToColor(string);
	}

	/**
	 * Creates a value from the supplied rgb.
	 *
	 * @param rgb numeric rgb value
	 * @return the of rgb result
	 */
	public static Color ofRgb(final int rgb) {
		return new Color(0xFF000000 | rgb & 0x00FFFFFF);
	}

}
