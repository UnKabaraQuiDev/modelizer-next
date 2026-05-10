package lu.kbra.modelizer_next.ui.canvas.data;

/**
 * Sides of a rectangular node that can receive link anchors.
 */
public enum AnchorSide {

	TOP,
	BOTTOM,
	LEFT,
	RIGHT;

	/**
	 * Returns the next anchor side in clockwise order.
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
	 * Checks whether left right is enabled or applies on the active canvas.
	 * @return {@code true} if left right is enabled or applies; otherwise {@code false}
	 */
	public boolean isLeftRight() {
		return this == LEFT || this == RIGHT;
	}

	/**
	 * Checks whether top bottom is enabled or applies on the active canvas.
	 * @return {@code true} if top bottom is enabled or applies; otherwise {@code false}
	 */
	public boolean isTopBottom() {
		return this == TOP || this == BOTTOM;
	}

}
