package lu.kbra.image_exporter.svg;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import lu.kbra.model_exporter.api.CanvasRenderer;
import lu.kbra.model_exporter.api.ExportFailedException;
import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.model_exporter.api.ExporterApiContext;
import lu.kbra.model_exporter.api.ImageModelVisitor;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.document.ModelDocument;

import lombok.Getter;

@Getter
public class SvgImageModelVisitor implements ModelVisitor {

	public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

	private static final String format = "svg";

	private final SvgImageExporterOptions options;

	public SvgImageModelVisitor(final SvgImageExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDocument(final ModelDocument file, ExportUpdateCallback callback) throws ExportFailedException {
		callback = callback.createSubSection(
				"Export: " + ExporterApiContext.getApiContext().getCurrentDocument() + " as " + SvgImageModelVisitor.format);
		try {
			final Map<PanelType, ? extends CanvasRenderer> renderers = ExporterApiContext.getApiContext()
					.getRenderers()
					.apply(this.options.getPanels());

			int i = 0;
			for (final PanelType pt : this.options.getPanels()) {
				callback = callback.createSubSection(pt.name());
				callback.setProgress(0);
				final CanvasRenderer canvas = renderers.get(pt);
				final DOMImplementation domImplementation = GenericDOMImplementation.getDOMImplementation();
				final Document document = domImplementation
						.createDocument(SvgImageModelVisitor.SVG_NAMESPACE_URI, SvgImageModelVisitor.format, null);
				final SVGGraphics2D svgGraphics = new SVGGraphics2D(document);

				final Dimension exportSize = canvas.getExportSize(this.options.getScope());
				svgGraphics.setSVGCanvasSize(exportSize);

				canvas.paintExport(svgGraphics, this.options.getScope());

				final File outputFile = new File(ImageModelVisitor
						.ensureExtension(
								ImageModelVisitor
										.replacePlaceholders((this.options.getOutputPath().isAbsolute() ? this.options.getOutputPath()
												: ExporterApiContext.getApiContext().getCurrentDocument().toPath().getParent())
												.resolve(this.options.getNameFormat())
												.toString(), pt),
								"svg"));

				try (FileWriter writer = new FileWriter(outputFile)) {
					svgGraphics.stream(writer, true);
				}
				callback.setProgress(100f);
				callback = callback.endSubSection();
				i++;
				callback.setProgress(100f / this.options.getPanels().size() * i);
			}
			callback.setProgress(100f);
		} catch (final IOException e) {
			throw new ExportFailedException(e);
		} finally {
			callback.endSubSection();
		}
	}

}
