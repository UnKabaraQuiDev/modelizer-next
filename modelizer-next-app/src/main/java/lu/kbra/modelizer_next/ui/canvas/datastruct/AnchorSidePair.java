package lu.kbra.modelizer_next.ui.canvas.datastruct;

import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;

public record AnchorSidePair(AnchorSide fromSide, AnchorSide toSide) {

	public boolean isFromVertical() {
		return this.fromSide.isTopBottom();
	}

	public boolean isToVertical() {
		return this.toSide.isTopBottom();
	}

}
