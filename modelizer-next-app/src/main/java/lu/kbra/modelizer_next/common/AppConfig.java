package lu.kbra.modelizer_next.common;

import java.awt.event.InputEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

import lu.kbra.modelizer_next.ui.ThemeMode;
import lu.kbra.pclib.PCUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * User configuration loaded from and saved to the application configuration file.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {

	private ThemeMode themeMode = ThemeMode.SYSTEM;
	private String selectedPaletteName = "Default";
	private String defaultPaletteName = "Default";
	private List<String> pinnedPaletteNames = new ArrayList<>();
	private boolean autoCheckUpdates = true;
	private String skippedUpdateVersion;
	private boolean emulateMiddleClick = false;
	private Integer alternativeLiveEditKey;
	private int maxRecentFileCount = 15;
	private LinkedHashSet<Path> recentFiles = new LinkedHashSet<>();

	public class AppConfigEditor {

		protected AppConfigEditor() {
		}

		public void setThemeMode(final ThemeMode themeMode) {
			AppConfig.this.themeMode = themeMode;
		}

		public void setSelectedPaletteName(final String selectedPaletteName) {
			AppConfig.this.selectedPaletteName = selectedPaletteName;
		}

		public void setDefaultPaletteName(final String defaultPaletteName) {
			AppConfig.this.defaultPaletteName = defaultPaletteName;
		}

		public void setPinnedPaletteNames(final List<String> pinnedPaletteNames) {
			AppConfig.this.pinnedPaletteNames = pinnedPaletteNames;
		}

		public void setAutoCheckUpdates(final boolean autoCheckUpdates) {
			AppConfig.this.autoCheckUpdates = autoCheckUpdates;
		}

		public void setSkippedUpdateVersion(final String skippedUpdateVersion) {
			AppConfig.this.skippedUpdateVersion = skippedUpdateVersion;
		}

		public void setEmulateMiddleClick(final boolean emulateMiddleClick) {
			AppConfig.this.emulateMiddleClick = emulateMiddleClick;
		}

		public void setMaxRecentFileCount(final int maxRecentFileCount) {
			AppConfig.this.maxRecentFileCount = maxRecentFileCount;
		}

		public void setRecentFiles(final LinkedHashSet<Path> recentFiles) {
			AppConfig.this.recentFiles = recentFiles;
		}

		public void addRecentFile(final Path p) {
			AppConfig.this.recentFiles.remove(p);
			AppConfig.this.recentFiles.add(p);
			AppConfig.this.limitRecentFileCount();
		}

		public void setAlternativeLiveEditKey(final Integer alternativeLiveEditKey) {
			AppConfig.this.alternativeLiveEditKey = alternativeLiveEditKey == null || alternativeLiveEditKey == 0 ? null
					: alternativeLiveEditKey;
		}

		public List<String> getPinnedPaletteNames() {
			if (AppConfig.this.pinnedPaletteNames == null) {
				AppConfig.this.pinnedPaletteNames = new ArrayList<>();
			}
			return AppConfig.this.pinnedPaletteNames;
		}

		public int getAlternativeLiveEditKey() {
			return AppConfig.this.alternativeLiveEditKey == null ? InputEvent.ALT_DOWN_MASK : AppConfig.this.alternativeLiveEditKey;
		}

		public boolean hasAlternativeLiveEditKey() {
			return AppConfig.this.alternativeLiveEditKey != null;
		}

		public LinkedHashSet<Path> getRecentFiles() {
			if (AppConfig.this.recentFiles == null) {
				AppConfig.this.recentFiles = new LinkedHashSet<>();
			}
			return AppConfig.this.recentFiles;
		}

		public ThemeMode getThemeMode() {
			return themeMode;
		}

		public String getSelectedPaletteName() {
			return selectedPaletteName;
		}

		public String getDefaultPaletteName() {
			return defaultPaletteName;
		}

		public boolean isAutoCheckUpdates() {
			return autoCheckUpdates;
		}

		public String getSkippedUpdateVersion() {
			return skippedUpdateVersion;
		}

		public boolean isEmulateMiddleClick() {
			return emulateMiddleClick;
		}

		public int getMaxRecentFileCount() {
			return maxRecentFileCount;
		}

	}

	public List<String> getPinnedPaletteNames() {
		if (this.pinnedPaletteNames == null) {
			this.pinnedPaletteNames = new ArrayList<>();
		}
		return this.pinnedPaletteNames;
	}

	public int getAlternativeLiveEditKey() {
		return this.alternativeLiveEditKey == null ? InputEvent.ALT_DOWN_MASK : this.alternativeLiveEditKey;
	}

	public boolean hasAlternativeLiveEditKey() {
		return this.alternativeLiveEditKey != null;
	}

	public LinkedHashSet<Path> getRecentFiles() {
		if (this.recentFiles == null) {
			this.recentFiles = new LinkedHashSet<>();
		}
		return this.recentFiles;
	}

	public AppConfig limitRecentFileCount() {
		// remove start because those are the older files
		PCUtils.limitSize(this.recentFiles, this.maxRecentFileCount, false);
		return this;
	}

	public void edit(final Consumer<AppConfigEditor> configEditor) {
		configEditor.accept(this.new AppConfigEditor());
	}

}
