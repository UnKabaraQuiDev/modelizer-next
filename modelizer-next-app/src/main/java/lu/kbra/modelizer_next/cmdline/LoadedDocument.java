package lu.kbra.modelizer_next.cmdline;

import java.net.URI;

import lu.kbra.modelizer_next.domain.document.ModelDocument;

/**
 * Immutable value object for loaded document data.
 *
 * @param sourceFile file to read or write
 * @param document   document to read or modify
 */
public record LoadedDocument(URI sourceFile, ModelDocument document) {
}