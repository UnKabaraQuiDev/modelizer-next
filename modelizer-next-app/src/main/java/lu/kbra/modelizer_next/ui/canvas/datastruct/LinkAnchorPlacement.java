package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

public record LinkAnchorPlacement(AnchorSide fromSide, AnchorSide toSide, int fromIndex, int fromCount, int toIndex, int toCount) {
}
