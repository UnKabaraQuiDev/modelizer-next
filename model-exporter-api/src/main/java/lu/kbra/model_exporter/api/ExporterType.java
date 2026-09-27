package lu.kbra.model_exporter.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExporterType {

	CODE("Code", "mnce"),
	IMAGE("Image", "mnie");

	private final String name;
	private final String extension;

}
