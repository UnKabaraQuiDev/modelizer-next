package lu.kbra.modelizer_next.common;

import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.ui.ThemeMode;

/**
 * User configuration loaded from and saved to the application configuration file.
 */
public class AppConfig {

	private ThemeMode themeMode = ThemeMode.SYSTEM;
	private String selectedPaletteName = "Default";
	private String defaultPaletteName = "Default";
	private List<String> pinnedPaletteNames = new ArrayList<>();
	private boolean autoCheckUpdates = true;
	private String skippedUpdateVersion;

	/**
	 * Returns the default palette name.
	 *
	 * @return the default palette name
	 */
	public String getDefaultPaletteName() {
		return this.defaultPaletteName;
	}

	/**
	 * Returns the pinned palette names.
	 *
	 * @return the pinned palette names
	 */
	public List<String> getPinnedPaletteNames() {
		if (this.pinnedPaletteNames == null) {
			this.pinnedPaletteNames = new ArrayList<>();
		}
		return this.pinnedPaletteNames;
	}

	/**
	 * Returns the selected palette name.
	 *
	 * @return the selected palette name
	 */
	public String getSelectedPaletteName() {
		return this.selectedPaletteName;
	}

	/**
	 * Returns the skipped update version.
	 *
	 * @return the skipped update version
	 */
	public String getSkippedUpdateVersion() {
		return this.skippedUpdateVersion;
	}

	/**
	 * Returns the theme mode.
	 *
	 * @return the theme mode
	 */
	public ThemeMode getThemeMode() {
		return this.themeMode;
	}

	/**
	 * Checks whether auto check updates is enabled or applies.
	 *
	 * @return {@code true} if auto check updates is enabled or applies; otherwise {@code false}
	 */
	public boolean isAutoCheckUpdates() {
		return this.autoCheckUpdates;
	}

	/**
	 * Sets the auto check updates.
	 *
	 * @param autoCheckUpdates whether auto check updates is enabled
	 */
	public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	/**
	 * Sets the default palette name.
	 *
	 * @param defaultPaletteName name value to use
	 */
	public void setDefaultPaletteName(final String defaultPaletteName) {
		this.defaultPaletteName = defaultPaletteName;
	}

	/**
	 * Sets the pinned palette names.
	 *
	 * @param pinnedPaletteNames name values to use
	 */
	public void setPinnedPaletteNames(final List<String> pinnedPaletteNames) {
		this.pinnedPaletteNames = pinnedPaletteNames == null ? new ArrayList<>() : new ArrayList<>(pinnedPaletteNames);
	}

	/**
	 * Sets the selected palette name.
	 *
	 * @param selectedPaletteName name value to use
	 */
	public void setSelectedPaletteName(final String selectedPaletteName) {
		this.selectedPaletteName = selectedPaletteName;
	}

	/**
	 * Sets the skipped update version.
	 *
	 * @param skippedUpdateVersion text value for skipped update version
	 */
	public void setSkippedUpdateVersion(final String skippedUpdateVersion) {
		this.skippedUpdateVersion = skippedUpdateVersion;
	}

	/**
	 * Sets the theme mode.
	 *
	 * @param themeMode theme mode value used by the operation
	 */
	public void setThemeMode(final ThemeMode themeMode) {
		this.themeMode = themeMode;
	}

	/**
	 * Builds a debug string for this application config.
	 *
	 * @return a debug string for this object
	 */
	@Override
	public String toString() {
		return "AppConfig@" + System.identityHashCode(this) + " [themeMode=" + this.themeMode + ", selectedPaletteName="
				+ this.selectedPaletteName + ", defaultPaletteName=" + this.defaultPaletteName + ", pinnedPaletteNames="
				+ this.getPinnedPaletteNames() + ", autoCheckUpdates=" + this.autoCheckUpdates + ", skippedUpdateVersion="
				+ this.skippedUpdateVersion + "]";
	}

}
