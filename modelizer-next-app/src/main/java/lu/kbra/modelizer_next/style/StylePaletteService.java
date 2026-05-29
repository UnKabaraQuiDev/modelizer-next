package lu.kbra.modelizer_next.style;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lu.kbra.modelizer_next.MNMain;
import lu.kbra.modelizer_next.common.App;

/**
 * Creates, normalizes, and applies reusable style palettes.
 */
public final class StylePaletteService {

	/**
	 * Deletes the by name.
	 *
	 * @param paletteName name value to use
	 */
	public static void deleteByName(final String paletteName) {
		if (paletteName == null || paletteName.isBlank()) {
			return;
		}

		final File file = new File(App.getStylesDirectory(), StylePaletteService.sanitizeFileName(paletteName) + ".json");
		if (file.isFile()) {
			file.delete();
		}
	}

	/**
	 * Ensures that the default palette exists or is up to date.
	 */
	private static void ensureDefaultPalette() {
		final File[] files = App.getStylesDirectory().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
		if (files != null && files.length > 0) {
			return;
		}

		final StylePalette palette = new StylePalette();
		palette.setName("Default");
		StylePaletteService.save(palette);
	}

	/**
	 * Loads the all.
	 *
	 * @return the matching values
	 */
	public static List<StylePalette> loadAll() {
		App.ensureDirsExists();
		StylePaletteService.ensureDefaultPalette();

		final File[] files = App.getStylesDirectory().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

		final List<StylePalette> palettes = new ArrayList<>();
		if (files == null) {
			return palettes;
		}

		Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

		for (final File file : files) {
			try {
				palettes.add(MNMain.OBJECT_MAPPER.readValue(file, StylePalette.class));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return palettes;
	}

	/**
	 * Sanitizes the file name so it can be used safely.
	 *
	 * @param name name value to read, write, or display
	 * @return the sanitize file name result
	 */
	private static String sanitizeFileName(final String name) {
		if (name == null || name.isBlank()) {
			return "unnamed";
		}
		return name.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	/**
	 * Saves the current state to persistent storage.
	 *
	 * @param palette palette value used by the operation
	 */
	public static void save(final StylePalette palette) {
		App.ensureDirsExists();

		final String safeName = StylePaletteService.sanitizeFileName(palette.getName());
		final File file = new File(App.getStylesDirectory(), safeName + ".json");

		try {
			MNMain.OBJECT_MAPPER.writeValue(file, palette);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a style palette service instance.
	 */
	private StylePaletteService() {
	}

}
