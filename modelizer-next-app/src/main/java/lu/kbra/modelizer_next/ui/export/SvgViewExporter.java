package lu.kbra.modelizer_next.ui.export;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;

final class SvgViewExporter {

	public static void
			export(final DiagramCanvas canvas, final ViewExportRequest request, final ViewExportContext context, final File outputFile)
					throws IOException {
		final DOMImplementation domImplementation = GenericDOMImplementation.getDOMImplementation();
		final Document document = domImplementation.createDocument(ViewExporter.SVG_NAMESPACE_URI, "svg", null);
		final SVGGraphics2D svgGraphics = new SVGGraphics2D(document);

		final Dimension exportSize = canvas.getExportSize(request.scope());
		svgGraphics.setSVGCanvasSize(exportSize);

		canvas.paintExport(svgGraphics, request.scope());

		try (FileWriter writer = new FileWriter(outputFile)) {
			svgGraphics.stream(writer, true);
		}
	}

}
