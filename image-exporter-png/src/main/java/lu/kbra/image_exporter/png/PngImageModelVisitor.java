package lu.kbra.image_exporter.png;

import lombok.Getter;
import lu.kbra.model_exporter.api.ModelVisitor;
import lu.kbra.modelizer_next.domain.DiagramModel;

@Getter
public class PngImageModelVisitor implements ModelVisitor {

	private final PngImageExporterOptions options;

	public PngImageModelVisitor(PngImageExporterOptions pclibOptions) {
		this.options = pclibOptions.clone();
	}

	@Override
	public void visitDiagram(DiagramModel file) {
	}

}
