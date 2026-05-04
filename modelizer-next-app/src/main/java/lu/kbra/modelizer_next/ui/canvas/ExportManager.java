package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;
import lu.kbra.modelizer_next.ui.export.ViewExportScope;

/**
 * Contains file export actions and export scope handling.
 */
public interface ExportManager extends DiagramCanvasExt {

	default Rectangle2D.Double computeExportContentBounds(final Graphics2D g2, final ViewExportScope scope) {
		final LinkedHashSet<SelectedElement> previousFilter = this.getCanvas().exportSelectionFilter;
		if (scope == ViewExportScope.SELECTION && this.getCanvas().exportSelectionFilter == null) {
			this.getCanvas().exportSelectionFilter = new LinkedHashSet<>(this.getCanvas().selectedElements);
		}

		try {
			this.getCanvas().ensureLayouts();

			if (this.getPanelType() == PanelType.CONCEPTUAL) {
				this.getCanvas().invalidateConceptualAnchorCache();
				this.getCanvas().ensureConceptualAnchorCache(g2);
			}

			Rectangle2D.Double bounds = null;
			final boolean onlySelection = scope == ViewExportScope.SELECTION;

			for (final ClassModel classModel : this.getCanvas().document.getModel().getClasses()) {
				if (!classModel.isVisible(this.getPanelType()) || onlySelection && !this.getCanvas().shouldExportClass(classModel)) {
					continue;
				}

				final NodeLayout layout = this.getCanvas()
						.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
				final Rectangle2D classBounds = this.getCanvas().computeClassBounds(g2, classModel, layout);
				bounds = this.getCanvas()
						.expandBounds(bounds, classBounds.getX(), classBounds.getY(), classBounds.getWidth(), classBounds.getHeight());
			}

			for (final CommentModel commentModel : this.getCanvas().document.getModel().getComments()) {
				if (commentModel == null || !this.getCanvas().isCommentVisible(commentModel)
						|| onlySelection && !this.getCanvas().shouldExportComment(commentModel)) {
					continue;
				}

				final NodeLayout layout = this.getCanvas()
						.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId()));
				final Rectangle2D commentBounds = this.getCanvas().computeCommentBounds(g2, commentModel.getText(), layout);
				bounds = this.getCanvas()
						.expandBounds(bounds,
								commentBounds.getX(),
								commentBounds.getY(),
								commentBounds.getWidth(),
								commentBounds.getHeight());

				final Point2D connectorAnchor = this.getCanvas().findBoundTargetAnchor(g2, commentModel);
				if (connectorAnchor != null) {
					bounds = this.getCanvas()
							.expandBounds(bounds,
									Math.min(connectorAnchor.getX(), commentBounds.getCenterX()),
									Math.min(connectorAnchor.getY(), commentBounds.getCenterY()),
									Math.abs(connectorAnchor.getX() - commentBounds.getCenterX()),
									Math.abs(connectorAnchor.getY() - commentBounds.getCenterY()));
				}
			}

			for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
				if (onlySelection && !this.getCanvas().shouldExportLink(linkModel)) {
					continue;
				}

				final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(g2, linkModel);
				if (geometry == null) {
					continue;
				}

				for (final Point2D point : geometry.points()) {
					bounds = this.getCanvas().expandBounds(bounds, point.getX(), point.getY(), 1.0, 1.0);
				}

