package lu.kbra.code_exporter.api;

import lu.kbra.modelizer_next.domain.DiagramModel;

public interface ModelVisitor {
	
	void visitDiagram(DiagramModel file);
	
}
