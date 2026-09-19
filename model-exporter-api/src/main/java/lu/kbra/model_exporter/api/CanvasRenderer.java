package lu.kbra.model_exporter.api;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import lu.kbra.modelizer_next.domain.data.ViewExportScope;

public interface CanvasRenderer {

	/**
	 * Creates an export image on the active canvas.
	 *
	 * @param scope export scope to use
	 * @return the created export image
	 */
	BufferedImage createExportImage(final ViewExportScope scope, final Optional<Color> backgroundColor);

	/**
	 * Creates an export preview image on the active canvas.
	 *
	 * @param scope     export scope to use
	 * @param maxWidth  width value
	 * @param maxHeight height value
	 * @return the created export preview image
	 */
	BufferedImage createExportPreviewImage(
			final ViewExportScope scope,
			final int maxWidth,
			final int maxHeight,
			final Optional<Color> backgroundColor);

	/**
	 * Returns the export size on the active canvas.
	 *
	 * @param scope export scope to use
	 * @return the export size
	 */
	Dimension getExportSize(final ViewExportScope scope);

	/**
	 * Paints the export.
	 *
	 * @param graphics graphics context used for drawing
	 * @param rawScope raw scope value used by the operation
	 */
	void paintExport(final Graphics2D graphics, final ViewExportScope rawScope);

}
