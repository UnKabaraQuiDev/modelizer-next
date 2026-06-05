package lu.kbra.modelizer_next.ui.canvas.datastruct;

public record CopyPasteSpecialData(
		boolean keepOutgoingLinks,
		boolean keepInternalLinks,
		boolean keepLinks,
		boolean withDefaultStyle,
		String nameOverwrite) {

}