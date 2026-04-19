package lu.kbra.modelizer_next.style;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.MNMain;

public final class StylePaletteService {

	private StylePaletteService() {
	}

	public static void deleteByName(final String paletteName) {
		if (paletteName == null || paletteName.isBlank()) {
			return;
		}

		final File file = new File(App.getStylesDirectory(), StylePaletteService.sanitizeFileName(paletteName) + ".json");
		if (file.isFile()) {
			file.delete();
		}
	}

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

	private static void ensureDefaultPalette() {
		final File[] files = App.getStylesDirectory().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
		if (files != null && files.length > 0) {
			return;
		}

		final StylePalette palette = new StylePalette();
		palette.setName("Default");
		StylePaletteService.save(palette);
	}

	private static String sanitizeFileName(final String name) {
		if (name == null || name.isBlank()) {
			return "unnamed";
		}
		return name.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

}
