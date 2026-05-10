package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

/**
 * Calculated placement of one link anchor and its label direction.
 *
 * @param fromSide  from side value used by the operation
 * @param toSide    to side value used by the operation
 * @param fromIndex zero-based index to use
 * @param fromCount count value to use
 * @param toIndex   zero-based index to use
 * @param toCount   count value to use
 */
public record LinkAnchorPlacement(AnchorSide fromSide, AnchorSide toSide, int fromIndex, int fromCount, int toIndex, int toCount) {
}
