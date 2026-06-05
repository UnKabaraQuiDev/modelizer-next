package lu.kbra.modelizer_next.ui.frame;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.DiagramCanvas;
import lu.kbra.pclib.PCUtils;

/**
 * Toolbar builder for the main frame.
 */
final class MainFrameToolBar extends JToolBar {

	public record ToolbarDropdownAction(String text, String actionKey, int mnemonic, String mnemonicIndex) {

	}

	private static final long serialVersionUID = 1L;

	/**
	 * Scales an icon to the requested size.
	 *
	 * @param icon         icon value used by the operation
	 * @param targetWidth  width value
	 * @param targetHeight height value
	 * @return the scale icon result
	 */
	private static ImageIcon scaleIcon(final ImageIcon icon, final int targetWidth, final int targetHeight) {
		BufferedImage current = MainFrameToolBar.toBufferedImage(icon.getImage());
		int width = current.getWidth();
		int height = current.getHeight();
		while (width > targetWidth || height > targetHeight) {
			width = Math.max(targetWidth, width / 2);
			height = Math.max(targetHeight, height / 2);
			final BufferedImage next = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = next.createGraphics();
			try {
				g.setComposite(AlphaComposite.Src);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(current, 0, 0, width, height, null);
			} finally {
				g.dispose();
			}
			current = next;
		}
		return new ImageIcon(current);
	}

