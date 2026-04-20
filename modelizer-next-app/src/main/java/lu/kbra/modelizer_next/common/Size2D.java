package lu.kbra.modelizer_next.common;

import java.awt.geom.Point2D.Double;

public class Size2D extends Double {

	private static final long serialVersionUID = -1729261687089888180L;

	public Size2D() {
	}

	public Size2D(final double x, final double y) {
		super(x, y);
	}

	public double getHeight() {
		return super.y;
	}

	public double getWidth() {
		return super.x;
	}

	public void setHeight(final double y) {
		super.y = y;
	}

	public void setWidth(final double x) {
		super.x = x;
	}

	public void setX(final double x) {
		super.x = x;
	}

	public void setY(final double y) {
		super.y = y;
	}

}
