package lu.kbra.modelizer_next;

import java.awt.Color;

public final class Consts {

	public static final String NAME;
	public static final String VERSION;
	public static final Color FG_COLOR = Color.BLACK;
	public static final Color BG_COLOR = Color.WHITE;
	public static final double MOUSE_ZOOM_FACTOR = 1.1f;
	public static final double MIN_ZOOM = 0.2;
	public static final double MAX_ZOOM = 4;

	static {
		final Package pack = Consts.class.getPackage();

		NAME = pack.getImplementationTitle();
		VERSION = pack.getImplementationVersion();
	}

}
