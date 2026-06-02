package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import lu.kbra.modelizer_next.common.App;
import lu.kbra.modelizer_next.common.AppConfig;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DiagramCanvasActions;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

/**
 * Registers keyboard actions used by the diagram canvas.
 */
interface DiagramCanvasActionRegistrar extends DiagramCanvasExt {

	/**
	 * Adds the action to the action map of the active canvas.
	 *
	 * @param inputMap  Swing input map to update
	 * @param actionMap Swing action map to update
	 * @param actionKey key under which the action is registered
	 * @param action    action to register or execute
	 */
	default void installActionBind(final InputMap inputMap, final ActionMap actionMap, final String actionKey, final Runnable action) {
		actionMap.put(actionKey, new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}

		});
	}

	/**
	 * Installs the default key bindings on the active canvas.
	 *
	 * @param actions actions value used by the operation
	 */
	default void installDefaultKeyBindings(final DiagramCanvasActions actions) {
		final InputMap inputMap = this.getCanvas().getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = this.getCanvas().getActionMap();

		final AppConfig config = App.CONFIG;

		this.installKeyBind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER), "undo", actions.undo());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK),
				"redo",
				actions.redo());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK),
				"editSelectionStyle",
				actions.editStyle());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | config.getAlternativeLiveEditKey()),
				"editSelectionStyleAlt",
				actions.editStyleAlt());

		this.installKeyBind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSelection", actions.renameSelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, config.getAlternativeLiveEditKey()),
				"renameSelectionAlt",
				actions.renameSelectionAlt());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				"selectFieldUp",
				() -> actions.moveFieldSelection().accept(-1));
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				"selectFieldDown",
				() -> actions.moveFieldSelection().accept(1));

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
				"moveFieldUp",
				() -> actions.moveSelectedFieldInList().accept(-1));
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
				"moveFieldDown",
				() -> actions.moveSelectedFieldInList().accept(1));

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, MainFrame.CTRL_MODIFIER),
				"addTable",
				actions.addTable());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, MainFrame.CTRL_MODIFIER),
				"addField",
				actions.addField());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK),
				"addComment",
				actions.addComment());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_L, MainFrame.CTRL_MODIFIER),
				"addLink",
				actions.addLink());

		this.installKeyBind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, MainFrame.CTRL_MODIFIER), "edit", actions.edit());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"deleteSelection",
				actions.deleteSelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK),
				"deleteSelection",
				actions.deleteSelection());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, MainFrame.CTRL_MODIFIER),
				"duplicateSelection",
				actions.duplicateSelection());

		this.installKeyBind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection", actions.clearSelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, MainFrame.CTRL_MODIFIER),
				"selectAll",
				actions.selectAll());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, MainFrame.CTRL_MODIFIER),
				"copySelection",
				actions.copySelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, MainFrame.CTRL_MODIFIER),
				"cutSelection",
				actions.cutSelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, MainFrame.CTRL_MODIFIER),
				"pasteSelection",
				actions.pasteSelection());

		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0),
				"focusSelection",
				actions.focusSelection());
		this.installKeyBind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "focusSelection", actions.focusSelection());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, MainFrame.CTRL_MODIFIER),
				"focusAll",
				actions.focusAll());
		this.installKeyBind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK),
				"focusAll",
				actions.focusAll());

		this.installActionBind(inputMap, actionMap, "syncPositionPrevious", actions.syncPositionPrevious());
		this.installActionBind(inputMap, actionMap, "syncPositionConceptual", actions.syncPositionConceptual());
		this.installActionBind(inputMap, actionMap, "syncPositionLogical", actions.syncPositionLogical());
		this.installActionBind(inputMap, actionMap, "syncPositionPhysical", actions.syncPositionPhysical());
		this.installActionBind(inputMap, actionMap, "syncSelectionPositionPrevious", actions.syncSelectionPositionPrevious());
		this.installActionBind(inputMap, actionMap, "syncSelectionPositionConceptual", actions.syncSelectionPositionConceptual());
		this.installActionBind(inputMap, actionMap, "syncSelectionPositionLogical", actions.syncSelectionPositionLogical());
		this.installActionBind(inputMap, actionMap, "syncSelectionPositionPhysical", actions.syncSelectionPositionPhysical());
	}

	/**
	 * Installs the key bind on the active canvas.
	 *
	 * @param inputMap  Swing input map to update
	 * @param actionMap Swing action map to update
	 * @param keyStroke keyboard shortcut to register
	 * @param actionKey key under which the action is registered
	 * @param action    action to register or execute
	 */
	default void installKeyBind(
			final InputMap inputMap,
			final ActionMap actionMap,
			final KeyStroke keyStroke,
			final String actionKey,
			final Runnable action) {
		inputMap.put(keyStroke, actionKey);
		this.installActionBind(inputMap, actionMap, actionKey, action);
	}

	default void resetKeyBinds() {
		final DiagramCanvas canvas = this.getCanvas();
		canvas.setInputMap(JComponent.WHEN_FOCUSED, new InputMap());
		canvas.setActionMap(new ActionMap());
	}

}
