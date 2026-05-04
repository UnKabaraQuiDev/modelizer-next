package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

public record ClassSideKey(String classId, AnchorSide side) {

	public boolean isVertical() {
		return side.isTopBottom();
	}

}
