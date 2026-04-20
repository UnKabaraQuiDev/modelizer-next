package lu.kbra.modelizer_next.common;

import java.awt.Color;

import lu.kbra.pclib.PCUtils;

public class ColorUtils {

	public static Color ofArgb(final int argb) {
		return new Color(argb);
	}

	public static Color ofHex(final String string) {
		return PCUtils.hexToColor(string);
	}

	public static Color ofRgb(final int rgb) {
		return new Color(0xFF000000 | rgb & 0x00FFFFFF);
	}

}