				if (geometry.labelPoint() != null) {
					bounds = this.getCanvas()
							.expandBounds(bounds, geometry.labelPoint().getX() - 60, geometry.labelPoint().getY() - 20, 120, 40);
				}
			}

			return bounds;
		} finally {
			this.getCanvas().exportSelectionFilter = previousFilter;
		}
	}

	default Dimension computeExportSize(final Graphics2D g2, final ViewExportScope scope) {
		if (scope == ViewExportScope.VIEW) {
			return this.getCanvas().getViewportExportSize();
		}

		final Rectangle2D.Double contentBounds = this.getCanvas().computeExportContentBounds(g2, scope);
		if (contentBounds == null) {
			return this.getCanvas().getViewportExportSize();
		}

		return new Dimension(Math.max(1, (int) Math.ceil(contentBounds.getWidth() + DiagramCanvas.EXPORT_MARGIN * 2.0)),
				Math.max(1, (int) Math.ceil(contentBounds.getHeight() + DiagramCanvas.EXPORT_MARGIN * 2.0)));
	}

	default Rectangle2D.Double computeExportWorldBounds(final Graphics2D g2, final ViewExportScope scope) {
		if (scope == ViewExportScope.VIEW) {
			final PanelState state = this.getCanvas().getPanelState();
			final Dimension viewportSize = this.getCanvas().getViewportExportSize();
			return new Rectangle2D.Double(-state.getPanX() / state.getZoom(),
					-state.getPanY() / state.getZoom(),
					viewportSize.getWidth() / state.getZoom(),
					viewportSize.getHeight() / state.getZoom());
		}

		final Rectangle2D.Double contentBounds = this.getCanvas().computeExportContentBounds(g2, scope);
		if (contentBounds == null) {
			return this.getCanvas().computeExportWorldBounds(g2, ViewExportScope.VIEW);
		}

		return new Rectangle2D.Double(contentBounds.getX() - DiagramCanvas.EXPORT_MARGIN,
				contentBounds.getY() - DiagramCanvas.EXPORT_MARGIN,
				contentBounds.getWidth() + DiagramCanvas.EXPORT_MARGIN * 2.0,
				contentBounds.getHeight() + DiagramCanvas.EXPORT_MARGIN * 2.0);
	}

	default Rectangle2D.Double computeSelectionBounds(final List<SelectedElement> selection) {
		Rectangle2D.Double bounds = null;
		final Set<String> seenNodeLayouts = new HashSet<>();

		for (final SelectedElement element : selection) {
			if (element == null) {
				continue;
			}

			if (element.type() == SelectedType.CLASS || element.type() == SelectedType.FIELD) {
				final String key = LayoutObjectType.CLASS + ":" + element.classId();

				if (!seenNodeLayouts.add(key)) {
					continue;
				}

				final NodeLayout layout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, element.classId());
				bounds = this.getCanvas()
						.expandBounds(bounds,
								layout.getPosition().getX(),
								layout.getPosition().getY(),
								layout.getSize().getWidth(),
								layout.getSize().getHeight());
			} else if (element.type() == SelectedType.COMMENT) {
				final String key = LayoutObjectType.COMMENT + ":" + element.commentId();

				if (!seenNodeLayouts.add(key)) {
					continue;
				}

				final NodeLayout layout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, element.commentId());
				bounds = this.getCanvas()
						.expandBounds(bounds,
								layout.getPosition().getX(),
								layout.getPosition().getY(),
								layout.getSize().getWidth(),
								layout.getSize().getHeight());
			} else if (element.type() == SelectedType.LINK) {
				final LinkLayout layout = this.getCanvas().findOrCreateLinkLayout(element.linkId());

				for (final Point2D.Double bendPoint : layout.getBendPoints()) {
					bounds = this.getCanvas().expandBounds(bounds, bendPoint.getX(), bendPoint.getY(), 1.0, 1.0);
				}

				if (layout.getNameLabelPosition() != null) {
					bounds = this.getCanvas()
							.expandBounds(bounds, layout.getNameLabelPosition().getX(), layout.getNameLabelPosition().getY(), 1.0, 1.0);
				}
			}
		}

		return bounds;
	}

	default Dimension getViewportExportSize() {
		return new Dimension(this.getCanvas().getWidth() <= 0 ? DiagramCanvas.DEFAULT_EXPORT_WIDTH : this.getCanvas().getWidth(),
				this.getCanvas().getHeight() <= 0 ? DiagramCanvas.DEFAULT_EXPORT_HEIGHT : this.getCanvas().getHeight());
	}

}
