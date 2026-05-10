package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Cache key that identifies one side of one class node.
 * @param classId id of the class to look up or modify
 * @param side node side to inspect
 */
public record ClassSideKey(String classId, AnchorSide side) {

	/**
	 * Checks whether vertical is enabled or applies on the active canvas.
	 * @return {@code true} if vertical is enabled or applies; otherwise {@code false}
	 */
	public boolean isVertical() {
		return this.side.isTopBottom();
	}

}
