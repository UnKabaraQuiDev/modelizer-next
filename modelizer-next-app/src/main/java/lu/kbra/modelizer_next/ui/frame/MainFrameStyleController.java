package lu.kbra.modelizer_next.ui.frame;

import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.style.StylePaletteService;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.modelizer_next.ui.canvas.data.StyleScope;
import lu.kbra.modelizer_next.ui.canvas.datastruct.StatusStyleAppearance;
import lu.kbra.modelizer_next.ui.dialogs.StylePaletteEditorDialog;

/**
 * Style and palette actions implemented by the main frame.
 */
public interface MainFrameStyleController {

	/**
	 * Creates a default style menu.
	 * @return the created default style menu
	 */
	default JMenu createDefaultStyleMenu() {
		final MainFrame frame = (MainFrame) this;
		final JMenu defaultMenu = new JMenu("Default style");
		final ButtonGroup defaultGroup = new ButtonGroup();

		for (final StylePalette palette : frame.palettes) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(palette.getName());
			item.setSelected(palette.getName().equals(App.CONFIG.getDefaultPaletteName()));
			item.addActionListener(event -> {
				App.CONFIG.setDefaultPaletteName(palette.getName());
				App.saveConfig();
				this.setDefaultPaletteToCanvases();
			});
			defaultGroup.add(item);
			defaultMenu.add(item);
		}
		return defaultMenu;
	}

	/**
	 * Creates a pin menu.
	 * @return the created pin menu
	 */
	default JMenu createPinMenu() {
		final MainFrame frame = (MainFrame) this;
		final JMenu pinMenu = new JMenu("Pin to status bar");
		for (final StylePalette palette : frame.palettes) {
			final JCheckBoxMenuItem item = new JCheckBoxMenuItem(palette.getName(),
					App.CONFIG.getPinnedPaletteNames().contains(palette.getName()));
			item.addActionListener(event -> this.setPalettePinned(palette.getName(), item.isSelected()));
			pinMenu.add(item);
		}
		return pinMenu;
	}

	/**
	 * Creates a pinned style button.
	 * @param palette palette value used by the operation
	 * @param previewType type value to use
	 * @return the created pinned style button
	 */
	default JButton createPinnedStyleButton(final StylePalette palette, final StyleScope previewType) {
		final MainFrame frame = (MainFrame) this;
		final JButton button = new JButton(palette.getName());
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setMargin(new Insets(3, 10, 3, 10));

		final StatusStyleAppearance appearance = this.resolvePinnedStyleAppearance(palette, previewType);
		button.setForeground(appearance.foreground());
		button.setBackground(appearance.background());
		button.setOpaque(true);
		button.setContentAreaFilled(true);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(appearance.border(), 2),
				BorderFactory.createEmptyBorder(2, 4, 2, 4)));
		button.setToolTipText("Apply style to the current selection");
		button.addActionListener(event -> {
			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			canvas.applyPaletteToSelection(palette);
			canvas.requestFocusInWindow();
			App.CONFIG.setSelectedPaletteName(palette.getName());
			App.saveConfig();
		});
		final DragListener dragListener = (DragListener) frame.pinnedStylesPanel.getClientProperty("dragListener");
		button.addMouseListener(dragListener);
		button.addMouseMotionListener(dragListener);
		return button;
	}

	/**
	 * Creates a reload styles item.
	 * @return the created reload styles item
	 */
	default JMenuItem createReloadStylesItem() {
		final JMenuItem reloadItem = new JMenuItem("Reload styles");
		reloadItem.addActionListener(event -> this.reloadStyles());
		return reloadItem;
	}

	/**
	 * Finds the palette by name that matches the supplied input.
	 * @param paletteName name value to use
	 * @return the matching palette by name, or {@code null} when no match exists
	 */
	default StylePalette findPaletteByName(final String paletteName) {
		final MainFrame frame = (MainFrame) this;
		if (paletteName == null || paletteName.isBlank()) {
			return null;
		}

		for (final StylePalette palette : frame.palettes) {
			if (paletteName.equals(palette.getName())) {
				return palette;
			}
		}
		return null;
	}

	/**
	 * Blends the given color with white by the supplied ratio.
	 * @param color color value to use
	 * @param amount numeric amount value
	 * @return the mix with white result
	 */
	default Color mixWithWhite(final Color color, final double amount) {
		if (color == null) {
			return Color.WHITE;
		}

		final double clampedAmount = Math.max(0.0, Math.min(1.0, amount));
		final int red = (int) Math.round(color.getRed() + (255 - color.getRed()) * clampedAmount);
		final int green = (int) Math.round(color.getGreen() + (255 - color.getGreen()) * clampedAmount);
		final int blue = (int) Math.round(color.getBlue() + (255 - color.getBlue()) * clampedAmount);
		return new Color(red, green, blue);
	}

	/**
	 * Rebuilds the style menu from the available palettes.
	 * @param stylesMenu styles menu value used by the operation
	 */
	default void populateStylesMenu(final JMenu stylesMenu) {
		final MainFrame frame = (MainFrame) this;
		stylesMenu.removeAll();

		final JMenuItem newPaletteItem = new JMenuItem("New palette...");
		newPaletteItem.addActionListener(event -> {
			final StylePalette palette = StylePaletteEditorDialog.showDialog(frame);
			if (palette == null) {
				return;
			}

			StylePaletteService.save(palette);
			this.reloadStyles();
		});

		stylesMenu.add(newPaletteItem);
		stylesMenu.addSeparator();
		stylesMenu.add(new StyleApplyMenu(frame));
		stylesMenu.add(new StyleEditMenu(frame));
		stylesMenu.add(this.createPinMenu());
		stylesMenu.add(this.createDefaultStyleMenu());
		stylesMenu.addSeparator();
		stylesMenu.add(this.createReloadStylesItem());
	}

	/**
	 * Reloads style palettes and refreshes the related UI controls.
	 */
	default void reloadStyles() {
		final MainFrame frame = (MainFrame) this;
		frame.palettes = StylePaletteService.loadAll();
		this.sanitizePinnedPaletteNames();
		this.setDefaultPaletteToCanvases();
		frame.setJMenuBar(new MainFrameMenuBar(frame));
		frame.refreshPinnedStylesPanel();
		frame.revalidate();
		frame.repaint();
	}

	/**
	 * Updates a pinned palette name after the palette was renamed.
	 * @param oldName name value to use
	 * @param newName name value to use
	 */
	default void replacePinnedPaletteName(final String oldName, final String newName) {
		final MainFrame frame = (MainFrame) this;
		if (oldName == null || newName == null || oldName.equals(newName)) {
			return;
		}

		final List<String> updatedNames = new ArrayList<>();
		final LinkedHashSet<String> seen = new LinkedHashSet<>();
		boolean changed = false;

		for (final String paletteName : App.CONFIG.getPinnedPaletteNames()) {
			final String resolvedName = oldName.equals(paletteName) ? newName : paletteName;
			changed |= !resolvedName.equals(paletteName);
			if (seen.add(resolvedName)) {
				updatedNames.add(resolvedName);
			}
		}

		if (!changed) {
			return;
		}

		App.CONFIG.setPinnedPaletteNames(updatedNames);
		App.saveConfig();
	}

	/**
	 * Resolves the pinned style appearance from the current model and layout state.
	 * @param palette palette value used by the operation
	 * @param previewType type value to use
	 * @return the resolved pinned style appearance
	 */
	default StatusStyleAppearance resolvePinnedStyleAppearance(final StylePalette palette, final StyleScope previewType) {
		if (palette == null) {
			return new StatusStyleAppearance(Color.BLACK, Color.WHITE, Color.GRAY);
		}

		return switch (previewType) {
		case FIELD ->
			new StatusStyleAppearance(palette.getFieldTextColor(), palette.getFieldBackgroundColor(), palette.getFieldTextColor());
		case COMMENT ->
			new StatusStyleAppearance(palette.getCommentTextColor(), palette.getCommentBackgroundColor(), palette.getCommentBorderColor());
		case LINK ->
			new StatusStyleAppearance(palette.getLinkColor(), this.mixWithWhite(palette.getLinkColor(), 0.88), palette.getLinkColor());
		case NONE, CLASS ->
			new StatusStyleAppearance(palette.getClassTextColor(), palette.getClassBackgroundColor(), palette.getClassBorderColor());
		};
	}

	/**
	 * Sanitizes the pinned palette names so it can be used safely.
	 */
	default void sanitizePinnedPaletteNames() {
		final List<String> currentNames = new ArrayList<>(App.CONFIG.getPinnedPaletteNames());
		final List<String> sanitizedNames = new ArrayList<>();
		final LinkedHashSet<String> seen = new LinkedHashSet<>();

		for (final String paletteName : currentNames) {
			if (this.findPaletteByName(paletteName) != null && seen.add(paletteName)) {
				sanitizedNames.add(paletteName);
			}
		}

		if (sanitizedNames.equals(currentNames)) {
			return;
		}

		App.CONFIG.setPinnedPaletteNames(sanitizedNames);
		App.saveConfig();
	}

	/**
	 * Sets the default palette to canvases.
	 */
	default void setDefaultPaletteToCanvases() {
		final MainFrame frame = (MainFrame) this;
		final StylePalette palette = this.findPaletteByName(App.CONFIG.getDefaultPaletteName());
		frame.conceptualCanvas.setDefaultPalette(palette);
		frame.logicalCanvas.setDefaultPalette(palette);
		frame.physicalCanvas.setDefaultPalette(palette);
	}

	/**
	 * Sets the palette pinned.
	 * @param paletteName name value to use
	 * @param pinned whether pinned is enabled
	 */
	default void setPalettePinned(final String paletteName, final boolean pinned) {
		final MainFrame frame = (MainFrame) this;
		final LinkedHashSet<String> names = new LinkedHashSet<>(App.CONFIG.getPinnedPaletteNames());
		if (pinned) {
			names.add(paletteName);
		} else {
			names.remove(paletteName);
		}

		App.CONFIG.setPinnedPaletteNames(new ArrayList<>(names));
		App.saveConfig();
		frame.refreshPinnedStylesPanel();
	}

}
