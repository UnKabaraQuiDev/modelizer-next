package lu.kbra.model_exporter.api;

public interface ImageOptionsManager extends OptionsManager {

	boolean supportsTransparency();

	boolean supportsFixedSize();

	boolean supportCompression();

	int getImageType();

}