	/**
	 * Converts the input to a buffered image.
	 *
	 * @param image image value used by the operation
	 * @return the to buffered image result
	 */
	private static BufferedImage toBufferedImage(final Image image) {
		if (image instanceof final BufferedImage bufferedImage) {
			return bufferedImage;
		}
		final BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = buffered.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(image, 0, 0, null);
		} finally {
			g.dispose();
		}
		return buffered;
	}

	private final Map<String, ImageIcon> toolbarIconCache = new HashMap<>();

	JButton undoButton;
	JButton redoButton;

	private final Map<JButton, BooleanSupplier> buttonEnabledSuppliers = new LinkedHashMap<>();

	/**
	 * Creates a main frame tool bar instance.
	 *
	 * @param frame frame that owns the created UI component
	 */
	MainFrameToolBar(final MainFrame frame) {
		this.setFloatable(false);
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());

		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		buttons.add(this.undoButton = this.createToolbarButton(frame, "undo.png", "Undo", "undo", frame.session::canUndo));
		buttons.add(this.redoButton = this.createToolbarButton(frame, "redo.png", "Redo", "redo", frame.session::canRedo));
		buttons.add(this.createToolbarButton(frame, "add-table.png", "New table", "addTable"));
		buttons.add(this.createToolbarButton(frame, "add-field.png", "New field", "addField"));
		buttons.add(this.createToolbarButton(frame, "add-comment.png", "New comment", "addComment"));
		buttons.add(this.createToolbarButton(frame, "add-link.png", "New link", "addLink"));
		buttons.add(this.createToolbarButton(frame, "delete.png", "Delete", "deleteSelection"));
		buttons.add(this.createToolbarButton(frame, "duplicate.png", "Duplicate", "duplicateSelection"));
		buttons.add(this.createToolbarButton(frame, "focus-selection.png", "Focus selection", "focusSelection"));
		buttons.add(this.createToolbarButton(frame, "focus-all.png", "Focus all", "focusAll"));
		buttons.add(this.createToolbarDropdownButton(frame,
				"sync-position.png",
				"Synchronize position with previous tab",
				"syncPositionPrevious",
				() -> frame.getActiveCanvas().getPanelType().previous() != null,
				List.of(new ToolbarDropdownAction("Synchronize positions with Conceptual panel",
						"syncPositionConceptual",
						KeyEvent.VK_C,
						"Conceptual"),
						new ToolbarDropdownAction("Synchronize positions with Logical panel",
								"syncPositionLogical",
								KeyEvent.VK_L,
								"Logical"),
						new ToolbarDropdownAction("Synchronize positions with Physical panel",
								"syncPositionPhysical",
								KeyEvent.VK_P,
								"Physical")),
				actionKey -> PanelType.valueOf(actionKey.replace("syncPosition", "").toUpperCase()) != frame.getActiveCanvas()
						.getPanelType()));
		buttons.add(this.createToolbarDropdownButton(frame,
				"sync-selection-position.png",
				"Synchronize position of selected elements with previous tab",
				"syncSelectionPositionPrevious",
				() -> frame.getActiveCanvas().getPanelType().previous() != null,
				List.of(new ToolbarDropdownAction("Synchronize positions of selected elements with Conceptual panel",
						"syncSelectionPositionConceptual",
						KeyEvent.VK_C,
						"Conceptual"),
						new ToolbarDropdownAction("Synchronize positions of selected elements with Logical panel",
								"syncSelectionPositionLogical",
								KeyEvent.VK_L,
								"Logical"),
						new ToolbarDropdownAction("Synchronize positions of selected elements with Physical panel",
								"syncSelectionPositionPhysical",
								KeyEvent.VK_P,
								"Physical")),
				actionKey -> PanelType.valueOf(actionKey.replace("syncSelectionPosition", "").toUpperCase()) != frame.getActiveCanvas()
						.getPanelType()));

		this.add(buttons, BorderLayout.WEST);
	}

	public void refreshToolbarState(final MainFrame frame) {
		for (int i = 0; i < super.getComponentCount(); i++) {
			if (super.getComponent(i) instanceof final JButton button) {
				final String actionKey = (String) button.getClientProperty("actionKey");
				final String baseText = (String) button.getClientProperty("baseText");
				if (actionKey == null || baseText == null) {
					continue;
				}

				final DiagramCanvas canvas = frame.getActiveCanvas();
				final String shortcutText = canvas == null ? "" : frame.findShortcutText(canvas, actionKey);
				button.setText(shortcutText.isBlank() ? baseText : baseText + " (" + shortcutText + ")");
			}
		}

		for (final Map.Entry<JButton, BooleanSupplier> entry : this.buttonEnabledSuppliers.entrySet()) {
			final JButton button = entry.getKey();
			final BooleanSupplier enabledSupplier = entry.getValue();

			button.setEnabled(Boolean.TRUE.equals(enabledSupplier.getAsBoolean()));
		}
	}

	/**
	 * Creates a toolbar button.
	 *
	 * @param frame       frame that owns the created UI component
	 * @param icon        text value for icon
	 * @param description text value for description
	 * @param actionKey   key under which the action is registered
	 * @return the created toolbar button
	 */
	private JButton createToolbarButton(final MainFrame frame, final String icon, final String description, final String actionKey) {
		return this.createToolbarButton(frame, icon, description, actionKey, null);
	}

	/**
	 * Creates a toolbar button.
	 *
	 * @param frame           frame that owns the created UI component
	 * @param icon            text value for icon
	 * @param description     text value for description
	 * @param actionKey       key under which the action is registered
	 * @param enabledSupplier supplier to change if the button is enabled
	 * @return the created toolbar button
	 */
	private JButton createToolbarButton(
			final MainFrame frame,
			final String icon,
			final String description,
			final String actionKey,
			final BooleanSupplier enabledSupplier) {

		final JButton button = new JButton();
		button.setIcon(this.getToolbarIcon(frame, icon));

		button.putClientProperty("baseText", description);
		button.putClientProperty("actionKey", actionKey);

		button.addActionListener(event -> {
			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas == null) {
				return;
			}

			final ActionEvent actionEvent = new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey);

			final Action action = canvas.getActionMap().get(actionKey);
			if (action != null) {
				action.actionPerformed(actionEvent);
				canvas.requestFocusInWindow();
			}
		});

		final DiagramCanvas canvas = frame.getActiveCanvas();
		if (canvas != null) {
			final String shortcutText = frame.findShortcutText(canvas, actionKey);
			if (!shortcutText.isBlank()) {
				button.setToolTipText(description + " (" + shortcutText + ")");
			}
		}

		button.setPreferredSize(new Dimension(40, 40));

		this.registerButtonEnabledSupplier(button, enabledSupplier);

		return button;
	}

	/**
	 * Creates a toolbar button with a separated dropdown area.
	 *
	 * @param frame                    frame that owns the created UI component
	 * @param icon                     text value for icon
	 * @param description              text value for description
	 * @param actionKey                key under which the main action is registered
	 * @param subActions               dropdown actions, mapped as display text -> action key
	 * @param subActionEnabledProvider enabled-state provider for dropdown actions
	 * @return the created toolbar button component
	 */
	private JPanel createToolbarDropdownButton(
			final MainFrame frame,
			final String icon,
			final String description,
			final String actionKey,
			final List<ToolbarDropdownAction> subActions,
			final Predicate<String> subActionEnabledProvider) {
		return this.createToolbarDropdownButton(frame, icon, description, actionKey, null, subActions, subActionEnabledProvider);
	}

	/**
	 * Creates a toolbar button with a separated dropdown area.
	 *
	 * @param frame                    frame that owns the created UI component
	 * @param icon                     text value for icon
	 * @param description              text value for description
	 * @param actionKey                key under which the main action is registered
	 * @param enabledSupplier          determines if the main button should be enabled or not
	 * @param subActions               dropdown actions, mapped as display text -> action key
	 * @param subActionEnabledProvider enabled-state provider for dropdown actions
	 * @return the created toolbar button component
	 */
	private JPanel createToolbarDropdownButton(
			final MainFrame frame,
			final String icon,
			final String description,
			final String actionKey,
			final BooleanSupplier enabledSupplier,
			final List<ToolbarDropdownAction> subActions,
			final Predicate<String> subActionEnabledProvider) {

		final JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setPreferredSize(new Dimension(58, 40));
		wrapper.setMaximumSize(new Dimension(58, 40));
		wrapper.setOpaque(false);

		final JButton mainButton = new JButton();
		mainButton.setIcon(this.getToolbarIcon(frame, icon));
		mainButton.setToolTipText(description);
		mainButton.putClientProperty("baseText", description);
		mainButton.putClientProperty("actionKey", actionKey);
		mainButton.setPreferredSize(new Dimension(40, 40));
//		mainButton.setFocusable(false);

		final JButton dropdownButton = new JButton("▾");
		dropdownButton.setPreferredSize(new Dimension(18, 40));
//		dropdownButton.setFocusable(false);
		dropdownButton.setMargin(new Insets(0, 0, 0, 0));
		dropdownButton.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));

		final JPopupMenu popupMenu = new JPopupMenu();
		final Map<JMenuItem, String> menuItems = new LinkedHashMap<>();

		for (final ToolbarDropdownAction entry : subActions) {
			final String subDescription = entry.text();
			final String subActionKey = entry.actionKey();
			final Integer mnemonic = entry.mnemonic();
			final String mnemonicIndex = entry.mnemonicIndex();

			final JMenuItem item = new JMenuItem(subDescription);
			if (mnemonic != null && mnemonic.intValue() != 0) {
				item.setMnemonic(mnemonic);

				final int index = subDescription.toUpperCase()
						.indexOf((mnemonicIndex == null ? KeyEvent.getKeyText(mnemonic) : mnemonicIndex).toUpperCase());
				if (index >= 0) {
					item.setDisplayedMnemonicIndex(index);
				}
			}

			final DiagramCanvas canvas = frame.getActiveCanvas();
			if (canvas != null) {
				final String shortcutText = frame.findShortcutText(canvas, subActionKey);
				if (!shortcutText.isBlank()) {
					item.setText(subDescription + " (" + shortcutText + ")");
				}
			}

			item.addActionListener(event -> this.performCanvasAction(frame, subActionKey));

			menuItems.put(item, subActionKey);
			popupMenu.add(item);
		}

		mainButton.addActionListener(event -> this.performCanvasAction(frame, actionKey));

		dropdownButton.addActionListener(event -> {
			for (final Map.Entry<JMenuItem, String> entry : menuItems.entrySet()) {
				final JMenuItem item = entry.getKey();
				final String subActionKey = entry.getValue();

				item.setEnabled(subActionEnabledProvider.test(subActionKey));
			}

			dropdownButton.requestFocusInWindow();
			popupMenu.show(dropdownButton, 0, dropdownButton.getHeight());

			SwingUtilities.invokeLater(() -> {
				popupMenu.requestFocusInWindow();

				for (final JMenuItem item : menuItems.keySet()) {
					if (item.isEnabled()) {
						item.requestFocusInWindow();
						break;
					}
				}
			});
		});

		final DiagramCanvas canvas = frame.getActiveCanvas();
		if (canvas != null) {
			final String shortcutText = frame.findShortcutText(canvas, actionKey);
			if (!shortcutText.isBlank()) {
				mainButton.setToolTipText(description + " (" + shortcutText + ")");
			}
		}

		wrapper.add(mainButton, BorderLayout.CENTER);
		wrapper.add(dropdownButton, BorderLayout.EAST);

		this.registerButtonEnabledSupplier(mainButton, enabledSupplier);

		return wrapper;
	}

	/**
	 * Returns the toolbar icon.
	 *
	 * @param frame frame that owns the created UI component
	 * @param icon  text value for icon
	 * @return the toolbar icon
	 */
	private ImageIcon getToolbarIcon(final MainFrame frame, final String icon) {
		return this.toolbarIconCache.computeIfAbsent(icon, key -> {
			final ImageIcon rawIcon = new ImageIcon(PCUtils.readPackagedBytesFile(frame.getClass(), "/icons/" + key));

			return MainFrameToolBar.scaleIcon(rawIcon, 34, 34);
		});
	}

	private void performCanvasAction(final MainFrame frame, final String actionKey) {
		final DiagramCanvas canvas = frame.getActiveCanvas();
		if (canvas == null) {
			return;
		}

		final ActionEvent actionEvent = new ActionEvent(canvas, ActionEvent.ACTION_PERFORMED, actionKey);

		final Action action = canvas.getActionMap().get(actionKey);
		if (action != null) {
			action.actionPerformed(actionEvent);
			canvas.requestFocusInWindow();
		}
	}

	private void registerButtonEnabledSupplier(final JButton button, final BooleanSupplier enabledSupplier) {
		if (enabledSupplier == null) {
			return;
		}

		this.buttonEnabledSuppliers.put(button, enabledSupplier);
		button.setEnabled(Boolean.TRUE.equals(enabledSupplier.getAsBoolean()));
	}

}
