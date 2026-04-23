package lu.kbra.modelizer_next.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

final class DiagramCanvasActionRegistrar {

	record DiagramCanvasActions(Runnable renameSelection, IntConsumer moveFieldSelection, IntConsumer moveSelectedFieldInList,
			Runnable addTable, Runnable addField, Runnable addComment, Runnable deleteSelection, Runnable duplicateSelection,
			Runnable clearSelection, Runnable addLink, Runnable selectAll, Runnable edit) {
	}

	private static void bind(
			final InputMap inputMap,
			final ActionMap actionMap,
			final KeyStroke keyStroke,
			final String actionKey,
			final Runnable action) {
		inputMap.put(keyStroke, actionKey);
		actionMap.put(actionKey, new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
	}

	static void installDefault(final JComponent component, final DiagramCanvasActions actions) {
		final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = component.getActionMap();

		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSelection", actions.renameSelection());
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				"selectFieldUp",
				() -> actions.moveFieldSelection().accept(-1));
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				"selectFieldDown",
				() -> actions.moveFieldSelection().accept(1));
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
				"moveFieldUp",
				() -> actions.moveSelectedFieldInList().accept(-1));
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
				"moveFieldDown",
				() -> actions.moveSelectedFieldInList().accept(1));
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "addTable", actions.addTable());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "addField", actions.addField());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "addComment", actions.addComment());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit", actions.edit());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelection", actions.deleteSelection());
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK),
				"deleteSelection",
				actions.deleteSelection());
		bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
				"duplicateSelection",
				actions.duplicateSelection());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection", actions.clearSelection());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "addLink", actions.addLink());
		bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "selectAll", actions.selectAll());
	}

	private DiagramCanvasActionRegistrar() {
	}
}
