package lu.kbra.modelizer_next;

public class AppConfig {

	private ThemeMode themeMode = ThemeMode.SYSTEM;
	private String selectedPaletteName;
	private String defaultPaletteName;
	private boolean autoCheckUpdates = true;
	private String skippedUpdateVersion;

	public ThemeMode getThemeMode() {
		return this.themeMode;
	}

	public void setThemeMode(final ThemeMode themeMode) {
		this.themeMode = themeMode;
	}

	public String getSelectedPaletteName() {
		return this.selectedPaletteName;
	}

	public void setSelectedPaletteName(final String selectedPaletteName) {
		this.selectedPaletteName = selectedPaletteName;
	}

	public String getDefaultPaletteName() {
		return this.defaultPaletteName;
	}

	public void setDefaultPaletteName(final String defaultPaletteName) {
		this.defaultPaletteName = defaultPaletteName;
	}

	public boolean isAutoCheckUpdates() {
		return this.autoCheckUpdates;
	}

	public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	public String getSkippedUpdateVersion() {
		return this.skippedUpdateVersion;
	}

	public void setSkippedUpdateVersion(final String skippedUpdateVersion) {
		this.skippedUpdateVersion = skippedUpdateVersion;
	}

	@Override
	public String toString() {
		return "AppConfig@" + System.identityHashCode(this) + " [themeMode=" + this.themeMode + ", selectedPaletteName="
				+ this.selectedPaletteName + ", defaultPaletteName=" + this.defaultPaletteName + ", autoCheckUpdates="
				+ this.autoCheckUpdates + ", skippedUpdateVersion=" + this.skippedUpdateVersion + "]";
	}

}
