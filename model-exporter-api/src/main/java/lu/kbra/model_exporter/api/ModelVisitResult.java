package lu.kbra.model_exporter.api;

import java.nio.file.Path;
import java.util.List;

public record ModelVisitResult(List<Path> exportedFiles) {

}
