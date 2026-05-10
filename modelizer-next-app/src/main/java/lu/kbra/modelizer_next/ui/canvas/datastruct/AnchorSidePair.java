package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Pair of node sides used when resolving link anchors.
 *
 * @param fromSide from side value used by the operation
 * @param toSide   to side value used by the operation
 */
public record AnchorSidePair(AnchorSide fromSide, AnchorSide toSide) {

	/**
	 * Checks whether from vertical is enabled or applies on the active canvas.
	 *
	 * @return {@code true} if from vertical is enabled or applies; otherwise {@code false}
	 */
	public boolean isFromVertical() {
		return this.fromSide.isTopBottom();
	}

	/**
	 * Checks whether to vertical is enabled or applies on the active canvas.
	 *
	 * @return {@code true} if to vertical is enabled or applies; otherwise {@code false}
	 */
	public boolean isToVertical() {
		return this.toSide.isTopBottom();
	}

}
