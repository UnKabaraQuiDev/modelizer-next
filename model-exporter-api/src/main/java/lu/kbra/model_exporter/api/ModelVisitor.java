package lu.kbra.model_exporter.api;

import lu.kbra.modelizer_next.domain.document.ModelDocument;

public interface ModelVisitor {

	ModelVisitResult visitDocument(ModelDocument file, ExportUpdateCallback callback) throws ExportFailedException;

}
