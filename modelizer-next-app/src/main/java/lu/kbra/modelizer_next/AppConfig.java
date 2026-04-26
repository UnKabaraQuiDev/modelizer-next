package lu.kbra.modelizer_next;

import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.ui.ThemeMode;

public class AppConfig {

	private ThemeMode themeMode = ThemeMode.SYSTEM;
	private String selectedPaletteName = "Default";
	private String defaultPaletteName = "Default";
	private List<String> pinnedPaletteNames = new ArrayList<>();
	private boolean autoCheckUpdates = true;
	private String skippedUpdateVersion;

	public String getDefaultPaletteName() {
		return this.defaultPaletteName;
	}

	public List<String> getPinnedPaletteNames() {
		if (this.pinnedPaletteNames == null) {
			this.pinnedPaletteNames = new ArrayList<>();
		}
		return this.pinnedPaletteNames;
	}

	public String getSelectedPaletteName() {
		return this.selectedPaletteName;
	}

	public String getSkippedUpdateVersion() {
		return this.skippedUpdateVersion;
	}

	public ThemeMode getThemeMode() {
		return this.themeMode;
	}

	public boolean isAutoCheckUpdates() {
		return this.autoCheckUpdates;
	}

	public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	public void setDefaultPaletteName(final String defaultPaletteName) {
		this.defaultPaletteName = defaultPaletteName;
	}

	public void setPinnedPaletteNames(final List<String> pinnedPaletteNames) {
		this.pinnedPaletteNames = pinnedPaletteNames == null ? new ArrayList<>() : new ArrayList<>(pinnedPaletteNames);
	}

	public void setSelectedPaletteName(final String selectedPaletteName) {
		this.selectedPaletteName = selectedPaletteName;
	}

	public void setSkippedUpdateVersion(final String skippedUpdateVersion) {
		this.skippedUpdateVersion = skippedUpdateVersion;
	}

	public void setThemeMode(final ThemeMode themeMode) {
		this.themeMode = themeMode;
	}

	@Override
	public String toString() {
		return "AppConfig@" + System.identityHashCode(this) + " [themeMode=" + this.themeMode + ", selectedPaletteName="
				+ this.selectedPaletteName + ", defaultPaletteName=" + this.defaultPaletteName + ", pinnedPaletteNames="
				+ this.getPinnedPaletteNames() + ", autoCheckUpdates=" + this.autoCheckUpdates + ", skippedUpdateVersion="
				+ this.skippedUpdateVersion + "]";
	}

}
