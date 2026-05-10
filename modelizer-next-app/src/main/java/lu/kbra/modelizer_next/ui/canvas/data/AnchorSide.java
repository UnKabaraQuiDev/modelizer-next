package lu.kbra.modelizer_next.ui.canvas.data;

public enum AnchorSide {

	TOP,
	BOTTOM,
	LEFT,
	RIGHT;

	/**
	 * Returns the next anchor side in clockwise order.
	 *
	 * @return the clockwise result
	 */
	public AnchorSide clockwise() {
		return switch (this) {
		case TOP -> RIGHT;
		case RIGHT -> BOTTOM;
		case BOTTOM -> LEFT;
		case LEFT -> TOP;
		};
	}

	/**
	 * Returns the next anchor side in counter-clockwise order.
	 *
	 * @return the counter clockwise result
	 */
	public AnchorSide counterClockwise() {
		return switch (this) {
		case TOP -> LEFT;
		case LEFT -> BOTTOM;
		case BOTTOM -> RIGHT;
		case RIGHT -> TOP;
		};
	}

	/**
	 * @return {@code true} if current element represents {@link LEFT} or {@link RIGHT}; otherwise
	 *         {@code false}
	 */
	public boolean isLeftRight() {
		return this == LEFT || this == RIGHT;
	}

	/**
	 * @return {@code true} if current element represents {@link TOP} or {@link BOTTOM}; otherwise
	 *         {@code false}
	 */
	public boolean isTopBottom() {
		return this == TOP || this == BOTTOM;
	}

}
