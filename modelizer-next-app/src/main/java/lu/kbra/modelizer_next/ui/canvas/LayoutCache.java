package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Rectangle2D;
import java.util.Map;

public interface LayoutCache extends DiagramCanvasExt {

	default void buildClassBoundsByIdCache() {
		final DiagramCanvas canvas = this.getCanvas();

		canvas.classBoundsById.clear();
//		final NodeLayout layout = canvas.resolveRenderLayout(canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
//		return canvas.computeClassBounds(fromClass, layout);

		this.getDocument()
				.getModel()
				.getClasses(this.getPanelType())
				.parallelStream()
				.forEach(classModel -> canvas.classBoundsById.put(classModel.getId(),
						canvas.computeClassBounds(classModel, canvas.findOrCreateNodeLayout(classModel))));
	}

	default Map<String, Rectangle2D> validateClassBoundsByIdCache() {
		final DiagramCanvas canvas = this.getCanvas();
		if (canvas.classBoundsById.size() != this.getDocument().getModel().getClasses(this.getPanelType()).size()) {
			this.buildClassBoundsByIdCache();
		}
		return canvas.classBoundsById;
	}

}
