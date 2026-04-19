package lu.kbra.modelizer_next.common;

import java.util.Objects;

public class Point2 {

	private double x;
	private double y;

	public Point2() {
		this(0.0, 0.0);
	}

	public Point2(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return this.x;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public double getY() {
		return this.y;
	}

	public void setY(final double y) {
		this.y = y;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Point2 point2)) {
			return false;
		}
		return Double.compare(point2.x, this.x) == 0 && Double.compare(point2.y, this.y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y);
	}

	@Override
	public String toString() {
		return "Point2@" + System.identityHashCode(this) + " [x=" + x + ", y=" + y + "]";
	}

}
