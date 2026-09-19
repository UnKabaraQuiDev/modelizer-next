package lu.kbra.model_exporter.api;

import lu.kbra.modelizer_next.domain.DiagramModel;

public interface ModelVisitor {
	
	void visitDiagram(DiagramModel file);
	
}
