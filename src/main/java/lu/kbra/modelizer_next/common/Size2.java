package lu.kbra.modelizer_next.common;

import java.util.Objects;

public class Size2 {

	private double width;
	private double height;

	public Size2() {
		this(0.0, 0.0);
	}

	public Size2(final double width, final double height) {
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return this.width;
	}

	public void setWidth(final double width) {
		this.width = width;
	}

	public double getHeight() {
		return this.height;
	}

	public void setHeight(final double height) {
		this.height = height;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Size2 size2)) {
			return false;
		}
		return Double.compare(size2.width, this.width) == 0 && Double.compare(size2.height, this.height) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.width, this.height);
	}

	@Override
	public String toString() {
		return "Size2@" + System.identityHashCode(this) + " [width=" + width + ", height=" + height + "]";
	}

}