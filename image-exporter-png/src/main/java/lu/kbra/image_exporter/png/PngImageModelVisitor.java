package lu.kbra.image_exporter.png;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import lombok.Getter;
import lu.kbra.model_exporter.api.CanvasRenderer;
import lu.kbra.model_exporter.api.ExportFailedException;
import lu.kbra.model_exporter.api.ExporterApiContext;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.DiagramModel;
import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.pclib.PCUtils;

@Getter
public class PngImageModelVisitor implements ModelVisitor {

	private static final String format = "png";

	private final PngImageExporterOptions options;

	public PngImageModelVisitor(final PngImageExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDiagram(final DiagramModel file) throws ExportFailedException {
		try {
			final Map<PanelType, ? extends CanvasRenderer> renderers = ExporterApiContext.getApiContext()
					.getRenderers()
					.apply(this.options.getPanels());

			for (final PanelType pt : this.options.getPanels()) {
				BufferedImage image = renderers.get(pt)
						.createExportImage(this.options.getScope(),
								this.options.isTransparentBackground() ? Optional.empty() : this.options.getBackgroundColor());

				if (!this.options.isTransparentBackground()) {
					final Color backgroundColor = this.options.getBackgroundColor().orElse(Color.WHITE);

					final BufferedImage flattened = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

					final Graphics2D graphics = flattened.createGraphics();
					try {
						graphics.setColor(backgroundColor);
						graphics.fillRect(0, 0, flattened.getWidth(), flattened.getHeight());

						graphics.drawImage(image, 0, 0, null);
					} finally {
						graphics.dispose();
					}

					image = flattened;
				}

				// Scale down to fit within maxDimension while preserving the aspect ratio.
				final Dimension maxDimension = this.options.getMaxDimension();

				if (maxDimension != null && maxDimension.width > 0 && maxDimension.height > 0
						&& (image.getWidth() > maxDimension.width || image.getHeight() > maxDimension.height)) {

					final double scale = Math.min((double) maxDimension.width / image.getWidth(),
							(double) maxDimension.height / image.getHeight());

					final int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
					final int height = Math.max(1, (int) Math.round(image.getHeight() * scale));

					final int imageType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

					final BufferedImage scaled = new BufferedImage(width, height, imageType);

					final Graphics2D graphics = scaled.createGraphics();
					try {
						graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
						graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						graphics.drawImage(image, 0, 0, width, height, null);
					} finally {
						graphics.dispose();
					}

					image = scaled;
				}

				final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(PngImageModelVisitor.format);

				if (!writers.hasNext()) {
					throw new IOException("No ImageWriter found for format: " + PngImageModelVisitor.format);
				}

				final ImageWriter writer = writers.next();

				final Path outputFile = Paths.get(this
						.ensureExtension(this.replacePlaceholders((this.options.getOutputPath().isAbsolute() ? this.options.getOutputPath()
								: ExporterApiContext.getApiContext().getCurrentDocument().toPath().getParent()).resolve(this.options.getNameFormat())
								.toString(), pt), "png"));

				try (OutputStream os = Files.newOutputStream(outputFile); ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {

					writer.setOutput(ios);

					final ImageWriteParam param = writer.getDefaultWriteParam();

					if (param.canWriteCompressed()) {
						param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

						// PNG compression level: 0 = none, 9 = maximum.
						final int compressionLevel = Math.max(0, Math.min(9, this.options.getCompressionLevel()));

						// ImageIO's PNG writer treats compressionQuality inversely:
						// 1.0 = least compression, 0.0 = maximum compression.
						param.setCompressionQuality(1.0f - compressionLevel / 9.0f);
					}

					writer.write(null, new IIOImage(image, null, null), param);
				} finally {
					writer.dispose();
				}
			}
		} catch (final IOException e) {
			throw new ExportFailedException(e);
		}
	}

	private String ensureExtension(final String string, final String ext) {
		return string.endsWith("." + ext) ? string : string + "." + ext;
	}

	private String replacePlaceholders(final String string, final PanelType panelType) {
		return string.replace("{FILENAME}", PCUtils.removeFileExtension(ExporterApiContext.getApiContext().getCurrentDocument().getName()))
				.replace("{PANEL}", panelType.name());
	}

}
