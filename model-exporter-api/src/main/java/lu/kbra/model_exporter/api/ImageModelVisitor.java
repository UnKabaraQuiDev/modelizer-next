package lu.kbra.model_exporter.api;

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

import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.document.ModelDocument;
import lu.kbra.pclib.PCUtils;

public final class ImageModelVisitor {

	public static void export(
			final ModelDocument file,
			final ImageOptionsManager optionsManager,
			final ImageExporterOptions options,
			ExportUpdateCallback callback,
			final String format,
			final String extension)
			throws ExportFailedException {
		callback = callback.createSubSection("Export: " + ExporterApiContext.getApiContext().getCurrentDocument() + " as " + format);
		try {
			final Map<PanelType, ? extends CanvasRenderer> renderers = ExporterApiContext.getApiContext()
					.getRenderers()
					.apply(options.getPanels());

			int i = 0;
			for (final PanelType pt : options.getPanels()) {
				callback = callback.createSubSection(pt.name());
				callback.setProgress(0);
				BufferedImage image = renderers.get(pt)
						.createExportImage(options.getScope(),
								optionsManager.supportsTransparency() && ((TransparencyOwner) options).isTransparentBackground()
										? Optional.empty()
										: Optional.of(options.getBackgroundColor().orElse(Color.WHITE)));

				if (optionsManager.supportsTransparency() && !((TransparencyOwner) options).isTransparentBackground()) {
					final Color backgroundColor = options.getBackgroundColor().orElse(Color.WHITE);

					final BufferedImage flattened = new BufferedImage(image.getWidth(), image.getHeight(), optionsManager.getImageType());

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
				if (optionsManager.supportsTransparency()) {
					final Dimension maxDimension = ((MaxDimensionOwner) options).getMaxDimension();

					if (maxDimension != null && maxDimension.width > 0 && maxDimension.height > 0
							&& (image.getWidth() > maxDimension.width || image.getHeight() > maxDimension.height)) {

						final double scale = Math.min((double) maxDimension.width / image.getWidth(),
								(double) maxDimension.height / image.getHeight());

						final int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
						final int height = Math.max(1, (int) Math.round(image.getHeight() * scale));

						final int imageType = optionsManager.getImageType();

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
				}

				final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);

				if (!writers.hasNext()) {
					throw new IOException("No ImageWriter found for format: " + format);
				}

				final ImageWriter writer = writers.next();

				final Path outputFile = Paths
						.get(ImageModelVisitor
								.ensureExtension(
										ImageModelVisitor
												.replacePlaceholders((options.getOutputPath().isAbsolute() ? options.getOutputPath()
														: ExporterApiContext.getApiContext().getCurrentDocument().toPath().getParent())
														.resolve(options.getNameFormat())
														.toString(), pt),
										extension));

				try (OutputStream os = Files.newOutputStream(outputFile); ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
					writer.setOutput(ios);

					final ImageWriteParam param = writer.getDefaultWriteParam();

					if (optionsManager.supportCompression() && param.canWriteCompressed()) {
						param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

						// PNG compression level: 0 = none, 9 = maximum.
						final int compressionLevel = Math.max(0, Math.min(9, ((CompressionOwner) options).getCompressionLevel()));

						// ImageIO's PNG writer treats compressionQuality inversely:
						// 1.0 = least compression, 0.0 = maximum compression.
						param.setCompressionQuality(1.0f - compressionLevel / 9.0f);
					}

					if (optionsManager.getImageType() != image.getType()) {
						image = ImageModelVisitor
								.convertImageType(image, optionsManager.getImageType(), options.getBackgroundColor().orElse(Color.WHITE));
					}

					writer.write(null, new IIOImage(image, null, null), param);
				} finally {
					writer.dispose();
				}

				callback.setProgress(100f);
				callback = callback.endSubSection();
				i++;
				callback.setProgress(100f / options.getPanels().size() * i);
			}
			callback.setProgress(100f);
		} catch (final IOException e) {
			throw new ExportFailedException(e);
		} finally {
			callback.endSubSection();
		}
	}

	private static BufferedImage convertImageType(final BufferedImage source, final int targetType, final Color backgroundColor) {
		if (source.getType() == targetType) {
			return source;
		}

		final BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), targetType);

		final Graphics2D graphics = target.createGraphics();
		try {
			if (!target.getColorModel().hasAlpha()) {
				graphics.setColor(backgroundColor);
				graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
			}

			graphics.drawImage(source, 0, 0, null);
		} finally {
			graphics.dispose();
		}

		return target;
	}

	public static String ensureExtension(final String string, final String ext) {
		return string.endsWith("." + ext) ? string : string + "." + ext;
	}

	public static String replacePlaceholders(final String string, final PanelType panelType) {
		return string.replace("{FILENAME}", PCUtils.removeFileExtension(ExporterApiContext.getApiContext().getCurrentDocument().getName()))
				.replace("{PANEL}", panelType.name());
	}

}
