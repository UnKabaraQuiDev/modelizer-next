package lu.kbra.modelizer_next.common;

import java.awt.geom.Point2D.Double;

/**
 * Mutable two-dimensional size value used by layout objects.
 */
public class Size2D extends Double {

	private static final long serialVersionUID = -1729261687089888180L;

	/**
	 * Creates a size 2 d instance.
	 */
	public Size2D() {
	}

	/**
	 * Creates a size 2 d instance.
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public Size2D(final double x, final double y) {
		super(x, y);
	}

	/**
	 * Returns the height.
	 *
	 * @return the height
	 */
	public double getHeight() {
		return super.y;
	}

	/**
	 * Returns the width.
	 *
	 * @return the width
	 */
	public double getWidth() {
		return super.x;
	}

	/**
	 * Returns the x.
	 *
	 * @return the x
	 */
	@Deprecated
	@Override
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y.
	 *
	 * @return the y
	 */
	@Deprecated
	@Override
	public double getY() {
		return this.y;
	}

	/**
	 * Sets the height.
	 *
	 * @param y y coordinate
	 */
	public void setHeight(final double y) {
		super.y = y;
	}

	/**
	 * Sets the width.
	 *
	 * @param x x coordinate
	 */
	public void setWidth(final double x) {
		super.x = x;
	}

	/**
	 * Sets the x.
	 *
	 * @param x x coordinate
	 */
	@Deprecated
	public void setX(final double x) {
		super.x = x;
	}

	/**
	 * Sets the y.
	 *
	 * @param y y coordinate
	 */
	@Deprecated
	public void setY(final double y) {
		super.y = y;
	}

}
