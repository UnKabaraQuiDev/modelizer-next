package lu.kbra.modelizer_next.ui.canvas.data;

public enum AnchorSide {

	TOP,
	BOTTOM,
	LEFT,
	RIGHT;

	public AnchorSide clockwise() {
		return switch (this) {
		case TOP -> RIGHT;
		case RIGHT -> BOTTOM;
		case BOTTOM -> LEFT;
		case LEFT -> TOP;
		};
	}

	public AnchorSide counterClockwise() {
		return switch (this) {
		case TOP -> LEFT;
		case LEFT -> BOTTOM;
		case BOTTOM -> RIGHT;
		case RIGHT -> TOP;
		};
	}

	public boolean isTopBottom() {
		return this == TOP || this == BOTTOM;
	}

	public boolean isLeftRight() {
		return this == LEFT || this == RIGHT;
	}

}
